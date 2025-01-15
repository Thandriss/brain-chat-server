package interestingideas.brainchatserver.service;

import interestingideas.brainchatserver.model.Chat;
import interestingideas.brainchatserver.repository.ChatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
@Slf4j
@RequiredArgsConstructor
@Service
public class ChatSessionManager {
    private final ChatsRepository chatsRepository;
    private final ConcurrentHashMap<Long, LocalDateTime> sessionMap = new ConcurrentHashMap<>();

    public void startSession(Long chatId, LocalDateTime endTime) {
        sessionMap.put(chatId, endTime);
    }

    public boolean isSessionActive(Long chatId) {
        Chat chat = chatsRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Chat not found"));
        return chat.getStatus().name().equals("1");
    }

    public void endSession(Long chatId) {
        sessionMap.remove(chatId);
    }
    public ConcurrentHashMap<Long, LocalDateTime> getAllChatActivities() {
        return sessionMap;
    }
}
