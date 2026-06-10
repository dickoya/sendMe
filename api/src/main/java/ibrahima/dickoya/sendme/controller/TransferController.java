package ibrahima.dickoya.sendme.controller;

import ibrahima.dickoya.sendme.dto.request.*;
import ibrahima.dickoya.sendme.dto.response.*;
import ibrahima.dickoya.sendme.entity.Transaction;
import ibrahima.dickoya.sendme.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST gérant les endpoints des transferts et transactions.
 *
 * Routes disponibles :
 * POST /api/transfers/simulate → simuler frais + taux de change
 * POST /api/transfers          → initier un transfert
 * GET  /api/transactions       → historique paginé
 * GET  /api/transactions/{id}  → détail d'une transaction
 *
 * Toutes les routes nécessitent un JWT valide.
 */
@RestController
@RequiredArgsConstructor
public class TransferController {

    /** Service de simulation des frais et taux de change. */
    private final SimulatorService simulatorService;

    /** Service principal des transferts. */
    private final TransferService transferService;

    // Modification A : C'est ici qu'il faut l'ajouter !
    /** Service de génération des reçus PDF. */
    private final ReceiptPdfService receiptPdfService;



    /**
     * Simule un transfert et retourne le récapitulatif complet.
     * Appelé en temps réel à chaque modification du montant côté frontend.
     *
     * @param request   montant, devises et canal
     * @return 200 OK avec le récapitulatif (frais, taux, montant reçu)
     */
    @PostMapping("/api/transfers/simulate")
    public ResponseEntity<ApiResponse<SimulateResponse>> simulate(
            @Valid @RequestBody SimulateRequest request) {

        SimulateResponse response = simulatorService.simulate(request);
        return ResponseEntity.ok(
                ApiResponse.success("Simulation effectuee", response));
    }

    /**
     * Initie un nouveau transfert d'argent.
     *
     * Le header Idempotency-Key dans le body protège contre les doublons.
     * Si le même idempotencyKey est envoyé deux fois, la même transaction
     * est retournée sans re-débiter (RG-06).
     *
     * @param request     données du transfert avec idempotencyKey
     * @param userDetails utilisateur connecté extrait du JWT
     * @return 201 CREATED avec le détail de la transaction créée
     */
    @PostMapping("/api/transfers")
    public ResponseEntity<ApiResponse<TransactionResponse>> transfer(
            @Valid @RequestBody TransferRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        TransactionResponse response =
                transferService.processTransfer(request, userDetails.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transfert initie avec succes", response));
    }

    /**
     * Récupère l'historique paginé des transactions de l'utilisateur connecté.
     *
     * Paramètres de pagination (optionnels) :
     * - page : numéro de page (commence à 0)
     * - size : nombre d'éléments par page (défaut : 10)
     *
     * Exemple : GET /api/transactions?page=0&size=10
     *
     * @param page        numéro de page
     * @param size        taille de la page
     * @param userDetails utilisateur connecté
     * @return 200 OK avec la page de transactions
     */
    @GetMapping("/api/transactions")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        // Créer l'objet Pageable avec tri par date décroissante
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());

        Page<TransactionResponse> transactions =
                transferService.getHistory(userDetails.getUsername(), pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Historique des transactions", transactions));
    }

    /**
     * Récupère le détail d'une transaction spécifique par son identifiant.
     *
     * @param id          l'identifiant de la transaction
     * @param userDetails utilisateur connecté
     * @return 200 OK avec le détail complet de la transaction
     */
    @GetMapping("/api/transactions/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        TransactionResponse response =
                transferService.getById(id, userDetails.getUsername());

        return ResponseEntity.ok(
                ApiResponse.success("Detail de la transaction", response));
    }
    /**
     * Télécharge le reçu PDF d'une transaction par son identifiant.
     *
     * Retourne un fichier PDF avec les headers HTTP appropriés
     * pour déclencher le téléchargement dans le navigateur.
     *
     * Headers retournés :
     * - Content-Type: application/pdf
     * - Content-Disposition: attachment; filename="recu-TXN-XXX.pdf"
     *
     * @param id          identifiant de la transaction
     * @param userDetails utilisateur connecté
     * @return ResponseEntity avec le contenu PDF en bytes
     */
    @GetMapping("/api/transactions/{id}/receipt")
    public ResponseEntity<byte[]> downloadReceipt(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        // Récupérer la transaction (avec vérification de propriété)
        Transaction transaction = transferService
                .getEntityById(id, userDetails.getUsername());

        // Générer le PDF
        byte[] pdfBytes = receiptPdfService.generateReceipt(transaction);

        // Construire le nom de fichier
        String filename = "recu-" + transaction.getReference() + ".pdf";

        // Retourner le PDF avec les bons headers HTTP
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_LENGTH,
                        String.valueOf(pdfBytes.length))
                .body(pdfBytes);
    }

    /**
     * Prévisualise le reçu PDF dans le navigateur (sans téléchargement).
     * Utilise Content-Disposition: inline au lieu de attachment.
     *
     * @param id          identifiant de la transaction
     * @param userDetails utilisateur connecté
     * @return ResponseEntity avec le contenu PDF en bytes
     */
    @GetMapping("/api/transactions/{id}/receipt/preview")
    public ResponseEntity<byte[]> previewReceipt(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Transaction transaction = transferService
                .getEntityById(id, userDetails.getUsername());

        byte[] pdfBytes = receiptPdfService.generateReceipt(transaction);
        String filename = "recu-" + transaction.getReference() + ".pdf";

        // inline = s'ouvre dans le navigateur, pas de téléchargement forcé
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + filename + "\"")
                .body(pdfBytes);
    }
}