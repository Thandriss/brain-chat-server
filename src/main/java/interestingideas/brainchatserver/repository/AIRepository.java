package interestingideas.brainchatserver.repository;

import interestingideas.brainchatserver.model.AI;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AIRepository extends JpaRepository<AI, Long> {
}
