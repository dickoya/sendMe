package dickoya.sendMe.repositories;

import dickoya.sendMe.models.Beneficiary;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {
    List<Beneficiary> findByUserId(Long userId);
}
