package interestingideas.brainchatserver.service;

import interestingideas.brainchatserver.model.Chat;
import interestingideas.brainchatserver.repository.ChatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
@Slf4j
@RequiredArgsConstructor
@Service
public class ChatSessionManager {
    private final ChatsRepository chatsRepository;
    private final ConcurrentHashMap<Long, LocalDateTime> sessionMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Integer> thresholds = new ConcurrentHashMap<>();
    private final Random rand = new Random();
    private final AiCached aiCache;

    public void startSession(Long chatId, LocalDateTime endTime) {
        sessionMap.put(chatId, endTime);
        thresholds.put(chatId, rand.nextInt(11) + 10);
    }
    @CacheEvict(value = "chatCache", key = "#chatId")
    public void endSession(Long chatId) {
        sessionMap.remove(chatId);
        thresholds.remove(chatId);
        Chat chat = chatsRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Chat not found"));
        cleanCachUUID(chat.getUuidChat());
        aiCache.deleteCacheAIById(chat.getAiId().getId());
    }

    @CacheEvict(value = "chatByUuidCache", key = "#uuidChat")
    public void cleanCachUUID(String uuidChat) {

    }


    public int getThreshold(Long chatId) {
        return thresholds.getOrDefault(chatId, 15); // по умолчанию
    }

    public boolean isSessionActive(Long chatId) {
        Chat chat = chatsRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Chat not found"));
        return chat.getStatus().name().equals("1");
    }

    public ConcurrentHashMap<Long, LocalDateTime> getAllChatActivities() {
        return sessionMap;
    }
}
