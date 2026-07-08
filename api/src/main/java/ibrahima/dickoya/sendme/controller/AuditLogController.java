package ibrahima.dickoya.sendme.controller;

import ibrahima.dickoya.sendme.dto.response.ApiResponse;
import ibrahima.dickoya.sendme.dto.response.AuditLogResponse;
import ibrahima.dickoya.sendme.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * Contrôleur REST pour la consultation des logs d'audit centralisés.
 * <p>
 * Accessible uniquement aux administrateurs (ROLE_ADMIN).
 * Permet de visualiser tous les événements de la plateforme
 * avec filtres par catégorie, type, statut, utilisateur, période.
 */
@RestController
@RequestMapping("/api/admin/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Administration - Audit Logs", description = "Consultation des logs d'audit centralisés (réservé admin)")
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    private final AuditService auditService;

    @GetMapping
    @Operation(summary = "Liste paginée des logs d'audit")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAllLogs(
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                "Logs d'audit récupérés", auditService.getAllLogs(pageable)));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Logs par catégorie")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getLogsByCategory(
            @PathVariable String category,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                "Logs filtrés par catégorie", auditService.getLogsByCategory(category, pageable)));
    }

    @GetMapping("/type/{eventType}")
    @Operation(summary = "Logs par type d'événement")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getLogsByEventType(
            @PathVariable String eventType,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                "Logs filtrés par type", auditService.getLogsByEventType(eventType, pageable)));
    }

    @GetMapping("/user/{userEmail}")
    @Operation(summary = "Logs par utilisateur")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getLogsByUserEmail(
            @PathVariable String userEmail,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                "Logs filtrés par utilisateur", auditService.getLogsByUserEmail(userEmail, pageable)));
    }

    @GetMapping("/search")
    @Operation(summary = "Recherche avancée de logs")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> searchLogs(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String userEmail,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                "Recherche effectuée", auditService.getFilteredLogs(
                        category, eventType, status, userEmail, dateFrom, dateTo, pageable)));
    }

    @GetMapping("/stats/category")
    @Operation(summary = "Statistiques par catégorie")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStatsByCategory() {
        return ResponseEntity.ok(ApiResponse.success(
                "Statistiques par catégorie", auditService.getCountByCategory()));
    }

    @GetMapping("/stats/type")
    @Operation(summary = "Statistiques par type d'événement")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStatsByEventType() {
        return ResponseEntity.ok(ApiResponse.success(
                "Statistiques par type", auditService.getCountByEventType()));
    }

    @GetMapping("/stats/status")
    @Operation(summary = "Statistiques par statut")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStatsByStatus() {
        return ResponseEntity.ok(ApiResponse.success(
                "Statistiques par statut", auditService.getCountByStatus()));
    }
}
