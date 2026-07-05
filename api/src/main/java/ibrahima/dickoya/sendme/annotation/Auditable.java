package ibrahima.dickoya.sendme.annotation;

import java.lang.annotation.*;

/**
 * Annotation marquant les méthodes admin qui doivent être tracées
 * dans les logs d'audit ({@code admin_audit_log}).
 * <p>
 * L'aspect {@code AuditAspect} intercepte les méthodes annotées
 * et enregistre automatiquement l'action avec le contexte
 * (admin connecté, IP, paramètres, résultat).
 * <p>
 * Utilisation :
 * 
 * <pre>{@code
 * &#64;Auditable(action = "USER_SUSPENDED", entityType = "USER")
 * public UserResponse toggleUserStatus(Long userId) { ... }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {

    /**
     * Type d'action (ex: USER_SUSPENDED, FEE_UPDATED, MAINTENANCE_SENT).
     */
    String action();

    /**
     * Type d'entité ciblée (ex: USER, CONFIG, NOTIFICATION).
     */
    String entityType();

    /**
     * Expression SpEL pour extraire l'identifiant de l'entité ciblée
     * depuis les paramètres de la méthode.
     * <p>
     * Exemple : {@code #userId} pour extraire le paramètre {@code Long userId}.
     * Si vide, l'ID est extrait automatiquement depuis le retour de la méthode.
     */
    String entityIdExpression() default "";
}
