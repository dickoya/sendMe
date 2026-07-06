package ibrahima.dickoya.sendme.service;

import ibrahima.dickoya.sendme.dto.request.*;
import ibrahima.dickoya.sendme.dto.response.BeneficiaryResponse;
import ibrahima.dickoya.sendme.entity.*;
import ibrahima.dickoya.sendme.enums.ChannelType;
import ibrahima.dickoya.sendme.exception.ResourceNotFoundException;
import ibrahima.dickoya.sendme.exception.TransferException;
import ibrahima.dickoya.sendme.repository.BeneficiaryRepository;
import ibrahima.dickoya.sendme.repository.UserRepository;
import ibrahima.dickoya.sendme.utils.MessageUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour BeneficiaryService")
class BeneficiaryServiceTest {

        @Mock
        private BeneficiaryRepository beneficiaryRepository;

        @Mock
        private UserRepository userRepository;

        @Mock
        private MessageUtils messageUtils;

        @InjectMocks
        private BeneficiaryService beneficiaryService;

        private User user;
        private final String USER_EMAIL = "ibrahima@sendme.com";
        private final Long USER_ID = 1L;
        private final Long BENEFICIARY_ID = 100L;

        @BeforeEach
        void setUp() {
                user = User.builder()
                                .id(USER_ID)
                                .firstName("Ibrahim")
                                .lastName("Dickoya")
                                .email(USER_EMAIL)
                                .build();

                // Configuration par défaut de MessageUtils pour tous les tests (lenient car pas
                // utilisé par tous les tests)
                lenient().when(messageUtils.get(eq("error.user.notfound"), any())).thenAnswer(invocation -> {
                        Object[] args = invocation.getArguments();
                        return "Utilisateur non trouve : " + args[1];
                });
                lenient().when(messageUtils.get(eq("error.beneficiary.notfound"), any())).thenAnswer(invocation -> {
                        Object[] args = invocation.getArguments();
                        return "Beneficiaire non trouve avec l'id : " + args[1];
                });
                lenient().when(messageUtils.get(eq("error.beneficiary.access.denied")))
                                .thenReturn("Acces refuse : ce beneficiaire ne vous appartient pas");
                lenient().when(messageUtils.get(eq("error.channel.required")))
                                .thenReturn("Au moins un canal de reception est requis");
                lenient().when(messageUtils.get(eq("error.channel.primary.required")))
                                .thenReturn("Un canal doit etre defini comme principal (isPrimary=true)");
                lenient().when(messageUtils.get(eq("error.channel.bankname.required")))
                                .thenReturn("Le nom de la banque est obligatoire pour un virement bancaire");
                lenient().when(messageUtils.get(eq("error.channel.iban.required")))
                                .thenReturn("L'IBAN est obligatoire pour un virement bancaire");
                lenient().when(messageUtils.get(eq("error.channel.bic.required")))
                                .thenReturn("Le BIC est obligatoire pour un virement bancaire");
                lenient().when(messageUtils.get(eq("error.channel.phone.required"), any())).thenAnswer(invocation -> {
                        Object[] args = invocation.getArguments();
                        return "Le numero de telephone est obligatoire pour le canal " + args[1];
                });
        }

        @Nested
        @DisplayName("Tests de création (create)")
        class CreateTests {

                @Test
                @DisplayName("Création avec succès - Canal BANK_TRANSFER")
                void create_ShouldReturnBeneficiaryResponse_WhenBankTransfer() {
                        // Arrange
                        BeneficiaryChannelRequest channelRequest = BeneficiaryChannelRequest.builder()
                                        .channel(ChannelType.BANK_TRANSFER)
                                        .bankName("Societe Generale")
                                        .iban("FR7630006008011234567890189")
                                        .bic("BNPAFRPPXXX")
                                        .isPrimary(true)
                                        .build();

                        BeneficiaryRequest request = BeneficiaryRequest.builder()
                                        .firstName("Fatou")
                                        .lastName("Kone")
                                        .channels(List.of(channelRequest))
                                        .build();

                        Beneficiary beneficiary = createBankBeneficiary();

                        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
                        when(beneficiaryRepository.save(any(Beneficiary.class))).thenReturn(beneficiary);

                        // Act
                        BeneficiaryResponse response = beneficiaryService.create(request, USER_EMAIL);

                        // Assert
                        assertThat(response).isNotNull();
                        assertThat(response.getFirstName()).isEqualTo("Fatou");
                        assertThat(response.getLastName()).isEqualTo("Kone");
                        assertThat(response.getChannels()).isNotEmpty();
                        assertThat(response.getChannels().get(0).getBankName()).isEqualTo("Societe Generale");

                        verify(userRepository).findByEmail(USER_EMAIL);
                        verify(beneficiaryRepository).save(any(Beneficiary.class));
                }

