package ibrahima.dickoya.sendme.repository;

import ibrahima.dickoya.sendme.entity.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {
    List<LoginHistory> findByUserIdOrderByLoginTimeDesc(Long userId);

    List<LoginHistory> findTop10ByUserIdOrderByLoginTimeDesc(Long userId);
}
