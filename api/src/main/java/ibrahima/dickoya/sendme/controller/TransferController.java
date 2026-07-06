package ibrahima.dickoya.sendme.controller;

import ibrahima.dickoya.sendme.dto.request.*;
import ibrahima.dickoya.sendme.dto.response.*;
import ibrahima.dickoya.sendme.entity.Transaction;
import ibrahima.dickoya.sendme.entity.User;
import ibrahima.dickoya.sendme.repository.UserRepository;
import ibrahima.dickoya.sendme.security.JwtService;
import ibrahima.dickoya.sendme.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour les transferts et l'historique des transactions.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Transferts & Transactions", description = """
                Gestion complète des transferts d'argent et de l'historique.

                **Flux de transfert :**
                1. Simuler (`POST /api/transfers/simulate`) — voir les frais avant de payer
                2. Transférer (`POST /api/transfers`) — débiter la carte et envoyer
                3. Consulter (`GET /api/transactions`) — voir l'historique
                4. Télécharger le reçu (`GET /api/transactions/{id}/receipt`)

                JWT requis pour tous les endpoints.
                """)
public class TransferController {

        private final SimulatorService simulatorService;
        private final TransferService transferService;
        private final ReceiptPdfService receiptPdfService;
        private final UserRepository userRepository;
        private final JwtService jwtService;
        private final PaymentChannelService paymentChannelService;

        @PostMapping("/api/transfers/simulate")
        @Operation(summary = "Simuler les frais d'un transfert", description = """
                        Calcule et retourne le récapitulatif complet avant confirmation :
                        - Frais de la plateforme (1.5%)
                        - Taux de change appliqué
                        - Montant exact reçu par le bénéficiaire
                        - Total débité sur la carte
                        - Réduction de parrainage si applicable

                        **Appelé en temps réel** à chaque modification du montant côté frontend.
                        Ne crée aucune transaction, aucun débit.

                        Si l'utilisateur est connecté (JWT), la réduction de parrainage
                        est automatiquement appliquée si un coupon valide existe.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Simulation calculée avec succès"),
                        @ApiResponse(responseCode = "400", description = "Données invalides")
        })
        public ResponseEntity<ibrahima.dickoya.sendme.dto.response.ApiResponse<SimulateResponse>> simulate(
                        @Valid @RequestBody SimulateRequest request,
                        HttpServletRequest httpRequest) {
                SimulateResponse response;

                // Extraire le JWT manuellement depuis le header Authorization
                // (l'endpoint est public, donc @AuthenticationPrincipal ne fonctionne pas)
                String authHeader = httpRequest.getHeader("Authorization");
                String userEmail = null;
                if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                        try {
                                String token = authHeader.substring(7);
                                userEmail = jwtService.extractUsername(token);
                        } catch (Exception e) {
                                // Token invalide ou expiré → ignorer
                        }
                }

                if (userEmail != null) {
                        // Utilisateur connecté → appliquer la réduction de parrainage
                        var user = userRepository.findByEmail(userEmail)
                                        .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
                        response = simulatorService.simulate(request, user.getId());
                } else {
                        // Utilisateur non connecté (page d'accueil publique) → simulation sans
                        // réduction
                        response = simulatorService.simulate(request);
                }
                return ResponseEntity.ok(
                                ibrahima.dickoya.sendme.dto.response.ApiResponse
                                                .success("Simulation effectuee", response));
        }

        @PostMapping("/api/transfers")
        @Operation(summary = "Initier un transfert d'argent", description = """
                        Orchestre le transfert complet :
                        1. Vérification de l'idempotence (évite les doublons)
                        2. Débit de la carte via Stripe
                        3. Envoi vers l'opérateur cible (Strategy Pattern)
                        4. Notification temps réel via SSE

                        **Header requis :** `Idempotency-Key: <uuid-v4>`

                        **Cartes de test Stripe :**
                        - `pm_card_visa` → succès
                        - `pm_card_visa_chargeDeclined` → refusée
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Transfert initié avec succès"),
                        @ApiResponse(responseCode = "400", description = "Paiement refusé ou transfert échoué"),
                        @ApiResponse(responseCode = "401", description = "Token JWT invalide"),
                        @ApiResponse(responseCode = "404", description = "Bénéficiaire non trouvé")
        })
        public ResponseEntity<ibrahima.dickoya.sendme.dto.response.ApiResponse<TransactionResponse>> transfer(
                        @Valid @RequestBody TransferRequest request,
                        @AuthenticationPrincipal UserDetails userDetails) {
                var response = transferService.processTransfer(
                                request, userDetails.getUsername());

                // Si la transaction est en échec, retourner une erreur 400
                // avec le message explicite plutôt qu'un 201 trompeur
                var status = response.getStatus();
                if (status == ibrahima.dickoya.sendme.enums.TransactionStatus.FAILED) {
                        // Utiliser le message Stripe s'il est disponible, sinon message générique
                        var stripeStatus = response.getStripePaymentStatus();
                        var errorMsg = stripeStatus != null && !stripeStatus.isBlank()
                                        ? "Paiement refuse : " + stripeStatus
                                        : "Paiement refuse par votre banque. Veuillez verifier vos informations de carte et reessayer.";
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ibrahima.dickoya.sendme.dto.response.ApiResponse
                                                        .error(errorMsg));
                }
                if (status == ibrahima.dickoya.sendme.enums.TransactionStatus.REFUNDED) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ibrahima.dickoya.sendme.dto.response.ApiResponse
                                                        .error("Transfert echoue apres validation. Votre carte a ete remboursee automatiquement."));
                }

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ibrahima.dickoya.sendme.dto.response.ApiResponse
                                                .success("Transfert initie avec succes", response));
        }

        @GetMapping("/api/transactions")
        @Operation(summary = "Historique des transactions", description = """
                        Retourne l'historique paginé des transactions de l'utilisateur connecté.
                        Triées par date décroissante (plus récentes en premier).

                        **Paramètres de pagination :**
                        - `page` : numéro de page (commence à 0)
                        - `size` : éléments par page (défaut : 10)

                        Exemple : `GET /api/transactions?page=0&size=10`
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Historique retourné"),
                        @ApiResponse(responseCode = "401", description = "Token JWT invalide")
        })
        public ResponseEntity<ibrahima.dickoya.sendme.dto.response.ApiResponse<Page<TransactionResponse>>> getHistory(
                        @Parameter(description = "Numéro de page (commence à 0)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Nombre d'éléments par page") @RequestParam(defaultValue = "10") int size,
                        @AuthenticationPrincipal UserDetails userDetails) {
                Pageable pageable = PageRequest.of(page, size,
                                Sort.by("createdAt").descending());
                var transactions = transferService.getHistory(
                                userDetails.getUsername(), pageable);
                return ResponseEntity.ok(
                                ibrahima.dickoya.sendme.dto.response.ApiResponse
                                                .success("Historique des transactions", transactions));
        }

        @GetMapping("/api/transactions/{id}")
        @Operation(summary = "Détail d'une transaction", description = "Retourne le détail complet d'une transaction par son identifiant. "
                        +
                        "Vérifie que la transaction appartient à l'utilisateur connecté.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Détail retourné"),
                        @ApiResponse(responseCode = "403", description = "Cette transaction ne vous appartient pas"),
                        @ApiResponse(responseCode = "404", description = "Transaction non trouvée")
        })
        public ResponseEntity<ibrahima.dickoya.sendme.dto.response.ApiResponse<TransactionResponse>> getById(
                        @Parameter(description = "Identifiant de la transaction", required = true) @PathVariable Long id,
                        @AuthenticationPrincipal UserDetails userDetails) {
                var response = transferService.getById(id, userDetails.getUsername());
                return ResponseEntity.ok(
                                ibrahima.dickoya.sendme.dto.response.ApiResponse
                                                .success("Detail de la transaction", response));
        }

        @GetMapping("/api/transactions/{id}/receipt")
        @Operation(summary = "Télécharger le reçu PDF", description = """
                        Génère et télécharge le reçu PDF officiel d'une transaction.

                        Le PDF contient :
                        - Informations de l'expéditeur et du bénéficiaire
                        - Détails financiers (montant, frais, taux de change)
                        - Référence Stripe du paiement
                        - Statut de la transaction

                        **Response Content-Type :** `application/pdf`
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "PDF généré et retourné en téléchargement"),
                        @ApiResponse(responseCode = "403", description = "Cette transaction ne vous appartient pas"),
                        @ApiResponse(responseCode = "404", description = "Transaction non trouvée")
        })
        public ResponseEntity<byte[]> downloadReceipt(
                        @Parameter(description = "Identifiant de la transaction", required = true) @PathVariable Long id,
                        @AuthenticationPrincipal UserDetails userDetails) {
                Transaction transaction = transferService
                                .getEntityById(id, userDetails.getUsername());
                byte[] pdfBytes = receiptPdfService.generateReceipt(transaction);
                String filename = "recu-" + transaction.getReference() + ".pdf";
                return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=\"" + filename + "\"")
                                .body(pdfBytes);
        }

        @GetMapping("/api/transactions/{id}/receipt/preview")
        @Operation(summary = "Prévisualiser le reçu PDF", description = """
                        Génère le reçu PDF officiel d'une transaction et le retourne pour affichage en ligne.
                        Contrairement au téléchargement, le Content-Disposition est mis à 'inline'
                        pour permettre au navigateur d'ouvrir le fichier directement dans un nouvel onglet.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "PDF généré et retourné pour prévisualisation"),
                        @ApiResponse(responseCode = "403", description = "Cette transaction ne vous appartient pas"),
                        @ApiResponse(responseCode = "404", description = "Transaction non trouvée")
        })
        public ResponseEntity<byte[]> previewReceipt(
                        @Parameter(description = "Identifiant de la transaction", required = true) @PathVariable Long id,
                        @AuthenticationPrincipal UserDetails userDetails) {

                // 1. Récupérer l'entité et vérifier les droits d'accès
                Transaction transaction = transferService
                                .getEntityById(id, userDetails.getUsername());

                // 2. Réutiliser ton service de génération PDF existant
                byte[] pdfBytes = receiptPdfService.generateReceipt(transaction);
                String filename = "recu-" + transaction.getReference() + ".pdf";

                // 3. Retourner le flux avec la disposition INLINE (ouvre l'onglet au lieu de
                // forcer le téléchargement)
                return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                                .body(pdfBytes);
        }

        /**
         * Retourne la liste des canaux de paiement actifs.
         * Endpoint public (sans authentification) utilisé par le frontend
         * pour afficher uniquement les canaux disponibles dans les formulaires
         * de bénéficiaires et de transferts.
         *
         * GET /api/channels/active
         *
         * @return 200 OK avec la liste des canaux actifs
         */
        @GetMapping("/api/channels/active")
        @Operation(summary = "Canaux de paiement actifs", description = "Retourne la liste des canaux de paiement actifs. Endpoint public utilisé par les formulaires de bénéficiaires et de transferts.")
        public ResponseEntity<ibrahima.dickoya.sendme.dto.response.ApiResponse<List<PaymentChannelResponse>>> getActiveChannels() {
                List<PaymentChannelResponse> channels = paymentChannelService.getActiveChannels();
                return ResponseEntity.ok(
                                ibrahima.dickoya.sendme.dto.response.ApiResponse.success("Canaux actifs", channels));
        }
}
