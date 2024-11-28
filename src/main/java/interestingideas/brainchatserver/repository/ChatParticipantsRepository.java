package interestingideas.brainchatserver.repository;

import interestingideas.brainchatserver.model.Chat;
import interestingideas.brainchatserver.model.ChatParticipants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatParticipantsRepository extends JpaRepository<ChatParticipants, Long> {
    @Query("select u from ChatParticipants u where u.chatId = ?1")
    List<ChatParticipants> findByChatId(Long chatId);
    @Query("select u from ChatParticipants u where u.userId = ?1")
    List<ChatParticipants> findChatParticipantsById(Long userId);
}