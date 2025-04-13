package interestingideas.brainchatserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import interestingideas.brainchatserver.model.Chat;
import interestingideas.brainchatserver.repository.ChatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;
@Slf4j
@Component
@RequiredArgsConstructor
public class InactivityChecker {
    private final ChatService chatService;
    private final ChatSessionManager chatSessionManager;
    private final AIService aiService;
    private final MessageProducerService messageProducerService;


    @Scheduled(fixedDelay = 10000)
    public void checkInactiveChats() {
        LocalDateTime now = LocalDateTime.now();

        if (!chatSessionManager.getAllChatActivities().isEmpty()) {
            chatSessionManager.getAllChatActivities().forEach((chatId, lastMessageTime) -> {
                log.info("Chat ID: {}, Last Activity: {}", chatId, lastMessageTime);
                int threshold = chatSessionManager.getThreshold(chatId);
                if (Duration.between(lastMessageTime, now).toSeconds() >= threshold) {
                    Chat chat = chatService.findByIdCached(chatId);
                    Long ai_id = chat.getAiId().getId();
                    String aiMessage = aiService.generateAIMessage(ai_id, chat.getUuidChat());
                    try {
                        messageProducerService.sendAIMessage(aiMessage, chat.getUuidChat(), ai_id);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }
}
