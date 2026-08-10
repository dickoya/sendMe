package ibrahima.dickoya.sendme.service;

import ibrahima.dickoya.sendme.dto.request.FeeConfigRequest;
import ibrahima.dickoya.sendme.dto.response.DashboardResponse;
import ibrahima.dickoya.sendme.dto.response.SuccessRateDataPoint;
import ibrahima.dickoya.sendme.dto.response.TransactionResponse;
import ibrahima.dickoya.sendme.dto.response.UserResponse;
import ibrahima.dickoya.sendme.dto.response.VolumeDataPoint;
import ibrahima.dickoya.sendme.entity.Transaction;
import ibrahima.dickoya.sendme.entity.User;
import ibrahima.dickoya.sendme.enums.Role;
import ibrahima.dickoya.sendme.enums.TransactionStatus;
import ibrahima.dickoya.sendme.exception.ResourceNotFoundException;
import ibrahima.dickoya.sendme.repository.TransactionRepository;
import ibrahima.dickoya.sendme.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service gérant toute la logique métier du module administration.
 *
 * Fonctionnalités :
 * - Tableau de bord avec statistiques globales en temps réel
 * - Gestion des utilisateurs (liste, activation, désactivation)
 * - Consultation de toutes les transactions
 * - Paramétrage des frais de la plateforme
 *
 * Toutes les méthodes de ce service sont protégées par le rôle ROLE_ADMIN
 * configuré dans SecurityConfig via @PreAuthorize.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

        /** Repository pour accéder aux utilisateurs. */
        private final UserRepository userRepository;

        /** Repository pour accéder aux transactions. */
        private final TransactionRepository transactionRepository;

        /**
         * Service SSE pour connaître le nombre de connexions actives.
         * Affiché dans le tableau de bord admin pour le monitoring.
         */
        private final SseEmitterService sseEmitterService;

        /**
         * Service simulateur pour mettre à jour le taux de commission.
         * L'admin peut changer le taux sans redémarrer l'application.
         */
        private final SimulatorService simulatorService;

        /**
         * Service email pour notifier l'utilisateur
         * quand son compte est activé ou désactivé.
         */
        private final EmailService emailService;
        private final AuditService auditService;

        /**
         * Génère le tableau de bord complet avec toutes les statistiques.
         *
         * Calcule en temps réel :
         * - Les statistiques des utilisateurs
         * - Les statistiques des transactions par statut
         * - Le volume financier total et quotidien
         * - La répartition par canal
         * - Le taux de succès global
         *
         * @return DashboardResponse toutes les statistiques de la plateforme
         */
        public DashboardResponse getDashboard() {

                log.info("[ADMIN] Génération du tableau de bord");

                // Statistiques utilisateurs
                long totalUsers = userRepository.count();
                long activeUsers = userRepository.countByIsActiveTrue();
                long inactiveUsers = totalUsers - activeUsers;

                // Statistiques transactions
                long totalTransactions = transactionRepository.count();
                long completedTransactions = transactionRepository
                                .countByStatus(TransactionStatus.COMPLETED);
                long failedTransactions = transactionRepository
                                .countByStatus(TransactionStatus.FAILED);
                long refundedTransactions = transactionRepository
                                .countByStatus(TransactionStatus.REFUNDED);
                long pendingTransactions = transactionRepository
                                .countByStatus(TransactionStatus.PENDING) +
                                transactionRepository.countByStatus(TransactionStatus.PROCESSING);

                // Calcul du taux de succès
                double successRate = totalTransactions > 0
                                ? (double) completedTransactions / totalTransactions * 100
                                : 0.0;

                // Statistiques financières
                // Volume total = somme des montants envoyés des transactions COMPLETED
                Double totalVolume = transactionRepository
                                .sumAmountSentByStatus(TransactionStatus.COMPLETED);
                totalVolume = totalVolume != null ? totalVolume : 0.0;

                // Total des frais collectés
                Double totalFees = transactionRepository
                                .sumFeesByStatus(TransactionStatus.COMPLETED);
                totalFees = totalFees != null ? totalFees : 0.0;

                // Volume du jour
                LocalDateTime startOfDay = LocalDateTime.now().withHour(0)
                                .withMinute(0).withSecond(0);
                Double todayVolume = transactionRepository
                                .sumAmountSentAfterDate(startOfDay, TransactionStatus.COMPLETED);
                todayVolume = todayVolume != null ? todayVolume : 0.0;

                long todayTransactions = transactionRepository
                                .countByCreatedAtAfter(startOfDay);

                // Répartition par canal
                Map<String, Long> transactionsByChannel = new HashMap<>();
                transactionRepository.countByChannel().forEach(result -> {
                        transactionsByChannel.put(
                                        result[0].toString(), // nom du canal
                                        (Long) result[1] // nombre de transactions
                        );
                });

                // Connexions SSE actives
                int activeSSEConnections = sseEmitterService.getActiveConnectionsCount();

                // Construire et retourner la réponse
                return DashboardResponse.builder()
                                .totalUsers(totalUsers)
                                .activeUsers(activeUsers)
                                .inactiveUsers(inactiveUsers)
                                .totalTransactions(totalTransactions)
                                .completedTransactions(completedTransactions)
                                .failedTransactions(failedTransactions)
                                .refundedTransactions(refundedTransactions)
                                .pendingTransactions(pendingTransactions)
                                .successRate(Math.round(successRate * 100.0) / 100.0)
                                .totalVolume(totalVolume)
                                .totalFees(totalFees)
                                .todayVolume(todayVolume)
                                .todayTransactions(todayTransactions)
                                .transactionsByChannel(transactionsByChannel)
                                .activeSSEConnections(activeSSEConnections)
                                .build();
        }

        /**
         * Récupère la liste paginée de tous les utilisateurs.
         * Accessible uniquement par l'administrateur.
         *
         * @param pageable paramètres de pagination
         * @return Page de UserResponse
         */
        public Page<UserResponse> getAllUsers(Pageable pageable) {
                return userRepository.findAll(pageable)
                                .map(this::toUserResponse);
        }

        /**
         * Récupère le détail d'un utilisateur par son identifiant.
         *
         * @param userId identifiant de l'utilisateur
         * @return UserResponse le détail de l'utilisateur
         */
        public UserResponse getUserById(Long userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Utilisateur non trouvé : " + userId));
                return toUserResponse(user);
        }

        /**
         * Active ou désactive un compte utilisateur.
         *
         * Un compte désactivé ne peut plus se connecter.
         * Spring Security vérifie isActive via UserDetailsService.
         * L'admin peut réactiver un compte à tout moment.
         *
         * @param userId identifiant de l'utilisateur à modifier
         * @return UserResponse l'utilisateur mis à jour
         */
        @Transactional
        public UserResponse toggleUserStatus(Long userId) {

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Utilisateur non trouvé : " + userId));

                // Inverser le statut actuel
                boolean newStatus = !user.getIsActive();
                user.setIsActive(newStatus);
                // Sauvegarder le nouveau statut
                userRepository.save(user);

                // Notifier l'utilisateur par email selon le nouveau statut
                if (newStatus) {
                        // Compte réactivé → email de bonne nouvelle
                        emailService.sendAccountReactivatedEmail(user);
                        log.info("[ADMIN] Email réactivation envoyé à : {}", user.getEmail());
                } else {
                        // Compte suspendu → email d'avertissement
                        emailService.sendAccountDisabledEmail(user);
                        log.info("[ADMIN] Email suspension envoyé à : {}", user.getEmail());
                }

                log.info("[ADMIN] Compte {} : statut changé à {}",
                                user.getEmail(), newStatus ? "ACTIF" : "DESACTIVE");

                // Enregistrer un log d'audit
                auditService.logEvent("ADMIN", "USER_TOGGLE", user.getEmail(), user.getId(),
                                "SUCCESS",
                                "Compte " + (newStatus ? "réactivé" : "désactivé") + " : " + user.getEmail());

                return toUserResponse(user);
        }

        /**
         * Modifie le rôle d'un utilisateur (ROLE_USER / ROLE_ADMIN).
         *
         * Sécurités appliquées :
         * - Impossible de retirer le rôle admin au dernier administrateur de la plateforme.
         *
         * @param userId  identifiant de l'utilisateur à modifier
         * @param newRole nouveau rôle à attribuer
         * @return UserResponse l'utilisateur mis à jour
         */
        @Transactional
        public UserResponse updateUserRole(Long userId, Role newRole) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Utilisateur non trouvé : " + userId));

                if (user.getRole() == newRole) {
                        return toUserResponse(user);
                }

                // Empêcher de retirer le rôle admin au dernier administrateur
                if (user.getRole() == Role.ROLE_ADMIN && newRole != Role.ROLE_ADMIN
                                && userRepository.countByRole(Role.ROLE_ADMIN) <= 1) {
                        throw new IllegalArgumentException(
                                        "Impossible de retirer le rôle admin : c'est le dernier administrateur de la plateforme.");
                }

                user.setRole(newRole);
                userRepository.save(user);

                log.info("[ADMIN] Rôle de {} (id={}) modifié vers {}", user.getEmail(), user.getId(), newRole);

                auditService.logEvent("ADMIN", "ROLE_UPDATE", user.getEmail(), user.getId(),
                                "SUCCESS",
                                "Rôle modifié vers " + newRole + " : " + user.getEmail());

                return toUserResponse(user);
        }

        /**
         * Récupère toutes les transactions avec pagination.
         * L'admin voit toutes les transactions de tous les utilisateurs.
         *
         * @param pageable paramètres de pagination
         * @return Page de TransactionResponse
         */
        public Page<TransactionResponse> getAllTransactions(Pageable pageable) {
                return transactionRepository
                                .findAllByOrderByCreatedAtDesc(pageable)
                                .map(this::toTransactionResponse);
        }

        /**
         * Récupère les transactions filtrées par statut.
         * Permet à l'admin de voir uniquement les transactions en échec,
         * en cours, ou complétées selon son besoin.
         *
         * @param status   statut à filtrer
         * @param pageable paramètres de pagination
         * @return Page de TransactionResponse filtrée
         */
        public Page<TransactionResponse> getTransactionsByStatus(
                        TransactionStatus status, Pageable pageable) {
                return transactionRepository
                                .findByStatusOrderByCreatedAtDesc(status, pageable)
                                .map(this::toTransactionResponse);
        }

        /**
         * Met à jour le taux de commission de la plateforme
         * et notifie tous les utilisateurs actifs par email.
         *
         * Le nouveau taux est appliqué immédiatement à tous
         * les nouveaux transferts. Les utilisateurs reçoivent
         * un email les informant du changement.
         *
         * @param request le nouveau taux et la raison du changement
         */
        public void updateFeeRate(FeeConfigRequest request) {

                // Récupérer l'ancien taux AVANT la mise à jour
                java.math.BigDecimal oldRate = simulatorService.getCurrentFeeRate()
                                .multiply(new java.math.BigDecimal("100")); // convertir en %

                // Appliquer le nouveau taux
                simulatorService.updateFeeRate(request.getFeeRate());

                log.info("[ADMIN] Taux de commission mis a jour : {}% → {}% — Raison : {}",
                                oldRate, request.getFeeRate(), request.getReason());

                // Date d'application = demain
                String effectiveDate = java.time.LocalDate.now()
                                .plusDays(1)
                                .format(java.time.format.DateTimeFormatter
                                                .ofPattern("dd MMMM yyyy"));

                // Envoyer la notification à tous les utilisateurs actifs
                sendFeeUpdateNotification(oldRate, request.getFeeRate(), effectiveDate);
        }

        /**
         * Convertit une entité User en DTO UserResponse.
         * Calcule aussi les statistiques de l'utilisateur.
         *
         * @param user l'entité à convertir
         * @return UserResponse le DTO formaté
         */
        private UserResponse toUserResponse(User user) {

                // Calculer le nombre de transactions de cet utilisateur
                long totalTransactions = transactionRepository
                                .countBySenderId(user.getId());

                // Calculer le montant total envoyé
                Double totalAmountSent = transactionRepository
                                .sumAmountSentBySenderIdAndStatus(
                                                user.getId(), TransactionStatus.COMPLETED);

                return UserResponse.builder()
                                .id(user.getId())
                                .firstName(user.getFirstName())
                                .lastName(user.getLastName())
                                .email(user.getEmail())
                                .phoneNumber(user.getPhoneNumber())
                                .role(user.getRole())
                                .isActive(user.getIsActive())
                                .createdAt(user.getCreatedAt())
                                .totalTransactions(totalTransactions)
                                .totalAmountSent(totalAmountSent != null ? totalAmountSent : 0.0)
                                .build();
        }

        /**
         * Convertit une entité Transaction en DTO TransactionResponse.
         *
         * @param transaction l'entité à convertir
         * @return TransactionResponse le DTO formaté
         */
        private TransactionResponse toTransactionResponse(Transaction transaction) {
                return TransactionResponse.builder()
                                .id(transaction.getId())
                                .reference(transaction.getReference())
                                .amountSent(transaction.getAmountSent())
                                .currencySent(transaction.getCurrencySent())
                                .amountReceived(transaction.getAmountReceived())
                                .currencyReceived(transaction.getCurrencyReceived())
                                .exchangeRate(transaction.getExchangeRate())
                                .fees(transaction.getFees())
                                .status(transaction.getStatus())
                                .channel(transaction.getChannel())
                                .stripePaymentIntentId(transaction.getStripePaymentIntentId())
                                .stripePaymentStatus(transaction.getStripePaymentStatus())
                                .createdAt(transaction.getCreatedAt())
                                .updatedAt(transaction.getUpdatedAt())
                                .build();
        }

        /**
         * Envoie une notification de maintenance programmée
         * à tous les utilisateurs ayant un compte actif.
         *
         * Appelé manuellement par l'administrateur via
         * POST /api/admin/notifications/maintenance
         * quand une maintenance technique est planifiée.
         *
         * Chaque email est envoyé de manière asynchrone (@Async)
         * pour ne pas bloquer la réponse HTTP même avec
         * un grand nombre d'utilisateurs.
         *
         * @param maintenanceDate date et heure de la maintenance (ex: "15 juin 2025 à
         *                        02:00")
         * @param duration        durée estimée (ex: "2 heures")
         */
        @Transactional(readOnly = true)
        public void sendMaintenanceNotification(
                        String maintenanceDate,
                        String duration) {

                // Récupérer uniquement les comptes actifs
                List<User> activeUsers = userRepository.findByIsActiveTrue();

                log.info("[ADMIN] Envoi notification maintenance à {} utilisateurs actifs",
                                activeUsers.size());

                // Envoyer l'email à chaque utilisateur actif
                activeUsers.forEach(user -> emailService.sendMaintenanceEmail(
                                user, maintenanceDate, duration));

                log.info("[ADMIN] Notifications maintenance envoyées avec succès");
        }

        /**
         * Envoie une notification de changement de tarif
         * à tous les utilisateurs ayant un compte actif.
         *
         * Appelé automatiquement par updateFeeRate() après
         * la mise à jour du taux de commission de la plateforme.
         *
         * @param oldRate       ancien taux de commission en pourcentage
         * @param newRate       nouveau taux de commission en pourcentage
         * @param effectiveDate date d'application du nouveau taux
         */
        @Transactional(readOnly = true)
        public void sendFeeUpdateNotification(
                        java.math.BigDecimal oldRate,
                        java.math.BigDecimal newRate,
                        String effectiveDate) {

                List<User> activeUsers = userRepository.findByIsActiveTrue();

                log.info("[ADMIN] Envoi notification changement tarifs à {} utilisateurs actifs",
                                activeUsers.size());

                activeUsers.forEach(user -> emailService.sendFeeUpdateEmail(
                                user, oldRate, newRate, effectiveDate));

                log.info("[ADMIN] Notifications changement tarifs envoyées avec succès");
        }

        // NOUVELLES MÉTHODES : Filtres, Batch, Volume, Success Rate

        /**
         * Récupère les utilisateurs avec filtres avancés.
         * Supporte la recherche textuelle, le filtre par statut/rôle,
         * la période d'inscription, et le tri.
         */
        public Page<UserResponse> getFilteredUsers(
                        String search,
                        Boolean isActive,
                        Role role,
                        LocalDate dateFrom,
                        LocalDate dateTo,
                        Pageable pageable) {

                LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay() : null;
                LocalDateTime to = dateTo != null ? dateTo.atTime(LocalTime.MAX) : null;

                return userRepository.findFiltered(search, isActive, role, from, to, pageable)
                                .map(this::toUserResponse);
        }

        /**
         * Active ou désactive plusieurs comptes utilisateurs en une seule opération.
         * Chaque compte est traité individuellement avec notification email.
         *
         * @param userIds liste des identifiants à modifier
         * @param suspend true pour suspendre, false pour réactiver
         * @return nombre de comptes modifiés avec succès
         */
        @Transactional
        public int batchToggleUsers(List<Long> userIds, boolean suspend) {
                int count = 0;
                for (Long userId : userIds) {
                        try {
                                User user = userRepository.findById(userId).orElse(null);
                                if (user == null)
                                        continue;

                                user.setIsActive(!suspend);
                                userRepository.save(user);

                                if (suspend) {
                                        emailService.sendAccountDisabledEmail(user);
                                } else {
                                        emailService.sendAccountReactivatedEmail(user);
                                }
                                count++;
                                log.info("[ADMIN] Batch {} : compte {} -> {}",
                                                suspend ? "suspension" : "réactivation",
                                                user.getEmail(), suspend ? "DESACTIVE" : "ACTIF");
                        } catch (Exception e) {
                                log.warn("[ADMIN] Erreur batch pour userId={}: {}", userId, e.getMessage());
                        }
                }
                return count;
        }

        /**
         * Volume total par jour pour les X derniers jours.
         * Utilisé pour le graphique en barres du dashboard.
         */
        public List<VolumeDataPoint> getVolumeByDay(int days) {
                LocalDateTime since = LocalDateTime.now().minusDays(days);
                List<Object[]> raw = transactionRepository.getVolumeByDay(since);

                return raw.stream().map(row -> VolumeDataPoint.builder()
                                .date(toLocalDate(row[0]))
                                .volume(toDouble(row[1]))
                                .transactionCount(toLong(row[2]))
                                .build())
                                .collect(Collectors.toList());
        }

        /**
         * Taux de succès par jour pour les X derniers jours.
         * Utilisé pour le graphique en ligne du dashboard.
         */
        public List<SuccessRateDataPoint> getSuccessRateByDay(int days) {
                LocalDateTime since = LocalDateTime.now().minusDays(days);
                List<Object[]> raw = transactionRepository.getSuccessRateByDay(since);

                return raw.stream().map(row -> {
                        Long total = toLong(row[1]);
                        Long completed = toLong(row[2]);
                        Long failed = toLong(row[3]);
                        double rate = total > 0 ? (double) completed / total * 100 : 0.0;

                        return SuccessRateDataPoint.builder()
                                        .date(toLocalDate(row[0]))
                                        .successRate(Math.round(rate * 100.0) / 100.0)
                                        .total(total)
                                        .completed(completed)
                                        .failed(failed)
                                        .build();
                }).collect(Collectors.toList());
        }

        private LocalDate toLocalDate(Object value) {
                if (value == null) {
                        return null;
                }
                if (value instanceof java.sql.Date sqlDate) {
                        return sqlDate.toLocalDate();
                }
                if (value instanceof java.sql.Timestamp timestamp) {
                        return timestamp.toLocalDateTime().toLocalDate();
                }
                if (value instanceof LocalDate localDate) {
                        return localDate;
                }
                if (value instanceof LocalDateTime localDateTime) {
                        return localDateTime.toLocalDate();
                }
                throw new IllegalArgumentException("Type de date inattendu: " + value.getClass());
        }

        private Double toDouble(Object value) {
                if (value == null) {
                        return 0.0;
                }
                if (value instanceof Number number) {
                        return number.doubleValue();
                }
                return Double.valueOf(value.toString());
        }

        private Long toLong(Object value) {
                if (value == null) {
                        return 0L;
                }
                if (value instanceof Number number) {
                        return number.longValue();
                }
                return Long.valueOf(value.toString());
        }
}
