package ibrahima.dickoya.sendme.controller;

import ibrahima.dickoya.sendme.dto.request.FeeConfigRequest;
import ibrahima.dickoya.sendme.dto.request.PaymentChannelRequest;
import ibrahima.dickoya.sendme.dto.response.*;
import ibrahima.dickoya.sendme.enums.Role;
import ibrahima.dickoya.sendme.enums.TransactionStatus;
import ibrahima.dickoya.sendme.service.AdminAuditLogService;
import ibrahima.dickoya.sendme.service.AdminService;
import ibrahima.dickoya.sendme.service.ExchangeRateService;
import ibrahima.dickoya.sendme.service.PaymentChannelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST gérant tous les endpoints d'administration.
 *
 * ACCÈS RESTREINT : Toutes les routes de ce contrôleur nécessitent
 * le rôle ROLE_ADMIN. La vérification est faite par Spring Security
 * via l'annotation @PreAuthorize sur chaque méthode.
 *
 * Un utilisateur normal qui tente d'accéder à ces routes
 * recevra une erreur 403 Forbidden.
 *
 * Routes disponibles :
 * GET /api/admin/dashboard → statistiques globales
 * GET /api/admin/users → liste paginée des utilisateurs
 * GET /api/admin/users/{id} → détail d'un utilisateur
 * PUT /api/admin/users/{id}/toggle → activer/désactiver un compte
 * GET /api/admin/transactions → toutes les transactions
 * GET /api/admin/transactions/status → transactions par statut
 * PUT /api/admin/config/fees → modifier le taux de commission
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Validated
@Tag(name = "Administration", description = """
                Module d'administration — accès restreint au rôle ROLE_ADMIN.

                **Compte admin par défaut :**
                - Email : `admin@sendme.com`
                - Mot de passe : `Admin@sendme2025`

                **Fonctionnalités :**
                - Tableau de bord avec statistiques en temps réel
                - Gestion des comptes utilisateurs
                - Consultation de toutes les transactions
                - Paramétrage des frais de la plateforme

                 Toute tentative d'accès sans le rôle ADMIN retourne **403 Forbidden**.
                """)
public class AdminController {

        /** Service contenant toute la logique métier admin. */
        private final AdminService adminService;
        private final ExchangeRateService exchangeRateService;
        private final AdminAuditLogService adminAuditLogService;
        private final PaymentChannelService paymentChannelService;

        /**
         * Retourne le tableau de bord avec toutes les statistiques.
         *
         * Contient :
         * - Statistiques utilisateurs (total, actifs, inactifs)
         * - Statistiques transactions (par statut, taux de succès)
         * - Statistiques financières (volume total, frais, stats du jour)
         * - Répartition par canal de réception
         * - Nombre de connexions SSE actives
         *
         * @return 200 OK avec DashboardResponse
         */
        @GetMapping("/dashboard")
        @PreAuthorize("hasRole('ADMIN')")
        // getDashboard()
        @Operation(summary = "Tableau de bord", description = "Retourne toutes les statistiques globales de la plateforme "
                        +
                        "en temps réel : utilisateurs, transactions, volume financier, " +
                        "répartition par canal et connexions SSE actives.")

        public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {

                DashboardResponse dashboard = adminService.getDashboard();
                return ResponseEntity.ok(
                                ApiResponse.success("Tableau de bord administrateur", dashboard));
        }

        @GetMapping("/debug/rate")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Debug taux de change", description = "Retourne le taux brut, le taux avec marge et le timestamp du cache.")
        public ResponseEntity<ApiResponse<Map<String, Object>>> debugRate(
                        @RequestParam(defaultValue = "EUR") String from,
                        @RequestParam(defaultValue = "XOF") String to) {

                Map<String, Object> debugInfo = new HashMap<>();
                BigDecimal rawRate = exchangeRateService.getRawRate(from, to);
                BigDecimal marginRate = exchangeRateService.getRate(from, to);

                debugInfo.put("from", from.toUpperCase());
                debugInfo.put("to", to.toUpperCase());
                debugInfo.put("rawRate", rawRate);
                debugInfo.put("marginRate", marginRate);
                debugInfo.put("cacheTimestamp", exchangeRateService.getCacheTimestamp(from));

                return ResponseEntity.ok(
                                ApiResponse.success("Debug taux de change", debugInfo));
        }

