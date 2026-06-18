package ibrahima.dickoya.sendme.service;

import ibrahima.dickoya.sendme.dto.request.ChangePasswordRequest;
import ibrahima.dickoya.sendme.dto.request.DeleteAccountRequest;
import ibrahima.dickoya.sendme.dto.request.UpdateProfileRequest;
import ibrahima.dickoya.sendme.dto.response.LoginHistoryResponse;
import ibrahima.dickoya.sendme.dto.response.ProfileResponse;
import ibrahima.dickoya.sendme.entity.LoginHistory;
import ibrahima.dickoya.sendme.entity.User;
import ibrahima.dickoya.sendme.exception.ResourceNotFoundException;
import ibrahima.dickoya.sendme.repository.LoginHistoryRepository;
import ibrahima.dickoya.sendme.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final UserRepository userRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Récupère le profil de l'utilisateur connecté.
     */
    public ProfileResponse getProfile(String email) {
        User user = findUserByEmail(email);
        return mapToProfileResponse(user);
    }

    /**
     * Met à jour le profil de l'utilisateur connecté.
     */
    @Transactional
    public ProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = findUserByEmail(email);

        // Vérifier si le nouvel email est déjà pris par un autre utilisateur
        if (!request.getEmail().equals(user.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Un compte existe deja avec l'email : " + request.getEmail());
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());

        userRepository.save(user);
        log.info("Profil mis à jour pour : {}", user.getEmail());

        return mapToProfileResponse(user);
    }

    /**
     * Change le mot de passe de l'utilisateur connecté.
     */
    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = findUserByEmail(email);

        // Vérifier l'ancien mot de passe
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Le mot de passe actuel est incorrect");
        }

        // Vérifier que les deux nouveaux mots de passe correspondent
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Les nouveaux mots de passe ne correspondent pas");
        }

        // Vérifier que le nouveau mot de passe est différent de l'ancien
        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new IllegalArgumentException("Le nouveau mot de passe doit être différent de l'ancien");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Mot de passe changé pour : {}", user.getEmail());
    }

    /**
     * Supprime le compte de l'utilisateur connecté.
     */
    @Transactional
    public void deleteAccount(String email, DeleteAccountRequest request) {
        User user = findUserByEmail(email);

        // Vérifier le mot de passe
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Mot de passe incorrect");
        }

        // Désactiver le compte plutôt que de le supprimer physiquement
        user.setIsActive(false);
        userRepository.save(user);

        log.warn("Compte désactivé pour : {}", user.getEmail());
    }

    /**
     * Récupère l'historique de connexion de l'utilisateur connecté.
     */
    public List<LoginHistoryResponse> getLoginHistory(String email) {
        User user = findUserByEmail(email);
        List<LoginHistory> history = loginHistoryRepository.findTop10ByUserIdOrderByLoginTimeDesc(user.getId());

        return history.stream()
                .map(this::mapToLoginHistoryResponse)
                .collect(Collectors.toList());
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable avec l'email : " + email));
    }

    private ProfileResponse mapToProfileResponse(User user) {
        return ProfileResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private LoginHistoryResponse mapToLoginHistoryResponse(LoginHistory history) {
        return LoginHistoryResponse.builder()
                .id(history.getId())
                .ipAddress(history.getIpAddress())
                .userAgent(history.getUserAgent())
                .success(history.isSuccess())
                .loginTime(history.getLoginTime())
                .build();
    }
}
