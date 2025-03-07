package interestingideas.brainchatserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import interestingideas.brainchatserver.model.Chat;
import interestingideas.brainchatserver.repository.ChatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class InactivityChecker {
    private final ChatsRepository chatsRepository;
    private final ChatSessionManager chatSessionManager;
    private final AIService aiService;
    private final MessageProducerService messageProducerService;

    @Scheduled(fixedRate = 10000)
    public void checkInactiveChats() {
        System.out.println("run checker");
        LocalDateTime now = LocalDateTime.now();
        chatSessionManager.getAllChatActivities().forEach((chatId, lastMessageTime) -> {
            System.out.println("chatId " + chatId);
            System.out.println("Duration "+Duration.between(lastMessageTime, now).toSeconds());
            System.out.println("Is chat active " + chatSessionManager.isSessionActive(chatId));
            int min = 15;
            int max = 35;
            Random rand = new Random();
            int seconds = rand.nextInt(max - min + 1) + min;
            System.out.println("seconds " + seconds);
            if (Duration.between(lastMessageTime, now).toSeconds() >= seconds) {
                System.out.println("here");
                Chat chat = chatsRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Chat not found"));
                System.out.println("AI ID      " + chat.getAiId().getId());
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
