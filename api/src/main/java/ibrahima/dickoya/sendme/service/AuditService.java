package ibrahima.dickoya.sendme.service;

import ibrahima.dickoya.sendme.dto.response.AuditLogResponse;
import ibrahima.dickoya.sendme.entity.AuditLog;
import ibrahima.dickoya.sendme.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service centralisé de traçage des événements d'audit.
 * <p>
 * Tous les événements de la plateforme (AUTH, TRANSFER, ADMIN, CONFIG, SYSTEM)
 * sont enregistrés dans la table unique {@code audit_logs}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository repository;

    /**
     * Enregistre un événement d'audit.
     */
    @Transactional
    public void logEvent(
            String category,
            String eventType,
            String userEmail,
            Long userId,
            Long resourceId,
            String resourceType,
            String status,
            String message,
            String details,
            HttpServletRequest request) {

        try {
            AuditLog auditLog = AuditLog.builder()
                    .category(category)
                    .eventType(eventType)
                    .userEmail(userEmail)
                    .userId(userId)
                    .resourceId(resourceId)
                    .resourceType(resourceType)
                    .status(status != null ? status : "SUCCESS")
                    .message(message)
                    .details(details)
                    .ipAddress(request != null ? extractIpAddress(request) : null)
                    .userAgent(request != null ? request.getHeader("User-Agent") : null)
                    .build();

            repository.save(auditLog);
        } catch (Exception e) {
            log.warn("[AUDIT] Erreur lors de l'enregistrement du log : {}", e.getMessage());
        }
    }

    /** Version simplifiée sans requête HTTP. */
    @Transactional
    public void logEvent(String category, String eventType, String userEmail, Long userId,
            String status, String message) {
        logEvent(category, eventType, userEmail, userId, null, null, status, message, null, null);
    }

    /** Version encore plus simple (succès par défaut). */
    @Transactional
    public void logEvent(String category, String eventType, String userEmail, String message) {
        logEvent(category, eventType, userEmail, null, null, null, "SUCCESS", message, null, null);
    }

    // ── Consultation ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAllLogs(Pageable pageable) {
        return repository.findAllByOrderByCreatedAtDesc(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getLogsByCategory(String category, Pageable pageable) {
        return repository.findByCategoryOrderByCreatedAtDesc(category, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getLogsByEventType(String eventType, Pageable pageable) {
        return repository.findByEventTypeOrderByCreatedAtDesc(eventType, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getLogsByUserEmail(String userEmail, Pageable pageable) {
        return repository.findByUserEmailOrderByCreatedAtDesc(userEmail, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getFilteredLogs(
            String category, String eventType, String status, String userEmail,
            LocalDate dateFrom, LocalDate dateTo, Pageable pageable) {

        LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime to = dateTo != null ? dateTo.atTime(LocalTime.MAX) : null;

        return repository.findFiltered(category, eventType, status, userEmail, from, to, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getCountByCategory() {
        Map<String, Long> result = new HashMap<>();
        repository.countByCategory().forEach(row -> result.put((String) row[0], (Long) row[1]));
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getCountByEventType() {
        Map<String, Long> result = new HashMap<>();
        repository.countByEventType().forEach(row -> result.put((String) row[0], (Long) row[1]));
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getCountByStatus() {
        Map<String, Long> result = new HashMap<>();
        repository.countByStatus().forEach(row -> result.put((String) row[0], (Long) row[1]));
        return result;
    }

    // ── Utilitaires ──────────────────────────────────────────────────────

    private AuditLogResponse toResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .category(log.getCategory())
                .eventType(log.getEventType())
                .userEmail(log.getUserEmail())
                .userId(log.getUserId())
                .resourceId(log.getResourceId())
                .resourceType(log.getResourceType())
                .status(log.getStatus())
                .message(log.getMessage())
                .details(log.getDetails())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .createdAt(log.getCreatedAt())
                .build();
    }

    private String extractIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("X-Real-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        return ipAddress;
    }
}
