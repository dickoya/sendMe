package ibrahima.dickoya.sendme.service;

import ibrahima.dickoya.sendme.dto.request.*;
import ibrahima.dickoya.sendme.dto.response.*;
import ibrahima.dickoya.sendme.entity.*;
import ibrahima.dickoya.sendme.enums.ChannelType;
import ibrahima.dickoya.sendme.exception.ResourceNotFoundException;
import ibrahima.dickoya.sendme.exception.TransferException;
import ibrahima.dickoya.sendme.repository.*;
import ibrahima.dickoya.sendme.utils.CurrencyByCountry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service gérant toute la logique métier des bénéficiaires.
 *
 * Un bénéficiaire peut avoir 1 ou plusieurs canaux de réception.
 * Chaque canal a ses propres détails selon le type :
 * - BANK_TRANSFER → bankName, iban, bic
 * - Mobile Money → phoneNumber, countryCode
 *
 * La devise de réception est déterminée par le code pays (countryCode)
 * via CurrencyByCountry.
 *
 * Un bénéficiaire appartient toujours à un utilisateur précis.
 * Un utilisateur ne peut pas accéder aux bénéficiaires d'un autre utilisateur.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final UserRepository userRepository;

    /**
     * Crée un nouveau bénéficiaire pour l'utilisateur connecté.
     * Le bénéficiaire peut avoir 1 ou plusieurs canaux de réception.
     */
    @Transactional
    public BeneficiaryResponse create(BeneficiaryRequest request, String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Utilisateur non trouve : " + userEmail));

        // Valider les canaux
        validateChannels(request.getChannels());

        // Construire l'entité Beneficiary
        Beneficiary beneficiary = Beneficiary.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .channel(request.getChannel())
                .user(user)
                .channels(new ArrayList<>())
                .build();

        // Créer les canaux de réception
        for (BeneficiaryChannelRequest channelReq : request.getChannels()) {
            BeneficiaryChannel channel = buildChannel(channelReq, beneficiary);
            beneficiary.getChannels().add(channel);
        }

        Beneficiary saved = beneficiaryRepository.save(beneficiary);
        log.info("Beneficiaire cree : {} {} avec {} canal(aux) pour l'utilisateur : {}",
                saved.getFirstName(), saved.getLastName(), saved.getChannels().size(), userEmail);

        return toResponse(saved);
    }

    public Page<BeneficiaryResponse> findAllByUser(String userEmail, int page, int size) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Utilisateur non trouve : " + userEmail));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return beneficiaryRepository.findByUserId(user.getId(), pageable)
                .map(this::toResponse);
    }

    @Transactional
    public BeneficiaryResponse update(Long id, BeneficiaryRequest request, String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Utilisateur non trouve : " + userEmail));

        Beneficiary beneficiary = beneficiaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Beneficiaire non trouve avec l'id : " + id));

        if (!beneficiary.getUser().getId().equals(user.getId())) {
            throw new TransferException("Acces refuse : ce beneficiaire ne vous appartient pas");
        }

        // Valider les canaux
        validateChannels(request.getChannels());

        // Mettre à jour les champs de base
        beneficiary.setFirstName(request.getFirstName());
        beneficiary.setLastName(request.getLastName());
        beneficiary.setChannel(request.getChannel());

        // Remplacer les canaux existants par les nouveaux
        beneficiary.getChannels().clear();
        for (BeneficiaryChannelRequest channelReq : request.getChannels()) {
            BeneficiaryChannel channel = buildChannel(channelReq, beneficiary);
            beneficiary.getChannels().add(channel);
        }

        Beneficiary updated = beneficiaryRepository.save(beneficiary);
        log.info("Beneficiaire mis a jour : id={} avec {} canal(aux)", id, updated.getChannels().size());
        return toResponse(updated);
    }

    @Transactional
    public void delete(Long id, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Utilisateur non trouve : " + userEmail));

        Beneficiary beneficiary = beneficiaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Beneficiaire non trouve avec l'id : " + id));

        if (!beneficiary.getUser().getId().equals(user.getId())) {
            throw new TransferException("Acces refuse : ce beneficiaire ne vous appartient pas");
        }

        beneficiaryRepository.delete(beneficiary);
        log.info("Beneficiaire supprime : id={} par {}", id, userEmail);
    }

    /**
     * Valide les canaux de réception.
     * - BANK_TRANSFER : bankName, iban, bic obligatoires
     * - Mobile Money : phoneNumber obligatoire
     * - Au moins un canal doit être marqué comme principal (isPrimary)
     */
    private void validateChannels(List<BeneficiaryChannelRequest> channels) {
        if (channels == null || channels.isEmpty()) {
            throw new TransferException("Au moins un canal de reception est requis");
        }

        boolean hasPrimary = false;
        for (BeneficiaryChannelRequest ch : channels) {
            if (ch.getChannel() == ChannelType.BANK_TRANSFER) {
                if (ch.getBankName() == null || ch.getBankName().isBlank()) {
                    throw new TransferException("Le nom de la banque est obligatoire pour un virement bancaire");
                }
                if (ch.getIban() == null || ch.getIban().isBlank()) {
                    throw new TransferException("L'IBAN est obligatoire pour un virement bancaire");
                }
                if (ch.getBic() == null || ch.getBic().isBlank()) {
                    throw new TransferException("Le BIC est obligatoire pour un virement bancaire");
                }
            } else {
                if (ch.getPhoneNumber() == null || ch.getPhoneNumber().isBlank()) {
                    throw new TransferException(
                            "Le numero de telephone est obligatoire pour le canal " + ch.getChannel());
                }
            }
            if (ch.isPrimary()) {
                hasPrimary = true;
            }
        }

        if (!hasPrimary) {
            throw new TransferException("Un canal doit etre defini comme principal (isPrimary=true)");
        }
    }

    /**
     * Construit une entité BeneficiaryChannel à partir d'une requête.
     */
    private BeneficiaryChannel buildChannel(BeneficiaryChannelRequest req, Beneficiary beneficiary) {
        return BeneficiaryChannel.builder()
                .channel(req.getChannel())
                .phoneNumber(req.getPhoneNumber())
                .countryCode(req.getCountryCode())
                .bankName(req.getBankName())
                .iban(req.getIban())
                .bic(req.getBic())
                .isPrimary(req.isPrimary())
                .beneficiary(beneficiary)
                .build();
    }

    /**
     * Convertit une entité Beneficiary en DTO BeneficiaryResponse.
     */
    private BeneficiaryResponse toResponse(Beneficiary beneficiary) {

        List<BeneficiaryChannelResponse> channelResponses = beneficiary.getChannels().stream()
                .map(this::toChannelResponse)
                .collect(Collectors.toList());

        return BeneficiaryResponse.builder()
                .id(beneficiary.getId())
                .firstName(beneficiary.getFirstName())
                .lastName(beneficiary.getLastName())
                .channel(beneficiary.getChannel())
                .channels(channelResponses)
                .createdAt(beneficiary.getCreatedAt())
                .build();
    }

    /**
     * Convertit une entité BeneficiaryChannel en DTO BeneficiaryChannelResponse.
     * L'IBAN est masqué pour la sécurité.
     */
    private BeneficiaryChannelResponse toChannelResponse(BeneficiaryChannel channel) {
        String currency = channel.getCountryCode() != null
                ? CurrencyByCountry.getCurrency(channel.getCountryCode())
                : CurrencyByCountry.getDefaultCurrencyForChannel(channel.getChannel().name());

        return BeneficiaryChannelResponse.builder()
                .id(channel.getId())
                .channel(channel.getChannel())
                .phoneNumber(channel.getPhoneNumber())
                .countryCode(channel.getCountryCode())
                .bankName(channel.getBankName())
                .iban(maskIban(channel.getIban()))
                .bic(channel.getBic())
                .isPrimary(channel.getIsPrimary())
                .currency(currency)
                .build();
    }

    /**
     * Masque un IBAN pour l'affichage : FR76 **** **** 0189
     */
    private String maskIban(String iban) {
        if (iban == null || iban.length() < 8)
            return null;
        return iban.substring(0, 4) + " **** **** " + iban.substring(iban.length() - 4);
    }
}
