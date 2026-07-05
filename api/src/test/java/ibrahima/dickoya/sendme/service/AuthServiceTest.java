package ibrahima.dickoya.sendme.service;

import ibrahima.dickoya.sendme.dto.request.LoginRequest;
import ibrahima.dickoya.sendme.dto.request.RegisterRequest;
import ibrahima.dickoya.sendme.dto.response.AuthResponse;
import ibrahima.dickoya.sendme.entity.User;
import ibrahima.dickoya.sendme.enums.Role;
import ibrahima.dickoya.sendme.exception.EmailAlreadyExistsException;
import ibrahima.dickoya.sendme.repository.UserRepository;
import ibrahima.dickoya.sendme.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour AuthService")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private UserDetails userDetails;

    @Mock
    private ReferralService referralService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;
    private final String EMAIL = "ibrahima@sendme.com";
    private final String PASSWORD = "P@ssword123";
    private final String ENCODED_PASSWORD = "encodedPassword123";

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .firstName("Ibrahim")
                .lastName("Dickoya")
                .email(EMAIL)
                .password(PASSWORD)
                .phoneNumber("+224620000000")
                .build();

        loginRequest = LoginRequest.builder()
                .email(EMAIL)
                .password(PASSWORD)
                .build();

        user = User.builder()
                .id(1L)
                .firstName("Ibrahim")
                .lastName("Dickoya")
                .email(EMAIL)
                .passwordHash(ENCODED_PASSWORD)
                .phoneNumber("+224620000000")
                .role(Role.ROLE_USER)
                .isActive(true)
                .build();
    }

    @Nested
    @DisplayName("Tests d'inscription (register)")
    class RegisterTests {

        @Test
        @DisplayName("Inscription avec succès - Doit retourner AuthResponse sans tokens")
        void register_ShouldReturnAuthResponse_WhenSuccessful() {
            // Arrange
            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(referralService.generateReferralCode()).thenReturn("SENDME-ABC123");
            when(userRepository.save(any(User.class))).thenReturn(user);

            // Act
            AuthResponse response = authService.register(registerRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isNull();
            assertThat(response.getRefreshToken()).isNull();
            assertThat(response.getEmail()).isEqualTo(EMAIL);
            assertThat(response.getFirstName()).isEqualTo("Ibrahim");
            assertThat(response.getLastName()).isEqualTo("Dickoya");
            assertThat(response.getRole()).isEqualTo(Role.ROLE_USER.name());
            assertThat(response.getEmailVerified()).isFalse();

            verify(userRepository).existsByEmail(EMAIL);
            verify(passwordEncoder).encode(PASSWORD);
            verify(referralService).generateReferralCode();
            verify(userRepository).save(any(User.class));
            verify(emailService).sendEmailVerificationEmail(any(User.class), anyString());
        }

        @Test
        @DisplayName("Inscription avec email existant - Doit lancer EmailAlreadyExistsException")
        void register_ShouldThrowEmailAlreadyExistsException_WhenEmailExists() {
            // Arrange
            when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> authService.register(registerRequest))
                    .isInstanceOf(EmailAlreadyExistsException.class)
                    .hasMessage("Un compte existe deja avec l'email : " + EMAIL);

            verify(userRepository).existsByEmail(EMAIL);
            verify(userRepository, never()).save(any(User.class));
            verify(emailService, never()).sendEmailVerificationEmail(any(), anyString());
        }

        @Test
        @DisplayName("Inscription - Doit encoder le mot de passe avant sauvegarde")
        void register_ShouldEncodePassword_WhenSuccessful() {
            // Arrange
            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(referralService.generateReferralCode()).thenReturn("SENDME-ABC123");
            when(userRepository.save(any(User.class))).thenReturn(user);

            // Act
            authService.register(registerRequest);

            // Assert
            verify(passwordEncoder).encode(PASSWORD);
            verify(userRepository).save(argThat(savedUser -> ENCODED_PASSWORD.equals(savedUser.getPasswordHash())));
        }

        @Test
        @DisplayName("Inscription - Doit créer un utilisateur avec les bons champs")
        void register_ShouldCreateUserWithCorrectFields_WhenSuccessful() {
            // Arrange
            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(referralService.generateReferralCode()).thenReturn("SENDME-ABC123");
            when(userRepository.save(any(User.class))).thenReturn(user);

            // Act
            authService.register(registerRequest);

            // Assert
            verify(userRepository).save(argThat((User savedUser) -> {
                assertThat(savedUser.getFirstName()).isEqualTo("Ibrahim");
                assertThat(savedUser.getLastName()).isEqualTo("Dickoya");
                assertThat(savedUser.getEmail()).isEqualTo(EMAIL);
                assertThat(savedUser.getPhoneNumber()).isEqualTo("+224620000000");
                assertThat(savedUser.getRole()).isEqualTo(Role.ROLE_USER);
                assertThat(savedUser.getIsActive()).isTrue();
                assertThat(savedUser.getEmailVerified()).isFalse();
                assertThat(savedUser.getReferralCode()).isEqualTo("SENDME-ABC123");
                assertThat(savedUser.getEmailVerificationToken()).isNotNull();
                assertThat(savedUser.getEmailVerificationTokenExpiresAt()).isNotNull();
                return true;
            }));
        }

        @Test
        @DisplayName("Inscription sans numéro de téléphone - Doit fonctionner")
        void register_ShouldWorkWithoutPhoneNumber_WhenPhoneNumberIsNull() {
            // Arrange
            registerRequest.setPhoneNumber(null);
            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(referralService.generateReferralCode()).thenReturn("SENDME-ABC123");
            when(userRepository.save(any(User.class))).thenReturn(user);

            // Act
            AuthResponse response = authService.register(registerRequest);

            // Assert
            assertThat(response).isNotNull();
            verify(userRepository).save(argThat(savedUser -> savedUser.getPhoneNumber() == null));
        }

        @Test
        @DisplayName("Inscription - Doit envoyer l'email de vérification")
        void register_ShouldSendVerificationEmail_WhenSuccessful() {
            // Arrange
            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(referralService.generateReferralCode()).thenReturn("SENDME-ABC123");
            when(userRepository.save(any(User.class))).thenReturn(user);

            // Act
            authService.register(registerRequest);

            // Assert
            verify(emailService).sendEmailVerificationEmail(any(User.class), anyString());
        }

        @Test
        @DisplayName("Inscription - Ne doit pas planter si l'email de vérification échoue")
        void register_ShouldNotThrow_WhenVerificationEmailFails() {
            // Arrange
            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(referralService.generateReferralCode()).thenReturn("SENDME-ABC123");
            when(userRepository.save(any(User.class))).thenReturn(user);
            doThrow(new RuntimeException("SMTP error"))
                    .when(emailService).sendEmailVerificationEmail(any(User.class), anyString());

            // Act (ne doit pas lever d'exception)
            AuthResponse response = authService.register(registerRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getEmail()).isEqualTo(EMAIL);
        }

        @Test
        @DisplayName("Inscription avec code de parrainage - Doit appliquer le code")
        void register_ShouldApplyReferralCode_WhenProvided() {
            // Arrange
            registerRequest.setReferralCode("REF-CODE-123");
            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(referralService.generateReferralCode()).thenReturn("SENDME-ABC123");
            when(userRepository.save(any(User.class))).thenReturn(user);

            // Act
            authService.register(registerRequest);

            // Assert
            verify(referralService).applyReferralCode(any(User.class), eq("REF-CODE-123"));
        }

        @Test
        @DisplayName("Inscription sans code de parrainage - Ne doit pas appliquer de code")
        void register_ShouldNotApplyReferralCode_WhenNotProvided() {
            // Arrange
            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(referralService.generateReferralCode()).thenReturn("SENDME-ABC123");
            when(userRepository.save(any(User.class))).thenReturn(user);

            // Act
            authService.register(registerRequest);

            // Assert
            verify(referralService, never()).applyReferralCode(any(), anyString());
        }
    }
}
