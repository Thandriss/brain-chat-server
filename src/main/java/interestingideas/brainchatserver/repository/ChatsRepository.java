package interestingideas.brainchatserver.repository;

import interestingideas.brainchatserver.model.AI;
import interestingideas.brainchatserver.model.Chat;
import interestingideas.brainchatserver.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatsRepository extends JpaRepository<Chat, Long> {
    @Query("select u from Chat u where u.UuidChat = ?1")
    Optional<Chat> findByUuid(String uuid);

    @Query("select u from Chat u where u.adminId = ?1")
    List<Chat> findByAdminId(User adminId);

    @Query("select u from Chat u where u.status = ?1")
    List<Chat> findAllByStatus(String status);

    @Query("select u from Chat u where u.aiId = ?1")
    List<Chat> findByAIId(AI aiId);
}
