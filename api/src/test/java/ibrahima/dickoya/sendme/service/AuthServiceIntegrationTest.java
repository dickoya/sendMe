package ibrahima.dickoya.sendme.service;

import ibrahima.dickoya.sendme.config.TestH2Config;
import ibrahima.dickoya.sendme.dto.request.LoginRequest;
import ibrahima.dickoya.sendme.dto.request.RegisterRequest;
import ibrahima.dickoya.sendme.dto.response.AuthResponse;
import ibrahima.dickoya.sendme.entity.User;
import ibrahima.dickoya.sendme.exception.EmailAlreadyExistsException;
import ibrahima.dickoya.sendme.exception.EmailNotVerifiedException;
import ibrahima.dickoya.sendme.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test d'intégration du service d'authentification avec base H2.
 *
 * Couvre le flux complet :
 * 1. Inscription → vérification email → connexion
 * 2. Cas d'erreur : email déjà existant, token invalide, email non vérifié
 *
 * Les dépendances externes (EmailService) sont mockées.
 * La base de données H2 est utilisée en mode mémoire.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(TestH2Config.class)
class AuthServiceIntegrationTest {

        @Autowired
        private AuthService authService;

        @Autowired
        private UserRepository userRepository;

        @MockBean
        private EmailService emailService;

        @MockBean
        private HttpServletRequest httpServletRequest;

        private RegisterRequest registerRequest;

        @BeforeEach
        void setUp() {
                // Configuration du mock HTTP pour le login
                when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn(null);
                when(httpServletRequest.getHeader("X-Real-IP")).thenReturn(null);
                when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
                when(httpServletRequest.getHeader("User-Agent")).thenReturn("Test-Agent");

                // Préparer une requête d'inscription standard
                registerRequest = RegisterRequest.builder()
                                .firstName("Jean")
                                .lastName("Dupont")
                                .email("jean.dupont@email.com")
                                .password("Password123!")
                                .phoneNumber("+33612345678")
                                .build();
        }

        @Test
        @DisplayName("Inscription → vérification email → connexion : flux complet")
        void testRegisterVerifyEmailAndLogin() {
                // ÉTAPE 1 : Inscription
                AuthResponse registerResponse = authService.register(registerRequest);

                assertThat(registerResponse).isNotNull();
                assertThat(registerResponse.getEmail()).isEqualTo("jean.dupont@email.com");
                assertThat(registerResponse.getFirstName()).isEqualTo("Jean");
                assertThat(registerResponse.getLastName()).isEqualTo("Dupont");
                assertThat(registerResponse.getEmailVerified()).isFalse();
                assertThat(registerResponse.getAccessToken()).isNull(); // Pas de token avant vérification
                assertThat(registerResponse.getReferralCode()).startsWith("SENDME-");

                // Vérifier que l'utilisateur est bien persisté en base
                User savedUser = userRepository.findByEmail("jean.dupont@email.com").orElseThrow();
                assertThat(savedUser.getEmailVerified()).isFalse();
                assertThat(savedUser.getEmailVerificationToken()).isNotNull();
                assertThat(savedUser.getIsActive()).isTrue();

                // Vérifier que l'email de vérification a été envoyé
                verify(emailService, times(1)).sendEmailVerificationEmail(any(User.class), anyString());

                // ÉTAPE 2 : Vérification email
                String verificationToken = savedUser.getEmailVerificationToken();
                AuthResponse verifyResponse = authService.verifyEmail(verificationToken);

                assertThat(verifyResponse).isNotNull();
                assertThat(verifyResponse.getEmailVerified()).isTrue();
                assertThat(verifyResponse.getAccessToken()).isNotNull();
                assertThat(verifyResponse.getRefreshToken()).isNotNull();
                assertThat(verifyResponse.getTokenType()).isEqualTo("Bearer");

                // Vérifier que l'utilisateur est bien marqué comme vérifié en base
                User verifiedUser = userRepository.findByEmail("jean.dupont@email.com").orElseThrow();
                assertThat(verifiedUser.getEmailVerified()).isTrue();
                assertThat(verifiedUser.getEmailVerificationToken()).isNull();

                // Vérifier que l'email de bienvenue a été envoyé
                verify(emailService, times(1)).sendWelcomeEmail(any(User.class));

                // ÉTAPE 3 : Connexion
                LoginRequest loginRequest = LoginRequest.builder()
                                .email("jean.dupont@email.com")
                                .password("Password123!")
                                .build();

                AuthResponse loginResponse = authService.login(loginRequest, httpServletRequest);

                assertThat(loginResponse).isNotNull();
                assertThat(loginResponse.getEmail()).isEqualTo("jean.dupont@email.com");
                assertThat(loginResponse.getEmailVerified()).isTrue();
                assertThat(loginResponse.getAccessToken()).isNotNull();
                assertThat(loginResponse.getRefreshToken()).isNotNull();

                // Vérifier que l'alerte de connexion a été envoyée
                verify(emailService, times(1)).sendLoginAlertEmail(any(User.class), anyString());
        }

