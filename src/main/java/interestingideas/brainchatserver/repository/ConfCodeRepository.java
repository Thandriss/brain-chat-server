package interestingideas.brainchatserver.repository;

import interestingideas.brainchatserver.model.ConfirmationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ConfCodeRepository extends JpaRepository<ConfirmationCode, Long> {
    Optional<ConfirmationCode> findByCodeAndExpiredDateTimeAfter(String code, LocalDateTime now);
}
