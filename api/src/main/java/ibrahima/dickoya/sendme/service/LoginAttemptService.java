package ibrahima.dickoya.sendme.service;

import ibrahima.dickoya.sendme.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service responsable du suivi et de la limitation des tentatives de connexion
 * par email.
 * <p>
 * Compte le nombre de tentatives échouées consécutives pour un email donné.
 * Déclenche une alerte de sécurité par email lorsque le seuil (5) est atteint.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoginAttemptService {

    private static final int MAX_LOGIN_ATTEMPTS = 5;

    private final Map<String, Integer> loginAttempts = new ConcurrentHashMap<>();
    private final EmailService emailService;
    private final UserRepository userRepository;

    /**
     * Enregistre une tentative de connexion échouée pour un email.
     * Si le seuil de tentatives est atteint, une alerte de sécurité est envoyée
     * par email et le compteur est réinitialisé.
     *
     * @param email     l'adresse email concernée
     * @param ipAddress l'adresse IP depuis laquelle la tentative a eu lieu
     */
    public void recordFailedAttempt(String email, String ipAddress) {
        int currentAttempts = loginAttempts.merge(email, 1, Integer::sum);
        log.warn("Tentative de connexion echouee pour : {} — tentative n°{}", email, currentAttempts);

        if (currentAttempts >= MAX_LOGIN_ATTEMPTS) {
            try {
                userRepository.findByEmail(email)
                        .ifPresent(user -> emailService.sendSuspiciousLoginEmail(user, ipAddress, currentAttempts));
            } catch (Exception e) {
                log.error("[EMAIL] Impossible d'envoyer l'alerte de sécurité : {}", e.getMessage());
            }
            loginAttempts.put(email, 0);
        }
    }

    /**
     * Réinitialise le compteur de tentatives pour un email (appelé après une
     * connexion réussie).
     *
     * @param email l'adresse email à réinitialiser
     */
    public void resetAttempts(String email) {
        loginAttempts.remove(email);
    }

    /**
     * Retourne le nombre actuel de tentatives échouées pour un email.
     *
     * @param email l'adresse email à consulter
     * @return le nombre de tentatives échouées, ou 0 si aucune
     */
    public int getCurrentAttempts(String email) {
        return loginAttempts.getOrDefault(email, 0);
    }
}
