package ibrahima.dickoya.sendme.repository;

import ibrahima.dickoya.sendme.entity.Beneficiary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {
    List<Beneficiary> findByUserId(Long userId);

    Page<Beneficiary> findByUserId(Long userId, Pageable pageable);
}
