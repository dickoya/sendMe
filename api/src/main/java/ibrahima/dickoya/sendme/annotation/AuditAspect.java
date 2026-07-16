package ibrahima.dickoya.sendme.annotation;

import com.fasterxml.jackson.databind.ObjectMapper;
import ibrahima.dickoya.sendme.entity.AdminAuditLog;
import ibrahima.dickoya.sendme.entity.User;
import ibrahima.dickoya.sendme.repository.AdminAuditLogRepository;
import ibrahima.dickoya.sendme.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

/**
 * Aspect AOP qui intercepte les méthodes annotées {@code @Auditable}
 * et enregistre un log d'audit dans {@code admin_audit_log}.
 * <p>
 * Fonctionnement :
 * <ol>
 * <li>Avant l'exécution : capture les paramètres de la méthode</li>
 * <li>Exécute la méthode</li>
 * <li>Après l'exécution : enregistre le log avec le résultat</li>
 * </ol>
 */
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    private final AdminAuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        // Exécuter la méthode
        Object result = joinPoint.proceed();

        try {
            // Récupérer l'admin connecté
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return result;
            }

            // Récupérer l'IP depuis la requête HTTP
            String ipAddress = null;
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                ipAddress = request.getRemoteAddr();
            }

            // Extraire l'ID de l'entité
            Long entityId = extractEntityId(joinPoint, auditable, result);

            // Construire les détails JSON
            Map<String, Object> details = new HashMap<>();
            details.put("method", joinPoint.getSignature().getName());
            if (result != null && result instanceof Map) {
                details.put("result", result);
            }

            // Récupérer les infos de l'admin
            Object principal = auth.getPrincipal();
            Long adminId = null;
            String adminEmail = "unknown";

            if (principal instanceof ibrahima.dickoya.sendme.entity.User user) {
                adminId = user.getId();
                adminEmail = user.getEmail();
            } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
                adminEmail = userDetails.getUsername();
                adminId = userRepository.findByEmail(adminEmail)
                        .map(User::getId)
                        .orElse(null);
            }

            if (adminId == null) {
                log.warn("[AUDIT] Impossible de déterminer l'ID admin pour {}", adminEmail);
                return result;
            }

            // Créer et sauvegarder le log
            AdminAuditLog auditLog = AdminAuditLog.builder()
                    .adminId(adminId)
                    .adminEmail(adminEmail)
                    .action(auditable.action())
                    .entityType(auditable.entityType())
                    .entityId(entityId)
                    .details(objectMapper.writeValueAsString(details))
                    .ipAddress(ipAddress)
                    .build();

            auditLogRepository.save(auditLog);

            log.debug("[AUDIT] {} | {} | {} | entityId={} | IP={}",
                    adminEmail, auditable.action(), auditable.entityType(), entityId, ipAddress);

        } catch (Exception e) {
            // Ne jamais bloquer l'exécution principale à cause d'un problème d'audit
            log.warn("[AUDIT] Erreur lors de l'enregistrement du log d'audit", e);
        }

        return result;
    }

    /**
     * Extrait l'ID de l'entité ciblée.
     * Priorité :
     * 1. Expression SpEL si fournie
     * 2. Retour de la méthode s'il a une méthode getId()
     * 3. null
     */
    private Long extractEntityId(ProceedingJoinPoint joinPoint, Auditable auditable, Object result) {
        // 1. Expression SpEL
        if (!auditable.entityIdExpression().isEmpty()) {
            try {
                MethodSignature signature = (MethodSignature) joinPoint.getSignature();
                String[] paramNames = signature.getParameterNames();
                Object[] args = joinPoint.getArgs();

                StandardEvaluationContext context = new StandardEvaluationContext();
                if (paramNames != null) {
                    for (int i = 0; i < paramNames.length; i++) {
                        context.setVariable(paramNames[i], args[i]);
                    }
                }
                return parser.parseExpression(auditable.entityIdExpression()).getValue(context, Long.class);
            } catch (Exception e) {
                log.debug("[AUDIT] Impossible d'évaluer l'expression SpEL: {}", auditable.entityIdExpression());
            }
        }

        // 2. Depuis le retour de la méthode
        if (result != null) {
            try {
                // Essaye d'appeler getId() via réflexion
                var method = result.getClass().getMethod("getId");
                Object id = method.invoke(result);
                if (id instanceof Number number) {
                    return number.longValue();
                }
            } catch (NoSuchMethodException ignored) {
                // Pas de méthode getId() — ce n'est pas une entité
            } catch (Exception e) {
                log.debug("[AUDIT] Impossible d'extraire l'ID depuis le résultat", e);
            }
        }

        return null;
    }
}
