package interestingideas.brainchatserver.service;

import interestingideas.brainchatserver.dto.MessageDto;
import interestingideas.brainchatserver.model.AI;
import interestingideas.brainchatserver.model.Chat;
import interestingideas.brainchatserver.repository.AIRepository;
import interestingideas.brainchatserver.repository.ChatsRepository;
import interestingideas.brainchatserver.repository.MessagesRepository;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AIService {
    private final AIRepository aiRepository;
    private final AiCached aicached;
    private final ChatService groupService;
    private final ChatSessionManager chatSessionManager;
    private final ChatService chatService;
    String baseUrl = "https://api.openai.com/v1/chat/completions";

    @Value("${ai.key}")
    private String apiKey;


    public String generateAIMessage (Long ai_id, String accessCode) {
        try {
            System.out.println(ai_id);
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "gpt-4o-mini");
            requestBody.put("store", true);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_completion_tokens", 256);

            JSONArray messages = new JSONArray();
            AI ai = aicached.findByIdCached(ai_id);
            Chat chat = chatService.findByUuidCached(accessCode);
            messages.put(new JSONObject().put("role", "system").put("content", ai.getPrompt() + " Topic: " + chat.getTopic() + " Mode " + chat.getMode() + "If someone makes an off-topic comment in chat, redirect them or admonish them."));
            List<MessageDto> messageDtoList = groupService.getAllMessages(accessCode);
            if (!messageDtoList.isEmpty()) {
                for (MessageDto messageDto: messageDtoList) {
                    if (messageDto.getAiId() == null) {
                        messages.put(new JSONObject().put("role", "user").put("content", messageDto.getText()));
                    } else  {
                        messages.put(new JSONObject().put("role", "assistant").put("content", messageDto.getText() + " " + "Not respond on this message. It is your idea"));
                    }
                }
            }
            requestBody.put("messages", messages);

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                JSONObject jsonResponse = new JSONObject(response.body());
                JSONObject choice = jsonResponse.getJSONArray("choices").getJSONObject(0);
                String content = choice.getJSONObject("message").getString("content");
                LocalDateTime now = LocalDateTime.now();
                chatSessionManager.startSession(chat.getId(), now);
                System.out.println(content);
                return content;
            } else {
                System.err.println("Error: " + response.statusCode() + " - " + response.body());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @CacheEvict(value = "aiByIdCache", key = "#aiId")
    public String changeAIPrompt(Long aiId, String text) {
        AI ai = aiRepository.findById(aiId).orElseThrow(() -> new RuntimeException("AI not found"));
        ai.setPrompt(text);
        aiRepository.save(ai);
        return text;
    }

    public String getAIPrompt(Long aiId) {
        AI ai = aicached.findByIdCached(aiId);
        return ai.getPrompt();
    }
}
