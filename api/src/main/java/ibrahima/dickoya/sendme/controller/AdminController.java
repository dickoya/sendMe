package ibrahima.dickoya.sendme.controller;

import ibrahima.dickoya.sendme.dto.request.FeeConfigRequest;
import ibrahima.dickoya.sendme.dto.response.*;
import ibrahima.dickoya.sendme.enums.TransactionStatus;
import ibrahima.dickoya.sendme.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
 * GET  /api/admin/dashboard              → statistiques globales
 * GET  /api/admin/users                  → liste paginée des utilisateurs
 * GET  /api/admin/users/{id}             → détail d'un utilisateur
 * PUT  /api/admin/users/{id}/toggle      → activer/désactiver un compte
 * GET  /api/admin/transactions           → toutes les transactions
 * GET  /api/admin/transactions/status    → transactions par statut
 * PUT  /api/admin/config/fees            → modifier le taux de commission
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    /** Service contenant toute la logique métier admin. */
    private final AdminService adminService;

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
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {

        DashboardResponse dashboard = adminService.getDashboard();
        return ResponseEntity.ok(
                ApiResponse.success("Tableau de bord administrateur", dashboard));
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
     * @param page  numéro de page
     * @param size  taille de la page
     * @return 200 OK avec la page d'utilisateurs
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
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
     * Si le compte est actif → il est désactivé (l'utilisateur ne peut plus se connecter).
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
    public ResponseEntity<ApiResponse<Void>> updateFeeRate(
            @Valid @RequestBody FeeConfigRequest request) {

        adminService.updateFeeRate(request);
        return ResponseEntity.ok(ApiResponse.success(
                "Taux de commission mis a jour : " + request.getFeeRate() + "%",
                null));
    }
}