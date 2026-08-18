package ibrahima.dickoya.sendme.repository;

import ibrahima.dickoya.sendme.entity.Beneficiary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {
    List<Beneficiary> findByUserId(Long userId);

    // @EntityGraph : charge les canaux (channels) avec le bénéficiaire pour
    // éviter la LazyInitializationException hors transaction (open-in-view=false).
    @EntityGraph(attributePaths = "channels")
    Page<Beneficiary> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = "channels")
    Optional<Beneficiary> findById(Long id); 
}
