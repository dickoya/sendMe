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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

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

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;
    private final String EMAIL = "ibrahima@sendme.com";
    private final String PASSWORD = "P@ssword123";
    private final String ENCODED_PASSWORD = "encodedPassword123";
    private final String ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
    private final String REFRESH_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.refresh...";

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
        @DisplayName("Inscription avec succès - Doit retourner AuthResponse avec tokens")
        void register_ShouldReturnAuthResponse_WhenSuccessful() {
            // Arrange
            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(userDetails);
            when(jwtService.generateToken(userDetails)).thenReturn(ACCESS_TOKEN);
            when(jwtService.generateRefreshToken(userDetails)).thenReturn(REFRESH_TOKEN);

            // Act
            AuthResponse response = authService.register(registerRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(response.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
            assertThat(response.getTokenType()).isEqualTo("Bearer");
            assertThat(response.getExpiresIn()).isEqualTo(86400000L);
            assertThat(response.getEmail()).isEqualTo(EMAIL);
            assertThat(response.getFirstName()).isEqualTo("Ibrahim");
            assertThat(response.getLastName()).isEqualTo("Dickoya");
            assertThat(response.getRole()).isEqualTo(Role.ROLE_USER.name());

            verify(userRepository).existsByEmail(EMAIL);
            verify(passwordEncoder).encode(PASSWORD);
            verify(userRepository).save(any(User.class));
            verify(userDetailsService).loadUserByUsername(EMAIL);
            verify(jwtService).generateToken(userDetails);
            verify(jwtService).generateRefreshToken(userDetails);
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
            verify(jwtService, never()).generateToken(any());
        }

        @Test
        @DisplayName("Inscription - Doit encoder le mot de passe avant sauvegarde")
        void register_ShouldEncodePassword_WhenSuccessful() {
            // Arrange
            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(userDetails);
            when(jwtService.generateToken(userDetails)).thenReturn(ACCESS_TOKEN);
            when(jwtService.generateRefreshToken(userDetails)).thenReturn(REFRESH_TOKEN);

            // Act
            authService.register(registerRequest);

            // Assert
            verify(passwordEncoder).encode(PASSWORD);
            verify(userRepository).save(argThat(savedUser ->
                    ENCODED_PASSWORD.equals(savedUser.getPasswordHash())
            ));
        }

        @Test
        @DisplayName("Inscription - Doit créer un utilisateur avec les bons champs")
        void register_ShouldCreateUserWithCorrectFields_WhenSuccessful() {
            // Arrange
            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(userDetails);
            when(jwtService.generateToken(userDetails)).thenReturn(ACCESS_TOKEN);
            when(jwtService.generateRefreshToken(userDetails)).thenReturn(REFRESH_TOKEN);

            // Act
            authService.register(registerRequest);

            // Assert
            verify(userRepository).save(argThat(savedUser -> {
                assertThat(savedUser.getFirstName()).isEqualTo("Ibrahim");
                assertThat(savedUser.getLastName()).isEqualTo("Dickoya");
                assertThat(savedUser.getEmail()).isEqualTo(EMAIL);
                assertThat(savedUser.getPhoneNumber()).isEqualTo("+224620000000");
                assertThat(savedUser.getRole()).isEqualTo(Role.ROLE_USER);
                assertThat(savedUser.getIsActive()).isTrue();
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
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(userDetails);
            when(jwtService.generateToken(userDetails)).thenReturn(ACCESS_TOKEN);
            when(jwtService.generateRefreshToken(userDetails)).thenReturn(REFRESH_TOKEN);

            // Act
            AuthResponse response = authService.register(registerRequest);

            // Assert
            assertThat(response).isNotNull();
            verify(userRepository).save(argThat(savedUser ->
                    savedUser.getPhoneNumber() == null
            ));
        }
    }

    @Nested
    @DisplayName("Tests de connexion (login)")
    class LoginTests {

        @Test
        @DisplayName("Connexion avec succès - Doit retourner AuthResponse avec tokens")
        void login_ShouldReturnAuthResponse_WhenSuccessful() {
            // Arrange
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(userDetails);
            when(jwtService.generateToken(userDetails)).thenReturn(ACCESS_TOKEN);
            when(jwtService.generateRefreshToken(userDetails)).thenReturn(REFRESH_TOKEN);

            // Act
            AuthResponse response = authService.login(loginRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(response.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
            assertThat(response.getTokenType()).isEqualTo("Bearer");
            assertThat(response.getExpiresIn()).isEqualTo(86400000L);
            assertThat(response.getEmail()).isEqualTo(EMAIL);
            assertThat(response.getFirstName()).isEqualTo("Ibrahim");
            assertThat(response.getLastName()).isEqualTo("Dickoya");
            assertThat(response.getRole()).isEqualTo(Role.ROLE_USER.name());

            verify(authenticationManager).authenticate(
                    new UsernamePasswordAuthenticationToken(EMAIL, PASSWORD)
            );
            verify(userRepository).findByEmail(EMAIL);
            verify(userDetailsService).loadUserByUsername(EMAIL);
            verify(jwtService).generateToken(userDetails);
            verify(jwtService).generateRefreshToken(userDetails);
        }

        @Test
        @DisplayName("Connexion avec email inexistant - Doit lancer RuntimeException")
        void login_ShouldThrowRuntimeException_WhenEmailNotFound() {
            // Arrange
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Utilisateur non trouve");

            verify(authenticationManager).authenticate(
                    new UsernamePasswordAuthenticationToken(EMAIL, PASSWORD)
            );
            verify(userRepository).findByEmail(EMAIL);
            verify(jwtService, never()).generateToken(any());
        }

        @Test
        @DisplayName("Connexion avec mot de passe incorrect - Doit lancer BadCredentialsException")
        void login_ShouldThrowBadCredentialsException_WhenPasswordIsWrong() {
            // Arrange
            doThrow(new BadCredentialsException("Bad credentials"))
                    .when(authenticationManager)
                    .authenticate(any(UsernamePasswordAuthenticationToken.class));

            // Act & Assert
            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(BadCredentialsException.class);

            verify(authenticationManager).authenticate(any());
            verify(userRepository, never()).findByEmail(any());
            verify(jwtService, never()).generateToken(any());
        }

        @Test
        @DisplayName("Connexion avec compte désactivé - Doit lancer RuntimeException")
        void login_ShouldThrowRuntimeException_WhenAccountIsDisabled() {
            // Arrange
            user.setIsActive(false);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

            // Act & Assert
            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Compte desactive. Contactez l'administrateur.");

            verify(authenticationManager).authenticate(
                    new UsernamePasswordAuthenticationToken(EMAIL, PASSWORD)
            );
            verify(userRepository).findByEmail(EMAIL);
            verify(jwtService, never()).generateToken(any());
        }

        @Test
        @DisplayName("Connexion avec authenticationManager qui échoue - Doit propager l'exception")
        void login_ShouldPropagateAuthenticationException_WhenAuthenticationFails() {
            // Arrange
            doThrow(new DisabledException("User is disabled"))
                    .when(authenticationManager)
                    .authenticate(any(UsernamePasswordAuthenticationToken.class));

            // Act & Assert
            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(DisabledException.class);

            verify(authenticationManager).authenticate(any());
            verify(userRepository, never()).findByEmail(any());
            verify(jwtService, never()).generateToken(any());
        }

        @Test
        @DisplayName("Connexion - Doit générer des tokens avec les bons paramètres")
        void login_ShouldGenerateTokensWithCorrectUserDetails_WhenSuccessful() {
            // Arrange
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(userDetails);
            when(jwtService.generateToken(userDetails)).thenReturn(ACCESS_TOKEN);
            when(jwtService.generateRefreshToken(userDetails)).thenReturn(REFRESH_TOKEN);

            // Act
            authService.login(loginRequest);

            // Assert
            verify(jwtService).generateToken(userDetails);
            verify(jwtService).generateRefreshToken(userDetails);
        }
    }

    @Nested
    @DisplayName("Tests de sécurité et validation")
    class SecurityValidationTests {

        @Test
        @DisplayName("Les tokens de register doivent être de type Bearer")
        void registerTokens_ShouldBeBearerType() {
            // Arrange
            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(userDetails);
            when(jwtService.generateToken(userDetails)).thenReturn(ACCESS_TOKEN);
            when(jwtService.generateRefreshToken(userDetails)).thenReturn(REFRESH_TOKEN);

            // Act
            AuthResponse response = authService.register(registerRequest);

            // Assert
            assertThat(response.getTokenType()).isEqualTo("Bearer");
        }

        @Test
        @DisplayName("Les tokens de login doivent être de type Bearer")
        void loginTokens_ShouldBeBearerType() {
            // Arrange
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(userDetails);
            when(jwtService.generateToken(userDetails)).thenReturn(ACCESS_TOKEN);
            when(jwtService.generateRefreshToken(userDetails)).thenReturn(REFRESH_TOKEN);

            // Act
            AuthResponse response = authService.login(loginRequest);

            // Assert
            assertThat(response.getTokenType()).isEqualTo("Bearer");
        }

        @Test
        @DisplayName("La date d'expiration des tokens doit être de 86400000 ms (24h)")
        void tokenExpiration_ShouldBe24Hours() {
            // Arrange
            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(userDetails);
            when(jwtService.generateToken(userDetails)).thenReturn(ACCESS_TOKEN);
            when(jwtService.generateRefreshToken(userDetails)).thenReturn(REFRESH_TOKEN);

            // Act
            AuthResponse response = authService.register(registerRequest);

            // Assert
            assertThat(response.getExpiresIn()).isEqualTo(86400000L);
        }
    }
}