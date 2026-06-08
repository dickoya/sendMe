package ibrahima.dickoya.sendme.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mobile_money_details")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MobileMoneyDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beneficiary_id", nullable = false)
    private Beneficiary beneficiary;
}
