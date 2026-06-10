package ibrahima.dickoya.sendme.dto.response;

import ibrahima.dickoya.sendme.enums.Role;
import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO de réponse pour un utilisateur.
 * Utilisé par l'administrateur pour consulter la liste des utilisateurs.
 *
 * Ne contient jamais le mot de passe (passwordHash).
 * Contient les informations nécessaires pour la gestion admin.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserResponse {

    /** Identifiant unique de l'utilisateur. */
    private Long id;

    /** Prénom de l'utilisateur. */
    private String firstName;

    /** Nom de l'utilisateur. */
    private String lastName;

    /** Email de connexion — identifiant unique. */
    private String email;

    /** Numéro de téléphone (optionnel). */
    private String phoneNumber;

    /**
     * Rôle de l'utilisateur.
     * ROLE_USER ou ROLE_ADMIN.
     */
    private Role role;

    /**
     * Statut du compte.
     * true = compte actif, false = compte désactivé par l'admin.
     */
    private Boolean isActive;

    /** Date d'inscription de l'utilisateur. */
    private LocalDateTime createdAt;

    /**
     * Nombre total de transactions effectuées.
     * Calculé dynamiquement par le service.
     */
    private Long totalTransactions;

    /**
     * Montant total envoyé par cet utilisateur.
     * Calculé dynamiquement par le service.
     */
    private Double totalAmountSent;
}