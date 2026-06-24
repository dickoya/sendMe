package ibrahima.dickoya.sendme.repository;

import ibrahima.dickoya.sendme.entity.BeneficiaryChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BeneficiaryChannelRepository extends JpaRepository<BeneficiaryChannel, Long> {

    List<BeneficiaryChannel> findByBeneficiaryId(Long beneficiaryId);

    List<BeneficiaryChannel> findByBeneficiaryIdAndIsPrimaryTrue(Long beneficiaryId);
}
