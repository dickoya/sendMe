package ibrahima.dickoya.sendme.dto.response;

import lombok.*;

import java.util.List;

/**
 * DTO de réponse pour l'évolution des transferts de l'utilisateur.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferEvolutionResponse {

    private List<MonthlyTransfer> monthly;
    private List<YearlyTransfer> yearly;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthlyTransfer {
        private String month; // "2025-01"
        private Long count;
        private Double totalAmount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class YearlyTransfer {
        private int year;
        private Long count;
        private Double totalAmount;
    }
}
