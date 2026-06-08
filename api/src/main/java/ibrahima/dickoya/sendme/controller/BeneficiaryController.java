package ibrahima.dickoya.sendme.controller;

import ibrahima.dickoya.sendme.dto.request.BeneficiaryRequest;
import ibrahima.dickoya.sendme.dto.response.*;
import ibrahima.dickoya.sendme.service.BeneficiaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST gérant les endpoints des bénéficiaires.
 *
 * Toutes les routes nécessitent un JWT valide (configuré dans SecurityConfig).
 * L'utilisateur connecté est extrait automatiquement du JWT via @AuthenticationPrincipal.
 *
 * Routes disponibles :
 * GET    /api/beneficiaries          → lister ses bénéficiaires
 * POST   /api/beneficiaries          → créer un bénéficiaire
 * PUT    /api/beneficiaries/{id}     → modifier un bénéficiaire
 * DELETE /api/beneficiaries/{id}     → supprimer un bénéficiaire
 */
@RestController
@RequestMapping("/api/beneficiaries")
@RequiredArgsConstructor
public class BeneficiaryController {

    /** Service contenant la logique métier des bénéficiaires. */
    private final BeneficiaryService beneficiaryService;

    /**
     * Récupère la liste de tous les bénéficiaires de l'utilisateur connecté.
     *
     * @param userDetails l'utilisateur connecté (extrait automatiquement du JWT)
     * @return 200 OK avec la liste des bénéficiaires
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BeneficiaryResponse>>> getAll(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<BeneficiaryResponse> beneficiaries =
                beneficiaryService.findAllByUser(userDetails.getUsername());

        return ResponseEntity.ok(
                ApiResponse.success("Liste des beneficiaires", beneficiaries));
    }

    /**
     * Crée un nouveau bénéficiaire pour l'utilisateur connecté.
     *
     * @param request     les données du nouveau bénéficiaire (validées automatiquement)
     * @param userDetails l'utilisateur connecté (extrait du JWT)
     * @return 201 CREATED avec le bénéficiaire créé
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BeneficiaryResponse>> create(
            @Valid @RequestBody BeneficiaryRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        BeneficiaryResponse response =
                beneficiaryService.create(request, userDetails.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Beneficiaire cree avec succes", response));
    }

    /**
     * Modifie un bénéficiaire existant appartenant à l'utilisateur connecté.
     *
     * @param id          l'identifiant du bénéficiaire à modifier
     * @param request     les nouvelles données
     * @param userDetails l'utilisateur connecté (extrait du JWT)
     * @return 200 OK avec le bénéficiaire mis à jour
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BeneficiaryResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody BeneficiaryRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        BeneficiaryResponse response =
                beneficiaryService.update(id, request, userDetails.getUsername());

        return ResponseEntity.ok(
                ApiResponse.success("Beneficiaire mis a jour", response));
    }

    /**
     * Supprime un bénéficiaire appartenant à l'utilisateur connecté.
     *
     * @param id          l'identifiant du bénéficiaire à supprimer
     * @param userDetails l'utilisateur connecté (extrait du JWT)
     * @return 200 OK avec message de confirmation
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        beneficiaryService.delete(id, userDetails.getUsername());

        return ResponseEntity.ok(
                ApiResponse.success("Beneficiaire supprime avec succes", null));
    }
}