        /**
         * Récupère la liste paginée de tous les utilisateurs.
         *
         * Paramètres de pagination :
         * - page : numéro de page (commence à 0)
         * - size : éléments par page (défaut : 20)
         * - sort : champ de tri (ex: createdAt,desc)
         *
         * Exemple : GET /api/admin/users?page=0&size=20&sort=createdAt,desc
         *
         * @param page numéro de page
         * @param size taille de la page
         * @return 200 OK avec la page d'utilisateurs
         */
        @GetMapping("/users")
        @PreAuthorize("hasRole('ADMIN')")
        // getAllUsers()
        @Operation(summary = "Liste des utilisateurs", description = "Retourne la liste paginée de tous les utilisateurs avec "
                        +
                        "leurs statistiques (nombre de transactions, volume envoyé).")
        public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size) {

                Pageable pageable = PageRequest.of(page, size,
                                Sort.by("createdAt").descending());

                Page<UserResponse> users = adminService.getAllUsers(pageable);
                return ResponseEntity.ok(
                                ApiResponse.success("Liste des utilisateurs", users));
        }

        /**
         * Récupère le détail d'un utilisateur par son identifiant.
         * Inclut ses statistiques (nombre de transactions, volume envoyé).
         *
         * @param id identifiant de l'utilisateur
         * @return 200 OK avec UserResponse ou 404 si non trouvé
         */
        @GetMapping("/users/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<UserResponse>> getUserById(
                        @PathVariable Long id) {

                UserResponse user = adminService.getUserById(id);
                return ResponseEntity.ok(
                                ApiResponse.success("Detail de l'utilisateur", user));
        }

        /**
         * Active ou désactive un compte utilisateur.
         *
         * Si le compte est actif → il est désactivé (l'utilisateur ne peut plus se
         * connecter).
         * Si le compte est désactivé → il est réactivé.
         *
         * Cette action est irréversible côté utilisateur —
         * seul l'admin peut réactiver un compte.
         *
         * @param id identifiant de l'utilisateur à modifier
         * @return 200 OK avec l'utilisateur mis à jour
         */
        @PutMapping("/users/{id}/toggle")
        @PreAuthorize("hasRole('ADMIN')")
        // toggleUserStatus()
        @Operation(summary = "Activer / Désactiver un compte", description = "Inverse le statut isActive d'un utilisateur. "
                        +
                        "Un compte désactivé ne peut plus se connecter.")
        public ResponseEntity<ApiResponse<UserResponse>> toggleUserStatus(
                        @PathVariable Long id) {

                UserResponse user = adminService.toggleUserStatus(id);
                String message = user.getIsActive()
                                ? "Compte utilisateur active"
                                : "Compte utilisateur desactive";

                return ResponseEntity.ok(ApiResponse.success(message, user));
        }

        /**
         * Récupère toutes les transactions de la plateforme avec pagination.
         * L'admin voit les transactions de TOUS les utilisateurs.
         *
         * @param page   numéro de page
         * @param size   taille de la page
         * @param status filtre optionnel par statut
         * @return 200 OK avec la page de transactions
         */
        @GetMapping("/transactions")
        @PreAuthorize("hasRole('ADMIN')")
        // getAllTransactions()
        @Operation(summary = "Toutes les transactions", description = "Retourne toutes les transactions de la plateforme avec "
                        +
                        "filtrage optionnel par statut.")
        public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getAllTransactions(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @RequestParam(required = false) TransactionStatus status) {

                Pageable pageable = PageRequest.of(page, size,
                                Sort.by("createdAt").descending());

                Page<TransactionResponse> transactions;

                if (status != null) {
                        // Filtrer par statut si fourni
                        transactions = adminService.getTransactionsByStatus(status, pageable);
                } else {
                        // Toutes les transactions sans filtre
                        transactions = adminService.getAllTransactions(pageable);
                }

                return ResponseEntity.ok(
                                ApiResponse.success("Liste des transactions", transactions));
        }

        /**
         * Met à jour le taux de commission de la plateforme.
         *
         * Le nouveau taux est appliqué IMMÉDIATEMENT à tous les
         * nouveaux transferts sans redémarrage de l'application.
         * Les transactions en cours ne sont pas affectées.
         *
         * @param request le nouveau taux (entre 0% et 10%) et la raison
         * @return 200 OK avec message de confirmation
         */
        @PutMapping("/config/fees")
        @PreAuthorize("hasRole('ADMIN')")
        // updateFeeRate()
        @Operation(summary = "Modifier le taux de commission", description = "Met à jour le taux de commission appliqué à tous les "
                        +
                        "nouveaux transferts. Appliqué immédiatement sans redémarrage.")
        public ResponseEntity<ApiResponse<Void>> updateFeeRate(
                        @Valid @RequestBody FeeConfigRequest request) {

                adminService.updateFeeRate(request);
                return ResponseEntity.ok(ApiResponse.success(
                                "Taux de commission mis a jour : " + request.getFeeRate() + "%",
                                null));
        }

        /**
         * Envoie une notification de maintenance programmée
         * à tous les utilisateurs ayant un compte actif.
         *
         * Utilisé par l'administrateur pour prévenir les utilisateurs
         * avant une intervention technique sur la plateforme.
         *
         * Exemple :
         * POST /api/admin/notifications/maintenance
         * ?date=15 juin 2025 à 02:00
         * &duration=2 heures
         *
         * @param date     date et heure de la maintenance
         * @param duration durée estimée de la maintenance
         * @return 200 OK avec message de confirmation
         */
        @PostMapping("/notifications/maintenance")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Notifier une maintenance programmée", description = """
                        Envoie un email d'annonce de maintenance à TOUS
                        les utilisateurs ayant un compte actif.

                        Les emails sont envoyés de manière asynchrone
                        et n'impactent pas le temps de réponse de cet endpoint.
                        """)
        public ResponseEntity<ApiResponse<Void>> sendMaintenanceNotification(
                        @NotBlank(message = "La date de maintenance est obligatoire") @Parameter(description = "Date et heure de la maintenance", example = "15 juin 2025 à 02:00") @RequestParam String date,

                        @NotBlank(message = "La duree de maintenance est obligatoire") @Parameter(description = "Durée estimée de la maintenance", example = "2 heures") @RequestParam String duration) {

                adminService.sendMaintenanceNotification(date, duration);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Notification de maintenance envoyée à tous les utilisateurs actifs",
                                                null));
        }

        // ── NOUVEAUX ENDPOINTS : Filtres, Batch, Volume, Success Rate, Logs ──

        /**
         * Récupère les utilisateurs avec filtres avancés.
         * Supporte la recherche textuelle, le filtre par statut/rôle,
         * la période d'inscription, et le tri.
         */
        @GetMapping("/users/filter")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Utilisateurs filtrés", description = "Retourne la liste paginée des utilisateurs avec filtres avancés : "
                        +
                        "recherche textuelle, statut, rôle, période d'inscription.")
        public ResponseEntity<ApiResponse<Page<UserResponse>>> getFilteredUsers(
                        @RequestParam(required = false) String search,
                        @RequestParam(required = false) Boolean isActive,
                        @RequestParam(required = false) Role role,
                        @RequestParam(required = false) LocalDate dateFrom,
                        @RequestParam(required = false) LocalDate dateTo,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @RequestParam(defaultValue = "createdAt,desc") String sort) {

                String[] sortParams = sort.split(",");
                Sort sortObj = Sort.by(
                                sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc")
                                                ? Sort.Direction.ASC
                                                : Sort.Direction.DESC,
                                sortParams[0]);
                Pageable pageable = PageRequest.of(page, size, sortObj);

                Page<UserResponse> users = adminService.getFilteredUsers(
                                search, isActive, role, dateFrom, dateTo, pageable);
                return ResponseEntity.ok(
                                ApiResponse.success("Liste filtrée des utilisateurs", users));
        }

        /**
         * Active ou désactive plusieurs comptes en une seule opération.
         * Reçoit une liste d'IDs et une action (suspend/activate).
         */
        @PostMapping("/users/batch-toggle")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Actions en masse", description = "Active ou désactive plusieurs comptes utilisateurs en une seule requête.")
        public ResponseEntity<ApiResponse<Map<String, Object>>> batchToggleUsers(
                        @RequestBody Map<String, Object> request) {

                @SuppressWarnings("unchecked")
                List<Integer> userIdsRaw = (List<Integer>) request.get("userIds");
                boolean suspend = (boolean) request.getOrDefault("suspend", true);

                List<Long> userIds = userIdsRaw.stream()
                                .map(Integer::longValue)
                                .collect(java.util.stream.Collectors.toList());

                int count = adminService.batchToggleUsers(userIds, suspend);

                Map<String, Object> result = new HashMap<>();
                result.put("affected", count);
                result.put("action", suspend ? "suspended" : "activated");

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                count + " compte(s) " + (suspend ? "suspendu(s)" : "activé(s)"),
                                                result));
        }

        /**
         * Volume total par jour pour les X derniers jours.
         * Utilisé pour le graphique en barres du dashboard.
         */
        @GetMapping("/analytics/volume")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Volume par jour", description = "Retourne le volume total des transactions complétées par jour.")
        public ResponseEntity<ApiResponse<List<VolumeDataPoint>>> getVolumeByDay(
                        @RequestParam(defaultValue = "30") int days) {

                List<VolumeDataPoint> data = adminService.getVolumeByDay(days);
                return ResponseEntity.ok(
                                ApiResponse.success("Volume par jour", data));
        }

        /**
         * Taux de succès par jour pour les X derniers jours.
         * Utilisé pour le graphique en ligne du dashboard.
         */
        @GetMapping("/analytics/success-rate")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Taux de succès par jour", description = "Retourne le taux de succès des transactions par jour.")
        public ResponseEntity<ApiResponse<List<SuccessRateDataPoint>>> getSuccessRateByDay(
                        @RequestParam(defaultValue = "30") int days) {

                List<SuccessRateDataPoint> data = adminService.getSuccessRateByDay(days);
                return ResponseEntity.ok(
                                ApiResponse.success("Taux de succès par jour", data));
        }

        /**
         * Récupère les logs d'activité admin avec pagination et filtres.
         */
        @GetMapping("/logs")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Logs d'activité", description = "Retourne les logs d'activité des administrateurs avec filtres.")
        public ResponseEntity<ApiResponse<Page<AdminLogResponse>>> getAdminLogs(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "50") int size,
                        @RequestParam(required = false) String action,
                        @RequestParam(required = false) String adminEmail,
                        @RequestParam(required = false) LocalDate dateFrom,
                        @RequestParam(required = false) LocalDate dateTo) {

                Pageable pageable = PageRequest.of(page, size,
                                Sort.by("createdAt").descending());

                Page<AdminLogResponse> logs = adminAuditLogService.getFilteredLogs(
                                action, adminEmail, dateFrom, dateTo, pageable);
                return ResponseEntity.ok(
                                ApiResponse.success("Logs d'activité", logs));
        }

        // ── GESTION DES CANAUX DE PAIEMENT ────────────────────────────────────

        /**
         * Liste tous les canaux de paiement (actifs et inactifs).
         * Réservé à l'administration.
         *
         * GET /api/admin/payment-channels
         *
         * @return 200 OK avec la liste des canaux
         */
        @GetMapping("/payment-channels")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Liste des canaux de paiement", description = "Retourne tous les canaux de paiement (actifs et inactifs) pour la gestion admin.")
        public ResponseEntity<ApiResponse<List<PaymentChannelResponse>>> getAllPaymentChannels() {
                List<PaymentChannelResponse> channels = paymentChannelService.getAllChannels();
                return ResponseEntity.ok(
                                ApiResponse.success("Liste des canaux de paiement", channels));
        }

        /**
         * Ajoute un nouveau canal de paiement.
         *
         * POST /api/admin/payment-channels
         *
         * @param request les informations du nouveau canal
         * @return 200 OK avec le canal créé
         */
        @PostMapping("/payment-channels")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Ajouter un canal de paiement", description = "Ajoute un nouveau canal de paiement. Le type doit être unique.")
        public ResponseEntity<ApiResponse<PaymentChannelResponse>> addPaymentChannel(
                        @Valid @RequestBody PaymentChannelRequest request) {
                PaymentChannelResponse channel = paymentChannelService.addChannel(request);
                return ResponseEntity.ok(
                                ApiResponse.success("Canal de paiement ajouté : " + channel.getDisplayName(), channel));
        }

        /**
         * Active ou désactive un canal de paiement.
         *
         * PUT /api/admin/payment-channels/{id}/toggle
         *
         * @param id identifiant du canal
         * @return 200 OK avec le canal mis à jour
         */
        @PutMapping("/payment-channels/{id}/toggle")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Activer / Désactiver un canal", description = "Inverse le statut isActive d'un canal de paiement. "
                        +
                        "Un canal désactivé n'est plus visible par les utilisateurs.")
        public ResponseEntity<ApiResponse<PaymentChannelResponse>> togglePaymentChannel(
                        @PathVariable Long id) {
                PaymentChannelResponse channel = paymentChannelService.toggleChannel(id);
                String message = channel.isActive()
                                ? "Canal de paiement activé"
                                : "Canal de paiement désactivé";
                return ResponseEntity.ok(ApiResponse.success(message, channel));
        }
}