                @Test
                @DisplayName("Création avec succès - Canal Mobile Money")
                void create_ShouldReturnBeneficiaryResponse_WhenMobileMoney() {
                        // Arrange
                        BeneficiaryChannelRequest channelRequest = BeneficiaryChannelRequest.builder()
                                        .channel(ChannelType.ORANGE_MONEY)
                                        .phoneNumber("+224620000001")
                                        .countryCode("GIN")
                                        .isPrimary(true)
                                        .build();

                        BeneficiaryRequest request = BeneficiaryRequest.builder()
                                        .firstName("Amadou")
                                        .lastName("Diallo")
                                        .channels(List.of(channelRequest))
                                        .build();

                        Beneficiary beneficiary = createMobileBeneficiary();

                        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
                        when(beneficiaryRepository.save(any(Beneficiary.class))).thenReturn(beneficiary);

                        // Act
                        BeneficiaryResponse response = beneficiaryService.create(request, USER_EMAIL);

                        // Assert
                        assertThat(response).isNotNull();
                        assertThat(response.getFirstName()).isEqualTo("Amadou");
                        assertThat(response.getChannels()).isNotEmpty();
                        assertThat(response.getChannels().get(0).getPhoneNumber()).isEqualTo("+224620000001");

                        verify(userRepository).findByEmail(USER_EMAIL);
                        verify(beneficiaryRepository).save(any(Beneficiary.class));
                }

                @Test
                @DisplayName("Création avec utilisateur inexistant - Doit lancer ResourceNotFoundException")
                void create_ShouldThrowResourceNotFoundException_WhenUserNotFound() {
                        // Arrange
                        BeneficiaryRequest request = BeneficiaryRequest.builder()
                                        .firstName("Fatou")
                                        .lastName("Kone")
                                        .channels(null)
                                        .build();

                        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

                        // Act & Assert
                        assertThatThrownBy(() -> beneficiaryService.create(request, USER_EMAIL))
                                        .isInstanceOf(ResourceNotFoundException.class)
                                        .hasMessage("Utilisateur non trouve : " + USER_EMAIL);

                        verify(beneficiaryRepository, never()).save(any());
                }

                @Test
                @DisplayName("Création BANK_TRANSFER sans channels - Doit lancer TransferException")
                void create_ShouldThrowTransferException_WhenChannelsMissing() {
                        // Arrange
                        BeneficiaryRequest request = BeneficiaryRequest.builder()
                                        .firstName("Fatou")
                                        .lastName("Kone")
                                        .channels(null)
                                        .build();

                        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));

                        // Act & Assert
                        assertThatThrownBy(() -> beneficiaryService.create(request, USER_EMAIL))
                                        .isInstanceOf(TransferException.class)
                                        .hasMessage("Au moins un canal de reception est requis");

