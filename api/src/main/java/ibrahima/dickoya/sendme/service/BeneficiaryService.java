package ibrahima.dickoya.sendme.service;

import ibrahima.dickoya.sendme.dto.request.*;
import ibrahima.dickoya.sendme.dto.response.*;
import ibrahima.dickoya.sendme.entity.*;
import ibrahima.dickoya.sendme.enums.ChannelType;
import ibrahima.dickoya.sendme.exception.ResourceNotFoundException;
import ibrahima.dickoya.sendme.exception.TransferException;
import ibrahima.dickoya.sendme.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service gérant toute la logique métier des bénéficiaires.
 *
 * Règle métier RG-02 appliquée ici :
 * - Si channel = BANK_TRANSFER → bankDetails obligatoire (IBAN + BIC)
 * - Si channel = Mobile Money  → mobileDetails obligatoire (téléphone)
 *
 * Un bénéficiaire appartient toujours à un utilisateur précis.
 * Un utilisateur ne peut pas accéder aux bénéficiaires d'un autre utilisateur.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BeneficiaryService {

    /** Repository pour accéder aux bénéficiaires en base de données. */
    private final BeneficiaryRepository beneficiaryRepository;

    /** Repository pour accéder aux utilisateurs en base de données. */
    private final UserRepository userRepository;

    /**
     * Crée un nouveau bénéficiaire pour l'utilisateur connecté.
     *
     * Étapes :
     * 1. Vérifie que l'utilisateur existe
     * 2. Applique la règle RG-02 (validation des champs selon le canal)
     * 3. Sauvegarde le bénéficiaire avec ses détails spécifiques
     * 4. Retourne la réponse formatée
     *
     * @param request   les données du nouveau bénéficiaire
     * @param userEmail l'email de l'utilisateur connecté (extrait du JWT)
     * @return BeneficiaryResponse le bénéficiaire créé
     */
    @Transactional
    public BeneficiaryResponse create(BeneficiaryRequest request, String userEmail) {

        // 1. Récupérer l'utilisateur connecté depuis la base de données
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Utilisateur non trouve : " + userEmail));

        // 2. Appliquer la règle RG-02 : valider les champs selon le canal
        validateChannelDetails(request);

        // 3. Construire l'entité Beneficiary
        Beneficiary beneficiary = Beneficiary.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .channel(request.getChannel())
                .user(user) // associer au propriétaire
                .build();

        // 4. Si le canal est BANK_TRANSFER, créer les détails bancaires
        if (request.getChannel() == ChannelType.BANK_TRANSFER) {
            BankDetails bankDetails = BankDetails.builder()
                    .bankName(request.getBankDetails().getBankName())
                    .iban(request.getBankDetails().getIban())
                    .bic(request.getBankDetails().getBic())
                    .beneficiary(beneficiary) // lier au bénéficiaire
                    .build();
            beneficiary.setBankDetails(bankDetails);
        } else {
            // 5. Sinon, créer les détails Mobile Money
            MobileMoneyDetails mobileDetails = MobileMoneyDetails.builder()
                    .phoneNumber(request.getMobileDetails().getPhoneNumber())
                    .beneficiary(beneficiary) // lier au bénéficiaire
                    .build();
            beneficiary.setMobileMoneyDetails(mobileDetails);
        }

        // 6. Sauvegarder en base de données (cascade sauvegarde aussi les détails)
        Beneficiary saved = beneficiaryRepository.save(beneficiary);
        log.info("Beneficiaire cree : {} {} pour l'utilisateur : {}",
                saved.getFirstName(), saved.getLastName(), userEmail);

        // 7. Convertir en DTO de réponse et retourner
        return toResponse(saved);
    }

    /**
     * Récupère tous les bénéficiaires de l'utilisateur connecté.
     *
     * @param userEmail l'email de l'utilisateur connecté
     * @return liste des bénéficiaires formatés en DTO
     */
    public List<BeneficiaryResponse> findAllByUser(String userEmail) {

        // Récupérer l'utilisateur
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Utilisateur non trouve : " + userEmail));

        // Récupérer tous ses bénéficiaires et les convertir en DTO
        return beneficiaryRepository.findByUserId(user.getId())
                .stream()
                .map(this::toResponse) // convertir chaque entité en DTO
                .collect(Collectors.toList());
    }

    /**
     * Modifie un bénéficiaire existant.
     * Vérifie que le bénéficiaire appartient bien à l'utilisateur connecté
     * avant toute modification (sécurité).
     *
     * @param id        l'identifiant du bénéficiaire à modifier
     * @param request   les nouvelles données
     * @param userEmail l'email de l'utilisateur connecté
     * @return BeneficiaryResponse le bénéficiaire mis à jour
     */
    @Transactional
    public BeneficiaryResponse update(Long id, BeneficiaryRequest request, String userEmail) {

        // Récupérer l'utilisateur connecté
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Utilisateur non trouve : " + userEmail));

        // Récupérer le bénéficiaire et vérifier qu'il appartient à cet utilisateur
        Beneficiary beneficiary = beneficiaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Beneficiaire non trouve avec l'id : " + id));

        // Sécurité : vérifier que le bénéficiaire appartient bien à l'utilisateur
        if (!beneficiary.getUser().getId().equals(user.getId())) {
            throw new TransferException("Acces refuse : ce beneficiaire ne vous appartient pas");
        }

        // Valider les nouveaux détails selon le canal
        validateChannelDetails(request);

        // Mettre à jour les champs de base
        beneficiary.setFirstName(request.getFirstName());
        beneficiary.setLastName(request.getLastName());
        beneficiary.setChannel(request.getChannel());

        // Mettre à jour les détails selon le canal
        if (request.getChannel() == ChannelType.BANK_TRANSFER) {
            if (beneficiary.getBankDetails() != null) {
                // Modifier les détails bancaires existants
                beneficiary.getBankDetails().setBankName(request.getBankDetails().getBankName());
                beneficiary.getBankDetails().setIban(request.getBankDetails().getIban());
                beneficiary.getBankDetails().setBic(request.getBankDetails().getBic());
            } else {
                // Créer de nouveaux détails bancaires
                BankDetails bankDetails = BankDetails.builder()
                        .bankName(request.getBankDetails().getBankName())
                        .iban(request.getBankDetails().getIban())
                        .bic(request.getBankDetails().getBic())
                        .beneficiary(beneficiary)
                        .build();
                beneficiary.setBankDetails(bankDetails);
                beneficiary.setMobileMoneyDetails(null);
            }
        } else {
            if (beneficiary.getMobileMoneyDetails() != null) {
                // Modifier le numéro existant
                beneficiary.getMobileMoneyDetails()
                        .setPhoneNumber(request.getMobileDetails().getPhoneNumber());
            } else {
                // Créer de nouveaux détails Mobile Money
                MobileMoneyDetails mobileDetails = MobileMoneyDetails.builder()
                        .phoneNumber(request.getMobileDetails().getPhoneNumber())
                        .beneficiary(beneficiary)
                        .build();
                beneficiary.setMobileMoneyDetails(mobileDetails);
                beneficiary.setBankDetails(null);
            }
        }

        Beneficiary updated = beneficiaryRepository.save(beneficiary);
        log.info("Beneficiaire mis a jour : id={}", id);
        return toResponse(updated);
    }

    /**
     * Supprime un bénéficiaire.
     * Vérifie que le bénéficiaire appartient bien à l'utilisateur connecté.
     *
     * @param id        l'identifiant du bénéficiaire à supprimer
     * @param userEmail l'email de l'utilisateur connecté
     */
    @Transactional
    public void delete(Long id, String userEmail) {

        // Récupérer l'utilisateur connecté
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Utilisateur non trouve : " + userEmail));

        // Récupérer le bénéficiaire
        Beneficiary beneficiary = beneficiaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Beneficiaire non trouve avec l'id : " + id));

        // Sécurité : vérifier que le bénéficiaire appartient à l'utilisateur
        if (!beneficiary.getUser().getId().equals(user.getId())) {
            throw new TransferException("Acces refuse : ce beneficiaire ne vous appartient pas");
        }

        // Supprimer — la cascade supprime aussi bankDetails et mobileMoneyDetails
        beneficiaryRepository.delete(beneficiary);
        log.info("Beneficiaire supprime : id={} par {}", id, userEmail);
    }

    /**
     * Valide les champs spécifiques au canal choisi (Règle métier RG-02).
     *
     * - BANK_TRANSFER  → bankDetails ne doit pas être null
     * - Mobile Money   → mobileDetails ne doit pas être null
     *
     * @param request la requête à valider
     * @throws TransferException si les champs requis sont manquants
     */
    private void validateChannelDetails(BeneficiaryRequest request) {
        if (request.getChannel() == ChannelType.BANK_TRANSFER) {
            // Pour un virement bancaire, les détails bancaires sont obligatoires
            if (request.getBankDetails() == null) {
                throw new TransferException(
                        "Les details bancaires (IBAN, BIC) sont obligatoires pour un virement bancaire");
            }
        } else {
            // Pour tout canal Mobile Money, le numéro de téléphone est obligatoire
            if (request.getMobileDetails() == null) {
                throw new TransferException(
                        "Le numero de telephone est obligatoire pour un transfert Mobile Money");
            }
        }
    }

    /**
     * Convertit une entité Beneficiary en DTO BeneficiaryResponse.
     * Méthode utilitaire utilisée pour toutes les réponses du service.
     *
     * @param beneficiary l'entité à convertir
     * @return BeneficiaryResponse le DTO formaté
     */
    private BeneficiaryResponse toResponse(Beneficiary beneficiary) {

        BeneficiaryResponse response = BeneficiaryResponse.builder()
                .id(beneficiary.getId())
                .firstName(beneficiary.getFirstName())
                .lastName(beneficiary.getLastName())
                .channel(beneficiary.getChannel())
                .createdAt(beneficiary.getCreatedAt())
                .build();

        // Ajouter les détails bancaires si présents
        if (beneficiary.getBankDetails() != null) {
            response.setBankDetails(BankDetailsResponse.builder()
                    .id(beneficiary.getBankDetails().getId())
                    .bankName(beneficiary.getBankDetails().getBankName())
                    .iban(beneficiary.getBankDetails().getIban())
                    .bic(beneficiary.getBankDetails().getBic())
                    .build());
        }

        // Ajouter les détails Mobile Money si présents
        if (beneficiary.getMobileMoneyDetails() != null) {
            response.setMobileDetails(MobileMoneyDetailsResponse.builder()
                    .id(beneficiary.getMobileMoneyDetails().getId())
                    .phoneNumber(beneficiary.getMobileMoneyDetails().getPhoneNumber())
                    .build());
        }

        return response;
    }
}