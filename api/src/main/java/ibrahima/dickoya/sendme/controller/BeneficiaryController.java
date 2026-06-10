package ibrahima.dickoya.sendme.controller;

import ibrahima.dickoya.sendme.dto.request.BeneficiaryRequest;
import ibrahima.dickoya.sendme.dto.response.*;
import ibrahima.dickoya.sendme.service.BeneficiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour la gestion du carnet de bénéficiaires.
 */
@RestController
@RequestMapping("/api/beneficiaries")
@RequiredArgsConstructor
@Tag(
        name = "Bénéficiaires",
        description = """
        Gestion du carnet d'adresses des bénéficiaires.
        
        Un bénéficiaire est une personne enregistrée par l'utilisateur
        pour recevoir des transferts d'argent. Chaque bénéficiaire
        est associé à un canal de réception spécifique.
        
        **Règle RG-02 :**
        - Canal BANK_TRANSFER → IBAN et BIC obligatoires
        - Autres canaux → numéro de téléphone obligatoire
        
        JWT requis pour tous les endpoints.
        """
)
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    @GetMapping
    @Operation(
            summary = "Lister mes bénéficiaires",
            description = "Retourne tous les bénéficiaires de l'utilisateur connecté, " +
                    "triés par date de création décroissante."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste retournée avec succès"),
            @ApiResponse(responseCode = "401", description = "Token JWT manquant ou invalide")
    })
    public ResponseEntity<ibrahima.dickoya.sendme.dto.response.ApiResponse<List<BeneficiaryResponse>>>
    getAll(@AuthenticationPrincipal UserDetails userDetails) {
        var beneficiaries = beneficiaryService.findAllByUser(userDetails.getUsername());
        return ResponseEntity.ok(
                ibrahima.dickoya.sendme.dto.response.ApiResponse
                        .success("Liste des beneficiaires", beneficiaries));
    }

    @PostMapping
    @Operation(
            summary = "Créer un bénéficiaire",
            description = """
            Ajoute un nouveau bénéficiaire dans le carnet de l'utilisateur.
            
            Selon le canal choisi, les champs requis changent :
            - **BANK_TRANSFER** : bankDetails (bankName, iban, bic) obligatoire
            - **Autres canaux** : mobileDetails (phoneNumber) obligatoire
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Bénéficiaire créé avec succès"),
            @ApiResponse(responseCode = "400",
                    description = "Données invalides ou champs manquants selon le canal"),
            @ApiResponse(responseCode = "401", description = "Token JWT manquant ou invalide")
    })
    public ResponseEntity<ibrahima.dickoya.sendme.dto.response.ApiResponse<BeneficiaryResponse>>
    create(
            @Valid @RequestBody BeneficiaryRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        var response = beneficiaryService.create(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ibrahima.dickoya.sendme.dto.response.ApiResponse
                        .success("Beneficiaire cree avec succes", response));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Modifier un bénéficiaire",
            description = "Met à jour les informations d'un bénéficiaire existant. " +
                    "Vérifie que le bénéficiaire appartient à l'utilisateur connecté."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bénéficiaire mis à jour"),
            @ApiResponse(responseCode = "403", description = "Ce bénéficiaire ne vous appartient pas"),
            @ApiResponse(responseCode = "404", description = "Bénéficiaire non trouvé")
    })
    public ResponseEntity<ibrahima.dickoya.sendme.dto.response.ApiResponse<BeneficiaryResponse>>
    update(
            @Parameter(description = "Identifiant du bénéficiaire", required = true)
            @PathVariable Long id,
            @Valid @RequestBody BeneficiaryRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        var response = beneficiaryService.update(id, request, userDetails.getUsername());
        return ResponseEntity.ok(
                ibrahima.dickoya.sendme.dto.response.ApiResponse
                        .success("Beneficiaire mis a jour", response));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Supprimer un bénéficiaire",
            description = "Supprime définitivement un bénéficiaire du carnet. " +
                    "Supprime aussi les détails bancaires ou Mobile Money associés."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bénéficiaire supprimé"),
            @ApiResponse(responseCode = "403", description = "Ce bénéficiaire ne vous appartient pas"),
            @ApiResponse(responseCode = "404", description = "Bénéficiaire non trouvé")
    })
    public ResponseEntity<ibrahima.dickoya.sendme.dto.response.ApiResponse<Void>>
    delete(
            @Parameter(description = "Identifiant du bénéficiaire", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        beneficiaryService.delete(id, userDetails.getUsername());
        return ResponseEntity.ok(
                ibrahima.dickoya.sendme.dto.response.ApiResponse
                        .success("Beneficiaire supprime avec succes", null));
    }
}