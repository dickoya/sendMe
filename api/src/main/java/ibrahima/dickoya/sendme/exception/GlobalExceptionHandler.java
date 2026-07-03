package ibrahima.dickoya.sendme.exception;

import ibrahima.dickoya.sendme.dto.response.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

        // Email deja utilise — 409
        @ExceptionHandler(EmailAlreadyExistsException.class)
        public ResponseEntity<ApiResponse<Void>> handleEmailAlreadyExists(
                        EmailAlreadyExistsException ex) {
                log.warn("Email deja existant : {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ApiResponse.error(ex.getMessage()));
        }

        // Email non vérifié — 403
        @ExceptionHandler(EmailNotVerifiedException.class)
        public ResponseEntity<ApiResponse<Void>> handleEmailNotVerified(
                        EmailNotVerifiedException ex) {
                log.warn("Email non vérifié : {}", ex.getEmail());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(ApiResponse.error(ex.getMessage()));
        }

        // Mauvais identifiants — 401
        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ApiResponse<Void>> handleBadCredentials(
                        BadCredentialsException ex) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(ApiResponse.error("Email ou mot de passe incorrect"));
        }

        // Ressource non trouvee — 404
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(
                        ResourceNotFoundException ex) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.error(ex.getMessage()));
        }

        // Erreur de transfert — 400
        @ExceptionHandler(TransferException.class)
        public ResponseEntity<ApiResponse<Void>> handleTransferException(
                        TransferException ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(ex.getMessage()));
        }

        /**
         * Erreurs de validation des @RequestBody (DTOs) — 400
         * Déclenchée par @Valid sur les paramètres @RequestBody des contrôleurs.
         * Retourne la liste des champs invalides avec leurs messages d'erreur.
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationErrors(
                        MethodArgumentNotValidException ex) {
                Map<String, String> errors = new HashMap<>();
                ex.getBindingResult().getAllErrors().forEach(error -> {
                        String fieldName = ((FieldError) error).getField();
                        String message = error.getDefaultMessage();
                        errors.put(fieldName, message);
                });
                log.warn("Erreurs de validation DTO : {}", errors);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new ApiResponse<>(false, "Erreurs de validation", errors));
        }

        /**
         * Erreurs de validation des @RequestParam et @PathVariable — 400
         * Déclenchée par les annotations de validation (ex: @NotBlank String date)
         * sur les paramètres de requête ou les variables de chemin.
         */
        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolation(
                        ConstraintViolationException ex) {
                Map<String, String> errors = ex.getConstraintViolations().stream()
                                .collect(Collectors.toMap(
                                                cv -> cv.getPropertyPath().toString(),
                                                ConstraintViolation::getMessage,
                                                (msg1, msg2) -> msg1));
                log.warn("Erreurs de validation parametres : {}", errors);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new ApiResponse<>(false, "Erreurs de validation des parametres", errors));
        }

        /**
         * JSON malformé ou type de données incorrect — 400
         * Exemple : envoyer une chaîne là où un nombre est attendu,
         * ou un JSON syntaxiquement invalide.
         */
        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(
                        HttpMessageNotReadableException ex) {
                log.warn("JSON malforme : {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error("Format de requete invalide. Verifiez le JSON envoye."));
        }

        /**
         * Type de paramètre incompatible — 400
         * Exemple : envoyer une chaîne pour un @PathVariable Long
         */
        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatch(
                        MethodArgumentTypeMismatchException ex) {
                String message = "Le parametre '" + ex.getName() + "' est invalide. " +
                                "Valeur attendue : "
                                + (ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "inconnue");
                log.warn("Type de parametre invalide : {}", message);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(message));
        }

        // Refresh token invalide ou expiré — 400
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(
                        IllegalArgumentException ex) {
                log.warn("Requete invalide : {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(ex.getMessage()));
        }

        // Rate limit depasse — 429
        @ExceptionHandler(RateLimitExceededException.class)
        public ResponseEntity<ApiResponse<Void>> handleRateLimitExceeded(
                        RateLimitExceededException ex) {
                log.warn("Rate limit depasse : {}", ex.getMessage());
                return ResponseEntity.status(429)
                                .header("Retry-After", String.valueOf(ex.getRetryAfterSeconds()))
                                .body(ApiResponse.error(ex.getMessage()));
        }

        // Erreur generique — 500
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
                log.error("Erreur interne : ", ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponse.error("Une erreur interne est survenue"));
        }
}
