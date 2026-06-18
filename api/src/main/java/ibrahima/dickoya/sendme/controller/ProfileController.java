package ibrahima.dickoya.sendme.controller;

import ibrahima.dickoya.sendme.dto.request.ChangePasswordRequest;
import ibrahima.dickoya.sendme.dto.request.DeleteAccountRequest;
import ibrahima.dickoya.sendme.dto.request.UpdateProfileRequest;
import ibrahima.dickoya.sendme.dto.response.ApiResponse;
import ibrahima.dickoya.sendme.dto.response.LoginHistoryResponse;
import ibrahima.dickoya.sendme.dto.response.ProfileResponse;
import ibrahima.dickoya.sendme.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour la gestion du profil utilisateur.
 * Tous les endpoints nécessitent un utilisateur authentifié.
 */
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Tag(name = "Profil", description = "Gestion du profil utilisateur (modification, mot de passe, historique de connexion)")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final ProfileService profileService;

    /**
     * Récupère le profil de l'utilisateur connecté.
     */
    @GetMapping
    @Operation(summary = "Récupérer le profil", description = "Retourne les informations du profil de l'utilisateur connecté.")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        ProfileResponse profile = profileService.getProfile(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Profil récupéré avec succès", profile));
    }

    /**
     * Met à jour le profil de l'utilisateur connecté.
     */
    @PutMapping
    @Operation(summary = "Mettre à jour le profil", description = "Met à jour les informations du profil (nom, email, téléphone).")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        ProfileResponse profile = profileService.updateProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Profil mis à jour avec succès", profile));
    }

    /**
     * Change le mot de passe de l'utilisateur connecté.
     */
    @PutMapping("/password")
    @Operation(summary = "Changer le mot de passe", description = "Change le mot de passe de l'utilisateur connecté.")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        profileService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Mot de passe changé avec succès", null));
    }

    /**
     * Supprime (désactive) le compte de l'utilisateur connecté.
     */
    @DeleteMapping
    @Operation(summary = "Supprimer le compte", description = "Désactive le compte de l'utilisateur connecté.")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody DeleteAccountRequest request) {
        profileService.deleteAccount(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Compte supprimé avec succès", null));
    }

    /**
     * Récupère l'historique de connexion de l'utilisateur connecté.
     */
    @GetMapping("/login-history")
    @Operation(summary = "Historique de connexion", description = "Retourne les 10 dernières connexions de l'utilisateur.")
    public ResponseEntity<ApiResponse<List<LoginHistoryResponse>>> getLoginHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<LoginHistoryResponse> history = profileService.getLoginHistory(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Historique récupéré avec succès", history));
    }
}
