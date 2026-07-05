package ibrahima.dickoya.sendme.service;

import ibrahima.dickoya.sendme.dto.request.PaymentChannelRequest;
import ibrahima.dickoya.sendme.dto.response.PaymentChannelResponse;
import ibrahima.dickoya.sendme.entity.PaymentChannel;
import ibrahima.dickoya.sendme.exception.ResourceNotFoundException;
import ibrahima.dickoya.sendme.repository.PaymentChannelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service de gestion dynamique des canaux de paiement.
 *
 * Permet à l'administrateur de :
 * - Lister tous les canaux (actifs et inactifs)
 * - Ajouter un nouveau canal
 * - Activer/désactiver un canal existant
 *
 * Les utilisateurs ne voient que les canaux actifs.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentChannelService {

    private final PaymentChannelRepository paymentChannelRepository;

    /**
     * Récupère tous les canaux de paiement (pour l'admin).
     */
    public List<PaymentChannelResponse> getAllChannels() {
        return paymentChannelRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupère uniquement les canaux actifs (pour les utilisateurs).
     */
    public List<PaymentChannelResponse> getActiveChannels() {
        return paymentChannelRepository.findByIsActiveTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Ajoute un nouveau canal de paiement.
     *
     * @param request les informations du nouveau canal
     * @return le canal créé
     * @throws IllegalArgumentException si le type de canal existe déjà
     */
    @Transactional
    public PaymentChannelResponse addChannel(PaymentChannelRequest request) {
        if (paymentChannelRepository.existsByChannelType(request.getChannelType())) {
            throw new IllegalArgumentException(
                    "Un canal avec le type '" + request.getChannelType() + "' existe déjà");
        }

        PaymentChannel channel = PaymentChannel.builder()
                .channelType(request.getChannelType())
                .displayName(request.getDisplayName())
                .icon(request.getIcon())
                .isActive(true) // Nouveau canal actif par défaut
                .build();

        PaymentChannel saved = paymentChannelRepository.save(channel);
        log.info("[PAYMENT_CHANNEL] Nouveau canal ajouté : {} ({})", saved.getDisplayName(), saved.getChannelType());

        return toResponse(saved);
    }

    /**
     * Active ou désactive un canal de paiement.
     *
     * @param id identifiant du canal
     * @return le canal mis à jour
     */
    @Transactional
    public PaymentChannelResponse toggleChannel(Long id) {
        PaymentChannel channel = paymentChannelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Canal de paiement non trouvé : " + id));

        boolean newStatus = !channel.isActive();
        channel.setActive(newStatus);
        paymentChannelRepository.save(channel);

        log.info("[PAYMENT_CHANNEL] Canal {} : statut changé à {}",
                channel.getChannelType(), newStatus ? "ACTIF" : "INACTIF");

        return toResponse(channel);
    }

    /**
     * Vérifie si un type de canal est actif.
     * Utilisé par TransferService pour valider les transferts.
     */
    public boolean isChannelActive(String channelType) {
        return paymentChannelRepository.findByIsActiveTrue()
                .stream()
                .anyMatch(ch -> ch.getChannelType().equals(channelType));
    }

    /**
     * Convertit une entité PaymentChannel en DTO.
     */
    private PaymentChannelResponse toResponse(PaymentChannel channel) {
        return PaymentChannelResponse.builder()
                .id(channel.getId())
                .channelType(channel.getChannelType())
                .displayName(channel.getDisplayName())
                .icon(channel.getIcon())
                .isActive(channel.isActive())
                .build();
    }
}
