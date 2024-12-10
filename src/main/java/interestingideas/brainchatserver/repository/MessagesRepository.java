package interestingideas.brainchatserver.repository;
import interestingideas.brainchatserver.model.Chat;
import interestingideas.brainchatserver.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MessagesRepository extends JpaRepository<Message, Long> {
    @Query("select u from Message u where u.chatId = ?1")
    List<Message> findByChatId(Long chatId);
}