                        verify(beneficiaryRepository, never()).save(any());
                }

                @Test
                @DisplayName("Création Mobile Money sans channels - Doit lancer TransferException")
                void create_ShouldThrowTransferException_WhenMobileChannelsMissing() {
                        // Arrange
                        BeneficiaryRequest request = BeneficiaryRequest.builder()
                                        .firstName("Amadou")
                                        .lastName("Diallo")
                                        .channels(null)
                                        .build();

                        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));

                        // Act & Assert
                        assertThatThrownBy(() -> beneficiaryService.create(request, USER_EMAIL))
                                        .isInstanceOf(TransferException.class)
                                        .hasMessage("Au moins un canal de reception est requis");

                        verify(beneficiaryRepository, never()).save(any());
                }
        }

        @Nested
        @DisplayName("Tests de récupération (findAllByUser)")
        class FindAllTests {

                private final int PAGE = 0;
                private final int SIZE = 20;

                @Test
                @DisplayName("Récupérer tous les bénéficiaires - Succès")
                void findAllByUser_ShouldReturnPageOfBeneficiaries_WhenSuccessful() {
                        // Arrange
                        List<Beneficiary> beneficiaries = List.of(
                                        createBankBeneficiary(),
                                        createMobileBeneficiary());
                        Page<Beneficiary> beneficiaryPage = new PageImpl<>(beneficiaries);

                        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
                        when(beneficiaryRepository.findByUserId(eq(USER_ID), any(Pageable.class)))
                                        .thenReturn(beneficiaryPage);

                        // Act
                        Page<BeneficiaryResponse> responses = beneficiaryService
                                        .findAllByUser(USER_EMAIL, PAGE, SIZE);

                        // Assert
                        assertThat(responses).hasSize(2);
                        assertThat(responses.getContent().get(0).getFirstName()).isEqualTo("Fatou");
                        assertThat(responses.getContent().get(1).getChannels()).isNotEmpty();

                        verify(userRepository).findByEmail(USER_EMAIL);
                        verify(beneficiaryRepository).findByUserId(eq(USER_ID), any(Pageable.class));
                }

                @Test
                @DisplayName("Récupérer bénéficiaires - Page vide")
                void findAllByUser_ShouldReturnEmptyPage_WhenNoBeneficiaries() {
                        // Arrange
                        Page<Beneficiary> emptyPage = Page.empty();

                        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
                        when(beneficiaryRepository.findByUserId(eq(USER_ID), any(Pageable.class)))
                                        .thenReturn(emptyPage);

                        // Act
                        Page<BeneficiaryResponse> responses = beneficiaryService
                                        .findAllByUser(USER_EMAIL, PAGE, SIZE);

                        // Assert
                        assertThat(responses).isEmpty();

                        verify(userRepository).findByEmail(USER_EMAIL);
                        verify(beneficiaryRepository).findByUserId(eq(USER_ID), any(Pageable.class));
                }

                @Test
                @DisplayName("Récupérer bénéficiaires - Utilisateur inexistant")
                void findAllByUser_ShouldThrowResourceNotFoundException_WhenUserNotFound() {
                        // Arrange
                        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

                        // Act & Assert
                        assertThatThrownBy(() -> beneficiaryService.findAllByUser(USER_EMAIL, PAGE, SIZE))
                                        .isInstanceOf(ResourceNotFoundException.class)
                                        .hasMessage("Utilisateur non trouve : " + USER_EMAIL);

                        verify(beneficiaryRepository, never()).findByUserId(any(), any());
                }
        }

        @Nested
        @DisplayName("Tests de mise à jour (update)")
        class UpdateTests {

                @Test
                @DisplayName("Mise à jour avec succès - BANK_TRANSFER")
                void update_ShouldUpdateBeneficiary_WhenSuccessful() {
                        // Arrange
                        BeneficiaryChannelRequest channelRequest = BeneficiaryChannelRequest.builder()
                                        .channel(ChannelType.BANK_TRANSFER)
                                        .bankName("BNP Paribas")
                                        .iban("FR7612345678901234567890123")
                                        .bic("BNPAFRPPXXX")
                                        .isPrimary(true)
                                        .build();

                        BeneficiaryRequest request = BeneficiaryRequest.builder()
                                        .firstName("Fatou")
                                        .lastName("Traore")
                                        .channels(List.of(channelRequest))
                                        .build();

                        Beneficiary existingBeneficiary = createBankBeneficiary();
                        existingBeneficiary.setId(BENEFICIARY_ID);

                        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
                        when(beneficiaryRepository.findById(BENEFICIARY_ID))
                                        .thenReturn(Optional.of(existingBeneficiary));
                        when(beneficiaryRepository.save(any(Beneficiary.class))).thenReturn(existingBeneficiary);

                        // Act
                        BeneficiaryResponse response = beneficiaryService.update(BENEFICIARY_ID, request, USER_EMAIL);

                        // Assert
                        assertThat(response).isNotNull();
                        assertThat(response.getFirstName()).isEqualTo("Fatou");
                        assertThat(response.getLastName()).isEqualTo("Traore");
                        assertThat(response.getChannels().get(0).getBankName()).isEqualTo("BNP Paribas");

                        verify(beneficiaryRepository).save(any(Beneficiary.class));
                }

                @Test
                @DisplayName("Mise à jour bénéficiaire inexistant - Doit lancer ResourceNotFoundException")
                void update_ShouldThrowResourceNotFoundException_WhenBeneficiaryNotFound() {
                        // Arrange
                        BeneficiaryRequest request = BeneficiaryRequest.builder()
                                        .firstName("Fatou")
                                        .lastName("Kone")
                                        .channels(null)
                                        .build();

                        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
                        when(beneficiaryRepository.findById(BENEFICIARY_ID)).thenReturn(Optional.empty());

                        // Act & Assert
                        assertThatThrownBy(() -> beneficiaryService.update(BENEFICIARY_ID, request, USER_EMAIL))
                                        .isInstanceOf(ResourceNotFoundException.class)
                                        .hasMessage("Beneficiaire non trouve avec l'id : " + BENEFICIARY_ID);

                        verify(beneficiaryRepository, never()).save(any());
                }

                @Test
                @DisplayName("Mise à jour bénéficiaire d'un autre utilisateur - Doit lancer TransferException")
                void update_ShouldThrowTransferException_WhenBeneficiaryBelongsToAnotherUser() {
                        // Arrange
                        User otherUser = User.builder().id(999L).email("other@email.com").build();
                        BeneficiaryRequest request = BeneficiaryRequest.builder()
                                        .firstName("Fatou")
                                        .lastName("Kone")
                                        .channels(null)
                                        .build();

                        Beneficiary beneficiaryOfOtherUser = createBankBeneficiary();
                        beneficiaryOfOtherUser.setUser(otherUser);

                        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
                        when(beneficiaryRepository.findById(BENEFICIARY_ID))
                                        .thenReturn(Optional.of(beneficiaryOfOtherUser));

                        // Act & Assert
                        assertThatThrownBy(() -> beneficiaryService.update(BENEFICIARY_ID, request, USER_EMAIL))
                                        .isInstanceOf(TransferException.class)
                                        .hasMessage("Acces refuse : ce beneficiaire ne vous appartient pas");

                        verify(beneficiaryRepository, never()).save(any());
                }
        }

        @Nested
        @DisplayName("Tests de suppression (delete)")
        class DeleteTests {

                @Test
                @DisplayName("Suppression avec succès")
                void delete_ShouldDeleteBeneficiary_WhenSuccessful() {
                        // Arrange
                        Beneficiary beneficiary = createBankBeneficiary();
                        beneficiary.setId(BENEFICIARY_ID);

                        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
                        when(beneficiaryRepository.findById(BENEFICIARY_ID)).thenReturn(Optional.of(beneficiary));

                        // Act
                        beneficiaryService.delete(BENEFICIARY_ID, USER_EMAIL);

                        // Assert
                        verify(beneficiaryRepository).delete(beneficiary);
                }

                @Test
                @DisplayName("Suppression bénéficiaire inexistant - Doit lancer ResourceNotFoundException")
                void delete_ShouldThrowResourceNotFoundException_WhenBeneficiaryNotFound() {
                        // Arrange
                        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
                        when(beneficiaryRepository.findById(BENEFICIARY_ID)).thenReturn(Optional.empty());

                        // Act & Assert
                        assertThatThrownBy(() -> beneficiaryService.delete(BENEFICIARY_ID, USER_EMAIL))
                                        .isInstanceOf(ResourceNotFoundException.class)
                                        .hasMessage("Beneficiaire non trouve avec l'id : " + BENEFICIARY_ID);

                        verify(beneficiaryRepository, never()).delete(any());
                }

                @Test
                @DisplayName("Suppression bénéficiaire d'un autre utilisateur - Doit lancer TransferException")
                void delete_ShouldThrowTransferException_WhenBeneficiaryBelongsToAnotherUser() {
                        // Arrange
                        User otherUser = User.builder().id(999L).email("other@email.com").build();
                        Beneficiary beneficiaryOfOtherUser = createBankBeneficiary();
                        beneficiaryOfOtherUser.setUser(otherUser);

                        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
                        when(beneficiaryRepository.findById(BENEFICIARY_ID))
                                        .thenReturn(Optional.of(beneficiaryOfOtherUser));

                        // Act & Assert
                        assertThatThrownBy(() -> beneficiaryService.delete(BENEFICIARY_ID, USER_EMAIL))
                                        .isInstanceOf(TransferException.class)
                                        .hasMessage("Acces refuse : ce beneficiaire ne vous appartient pas");

                        verify(beneficiaryRepository, never()).delete(any());
                }
        }

        // Méthodes utilitaires pour créer des bénéficiaires de test

        private Beneficiary createBankBeneficiary() {
                BeneficiaryChannel channel = BeneficiaryChannel.builder()
                                .id(1L)
                                .channel(ChannelType.BANK_TRANSFER)
                                .bankName("Societe Generale")
                                .iban("FR7630006008011234567890189")
                                .bic("BNPAFRPPXXX")
                                .isPrimary(true)
                                .build();

                Beneficiary beneficiary = Beneficiary.builder()
                                .id(1L)
                                .firstName("Fatou")
                                .lastName("Kone")
                                .user(user)
                                .channels(new ArrayList<>(List.of(channel)))
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();

                channel.setBeneficiary(beneficiary);
                return beneficiary;
        }

        private Beneficiary createMobileBeneficiary() {
                BeneficiaryChannel channel = BeneficiaryChannel.builder()
                                .id(2L)
                                .channel(ChannelType.ORANGE_MONEY)
                                .phoneNumber("+224620000001")
                                .countryCode("GIN")
                                .isPrimary(true)
                                .build();

                Beneficiary beneficiary = Beneficiary.builder()
                                .id(2L)
                                .firstName("Amadou")
                                .lastName("Diallo")
                                .user(user)
                                .channels(new ArrayList<>(List.of(channel)))
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();

                channel.setBeneficiary(beneficiary);
                return beneficiary;
        }
}
