package ibrahima.dickoya.sendme.exception;

import ibrahima.dickoya.sendme.dto.response.ApiResponse;
import ibrahima.dickoya.sendme.utils.MessageUtils;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

        private final MessageUtils messageUtils;

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

        // Invalid credentials — 401
        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ApiResponse<Void>> handleBadCredentials(
                        BadCredentialsException ex) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(ApiResponse.error(messageUtils.get("auth.credentials.invalid")));
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
         * Validation errors for @RequestBody (DTOs) — 400
         * Triggered by @Valid on @RequestBody parameters in controllers.
         * Returns the list of invalid fields with their error messages.
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
                log.warn("DTO validation errors : {}", errors);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new ApiResponse<>(false, messageUtils.get("error.validation.failed"), errors));
        }

        /**
         * Validation errors for @RequestParam and @PathVariable — 400
         * Triggered by validation annotations (e.g. @NotBlank String date)
         * on request parameters or path variables.
         */
        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolation(
                        ConstraintViolationException ex) {
                Map<String, String> errors = ex.getConstraintViolations().stream()
                                .collect(Collectors.toMap(
                                                cv -> cv.getPropertyPath().toString(),
                                                ConstraintViolation::getMessage,
                                                (msg1, msg2) -> msg1));
                log.warn("Parameter validation errors : {}", errors);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new ApiResponse<>(false, messageUtils.get("error.validation.parameters"),
                                                errors));
        }

        /**
         * Malformed JSON or incorrect data type — 400
         * Example: sending a string where a number is expected,
         * or syntactically invalid JSON.
         */
        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(
                        HttpMessageNotReadableException ex) {
                log.warn("Malformed JSON : {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(messageUtils.get("error.request.invalid.format")));
        }

        /**
         * Incompatible parameter type — 400
         * Example: sending a string for a @PathVariable Long
         */
        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatch(
                        MethodArgumentTypeMismatchException ex) {
                String message = messageUtils.get("error.parameter.invalid",
                                ex.getName(),
                                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "?");
                log.warn("Invalid parameter type : {}", message);
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

        // Timeout des connexions asynchrones (SSE) — comportement NORMAL.
        // Les connexions SSE expirent régulièrement (délai async dépassé).
        // On ignore silencieusement pour éviter le flood de logs et la tentative
        // d'écrire un ApiResponse en 'text/event-stream' (HttpMessageNotWritableException).
        @ExceptionHandler(AsyncRequestTimeoutException.class)
        public void handleAsyncRequestTimeout(AsyncRequestTimeoutException ex) {
                log.debug("Timeout requête asynchrone (SSE) ignoré");
        }

        // Generic error — 500
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
                log.error("Internal error : ", ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponse.error(messageUtils.get("general.error")));
        }
}
