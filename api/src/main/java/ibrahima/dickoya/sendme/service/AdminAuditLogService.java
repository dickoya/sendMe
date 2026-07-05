package ibrahima.dickoya.sendme.service;

import ibrahima.dickoya.sendme.dto.response.AdminLogResponse;
import ibrahima.dickoya.sendme.entity.AdminAuditLog;
import ibrahima.dickoya.sendme.repository.AdminAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Service pour la consultation des logs d'audit admin.
 * <p>
 * Les logs sont créés automatiquement par l'aspect AOP {@code AuditAspect}
 * lorsqu'une méthode est annotée {@code @Auditable}.
 */
@Service
@RequiredArgsConstructor
public class AdminAuditLogService {

    private final AdminAuditLogRepository auditLogRepository;

    /**
     * Récupère les logs d'audit avec filtres optionnels.
     *
     * @param action     filtre par type d'action (null = tous)
     * @param adminEmail filtre par email admin (null = tous)
     * @param dateFrom   date de début (null = pas de limite)
     * @param dateTo     date de fin (null = pas de limite)
     * @param pageable   paramètres de pagination et tri
     * @return page de AdminLogResponse
     */
    public Page<AdminLogResponse> getFilteredLogs(
            String action,
            String adminEmail,
            LocalDate dateFrom,
            LocalDate dateTo,
            Pageable pageable) {

        LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime to = dateTo != null ? dateTo.atTime(LocalTime.MAX) : null;

        return auditLogRepository.findFiltered(action, adminEmail, from, to, pageable)
                .map(this::toResponse);
    }

    /**
     * Retourne la liste des types d'actions distincts pour le filtre.
     */
    public List<String> getDistinctActions() {
        return auditLogRepository.findDistinctActions();
    }

    private AdminLogResponse toResponse(AdminAuditLog log) {
        return AdminLogResponse.builder()
                .id(log.getId())
                .adminEmail(log.getAdminEmail())
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .details(log.getDetails())
                .ipAddress(log.getIpAddress())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