        @Test
        @DisplayName("Inscription avec un email déjà existant → EmailAlreadyExistsException")
        void testRegisterWithExistingEmail() {
                // Première inscription
                authService.register(registerRequest);

                // Tentative de seconde inscription avec le même email
                RegisterRequest duplicateRequest = RegisterRequest.builder()
                                .firstName("Autre")
                                .lastName("Nom")
                                .email("jean.dupont@email.com")
                                .password("AutrePass123!")
                                .build();

                assertThatThrownBy(() -> authService.register(duplicateRequest))
                                .isInstanceOf(EmailAlreadyExistsException.class)
                                .hasMessageContaining("existe deja");
        }

        @Test
        @DisplayName("Connexion avec email non vérifié → EmailNotVerifiedException")
        void testLoginWithUnverifiedEmail() {
                // Inscription sans vérification email
                authService.register(registerRequest);

                // Tentative de connexion
                LoginRequest loginRequest = LoginRequest.builder()
                                .email("jean.dupont@email.com")
                                .password("Password123!")
                                .build();

                assertThatThrownBy(() -> authService.login(loginRequest, httpServletRequest))
                                .isInstanceOf(EmailNotVerifiedException.class)
                                .hasMessageContaining("Veuillez vérifier");
        }

        @Test
        @DisplayName("Connexion avec mot de passe incorrect → BadCredentialsException")
        void testLoginWithWrongPassword() {
                // Inscription et vérification email
                authService.register(registerRequest);
                User savedUser = userRepository.findByEmail("jean.dupont@email.com").orElseThrow();
                authService.verifyEmail(savedUser.getEmailVerificationToken());

                // Tentative de connexion avec mauvais mot de passe
                LoginRequest loginRequest = LoginRequest.builder()
                                .email("jean.dupont@email.com")
                                .password("WrongPassword123!")
                                .build();

                assertThatThrownBy(() -> authService.login(loginRequest, httpServletRequest))
                                .isInstanceOf(BadCredentialsException.class)
                                .hasMessageContaining("incorrect");
        }

        @Test
        @DisplayName("Vérification email avec token invalide → IllegalArgumentException")
        void testVerifyEmailWithInvalidToken() {
                assertThatThrownBy(() -> authService.verifyEmail("invalid-token-12345"))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("invalide");
        }

        @Test
        @DisplayName("Renvoi de l'email de vérification → nouveau token généré")
        void testResendVerificationEmail() {
                // Inscription
                authService.register(registerRequest);

                // Récupérer le token initial
                User userBefore = userRepository.findByEmail("jean.dupont@email.com").orElseThrow();
                String initialToken = userBefore.getEmailVerificationToken();

                // Renvoyer l'email de vérification
                authService.resendVerificationEmail("jean.dupont@email.com");

                // Vérifier qu'un nouveau token a été généré
                User userAfter = userRepository.findByEmail("jean.dupont@email.com").orElseThrow();
                assertThat(userAfter.getEmailVerificationToken()).isNotEqualTo(initialToken);
                assertThat(userAfter.getEmailVerificationToken()).isNotNull();

                // Vérifier que l'email a été renvoyé
                verify(emailService, times(2)).sendEmailVerificationEmail(any(User.class), anyString());
        }

        @Test
        @DisplayName("Inscription avec code de parrainage → lien de parrainage créé")
        void testRegisterWithReferralCode() {
                // Créer un parrain
                RegisterRequest sponsorRequest = RegisterRequest.builder()
                                .firstName("Marie")
                                .lastName("Sponsor")
                                .email("marie.sponsor@email.com")
                                .password("Password123!")
                                .build();
                AuthResponse sponsorResponse = authService.register(sponsorRequest);

                // Inscrire un filleul avec le code du parrain
                RegisterRequest referredRequest = RegisterRequest.builder()
                                .firstName("Paul")
                                .lastName("Filleul")
                                .email("paul.filleul@email.com")
                                .password("Password123!")
                                .referralCode(sponsorResponse.getReferralCode())
                                .build();
                authService.register(referredRequest);

                // Vérifier que le filleul est lié au parrain
                User referredUser = userRepository.findByEmail("paul.filleul@email.com").orElseThrow();
                assertThat(referredUser.getReferredBy()).isNotNull();
                assertThat(referredUser.getReferredBy().getEmail()).isEqualTo("marie.sponsor@email.com");
        }
}
