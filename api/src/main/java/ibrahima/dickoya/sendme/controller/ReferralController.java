package ibrahima.dickoya.sendme.controller;

import ibrahima.dickoya.sendme.dto.response.ApiResponse;
import ibrahima.dickoya.sendme.dto.response.ReferralCodeResponse;
import ibrahima.dickoya.sendme.dto.response.ReferralCouponResponse;
import ibrahima.dickoya.sendme.entity.User;
import ibrahima.dickoya.sendme.repository.UserRepository;
import ibrahima.dickoya.sendme.service.ReferralService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Contrôleur REST pour le système de parrainage SendMe.
 * Permet aux utilisateurs de gérer leur code de parrainage
 * et de consulter leurs coupons de réduction.
 */
@RestController
@RequestMapping("/api/referral")
@RequiredArgsConstructor
@Tag(name = "Parrainage", description = "Gestion du système de parrainage et des coupons de réduction")
public class ReferralController {

        private final ReferralService referralService;
        private final UserRepository userRepository;

        /**
         * Récupère les informations de parrainage de l'utilisateur connecté.
         */
        @GetMapping("/my-code")
        @Operation(summary = "Mon code de parrainage", description = "Retourne le code de parrainage de l'utilisateur connecté, "
                        +
                        "le lien de partage, et les statistiques (nombre de filleuls, coupons).", security = @SecurityRequirement(name = "bearerAuth"))
        public ResponseEntity<ApiResponse<ReferralCodeResponse>> getMyReferralCode(
                        @AuthenticationPrincipal UserDetails userDetails) {
                User user = userRepository.findByEmail(userDetails.getUsername())
                                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

                ReferralCodeResponse response = referralService.getReferralInfo(user);
                return ResponseEntity.ok(ApiResponse.success("Code de parrainage récupéré", response));
        }

        /**
         * Récupère la liste des coupons de réduction disponibles.
         */
        @GetMapping("/coupons")
        @Operation(summary = "Mes coupons de réduction", description = "Retourne la liste des coupons de réduction valides "
                        +
                        "pour l'utilisateur connecté.", security = @SecurityRequirement(name = "bearerAuth"))
        public ResponseEntity<ApiResponse<List<ReferralCouponResponse>>> getMyCoupons(
                        @AuthenticationPrincipal UserDetails userDetails) {
                User user = userRepository.findByEmail(userDetails.getUsername())
                                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

                List<ReferralCouponResponse> coupons = referralService.getUserCoupons(user.getId());
                return ResponseEntity.ok(ApiResponse.success("Coupons récupérés", coupons));
        }

        /**
         * Vérifie si l'utilisateur a un coupon valide pour une devise donnée
         * et retourne le montant de la réduction.
         */
        @GetMapping("/discount")
        @Operation(summary = "Montant de réduction disponible", description = "Vérifie si l'utilisateur a un coupon valide pour la devise spécifiée "
                        +
                        "et retourne le montant de la réduction (0 si aucun coupon).", security = @SecurityRequirement(name = "bearerAuth"))
        public ResponseEntity<ApiResponse<BigDecimal>> getDiscount(
                        @AuthenticationPrincipal UserDetails userDetails,
                        @RequestParam(defaultValue = "EUR") String currency) {
                User user = userRepository.findByEmail(userDetails.getUsername())
                                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

                BigDecimal discount = referralService.getDiscountAmount(user.getId(), currency);
                return ResponseEntity.ok(ApiResponse.success("Réduction disponible", discount));
        }

        /**
         * Vérifie si un code de parrainage est valide (existe et actif).
         * Endpoint public (sans authentification) pour valider le code
         * côté frontend avant la soumission du formulaire d'inscription.
         */
        @GetMapping("/verify")
        @Operation(summary = "Vérifier un code de parrainage", description = "Vérifie si un code de parrainage existe et est actif. "
                        +
                        "Utilisé par le formulaire d'inscription pour valider le code en temps réel.")
        public ResponseEntity<ApiResponse<Boolean>> verifyReferralCode(
                        @RequestParam String code) {
                boolean isValid = referralService.isValidReferralCode(code);
                return ResponseEntity.ok(ApiResponse.success(
                                isValid ? "Code valide" : "Code invalide", isValid));
        }
}
