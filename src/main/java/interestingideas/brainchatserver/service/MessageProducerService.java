package interestingideas.brainchatserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import interestingideas.brainchatserver.model.AI;
import interestingideas.brainchatserver.model.Chat;
import interestingideas.brainchatserver.model.Message;
import interestingideas.brainchatserver.model.User;
import interestingideas.brainchatserver.repository.AIRepository;
import interestingideas.brainchatserver.repository.ChatsRepository;
import interestingideas.brainchatserver.repository.MessagesRepository;
import interestingideas.brainchatserver.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import netscape.javascript.JSObject;
import org.json.JSONObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class MessageProducerService {
    private final RabbitTemplate rabbitTemplate;
    private final MessagesRepository messagesRepository;
    private final ChatsRepository chatRepository;
    private final UsersRepository usersRepository;
    private final ChatSessionManager chatSessionManager;
    private final AIRepository aiRepository;

    public void sendMessage(String message, String queueName, Long id, String name) throws JsonProcessingException {
        System.out.println("sending " + "group_" + queueName);
        User user = usersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));;
        Chat chat = chatRepository.findByUuid(queueName)
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        LocalDateTime now = LocalDateTime.now();
        Message messageSave = Message.builder()
                        .content(message)
                        .timestamp(now)
                        .userId(user)
                        .chatId(chat)
                        .aiId(null)
                        .name(name)
                        .build();
        chatSessionManager.startSession(chat.getId(), now);
        messagesRepository.save(messageSave);
        Map<String, String> messageMap = new HashMap<>();
        messageMap.put("content", message);
        messageMap.put("name", name);
        ObjectMapper objectMapper = new ObjectMapper();
        String result = objectMapper.writeValueAsString(messageMap);
        rabbitTemplate.convertAndSend(queueName, "", result);
    }

    public void sendAIMessage(String message, String queueName, Long ai_id) throws JsonProcessingException {
        System.out.println("sending " + "group_" + queueName);
        Chat chat = chatRepository.findByUuid(queueName)
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        AI ai = aiRepository.findById(ai_id).orElseThrow(() -> new RuntimeException("AI not found"));
        Message messageSave = Message.builder()
                .content(message)
                .timestamp(LocalDateTime.now())
                .userId(null)
                .chatId(chat)
                .aiId(ai)
                .name(ai.getName())
                .build();
        messagesRepository.save(messageSave);
        Map<String, String> messageMap = new HashMap<>();
        messageMap.put("content", message);
        messageMap.put("name", ai.getName());
        ObjectMapper objectMapper = new ObjectMapper();
        String result = objectMapper.writeValueAsString(messageMap);
        rabbitTemplate.convertAndSend(queueName, "", result);
    }
}
