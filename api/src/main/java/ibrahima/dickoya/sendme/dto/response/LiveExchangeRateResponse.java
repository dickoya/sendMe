package ibrahima.dickoya.sendme.dto.response;

import lombok.*;

import java.util.List;

/**
 * DTO de réponse pour les taux de change en direct
 * affichés sur le tableau de bord utilisateur.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveExchangeRateResponse {

    private String from;
    private String to;
    private Double rate;
    private Double rawRate;
    private String timestamp;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExchangeRateHistoryPoint {
        private String date;
        private Double rate;
    }
}
