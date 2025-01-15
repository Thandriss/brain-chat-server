package interestingideas.brainchatserver.service;

import interestingideas.brainchatserver.dto.MessageDto;
import interestingideas.brainchatserver.model.AI;
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
import java.util.List;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AIService {
    private final AIRepository aiRepository;
    private final ChatsRepository chatRepository;
    private final MessagesRepository messagesRepository;
    private final ChatService groupService;
    String baseUrl = "https://api.openai.com/v1/chat/completions";
    String apiKey = "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";


    public String generateAIMessage (Long ai_id, String accessCode) {
        try {
            System.out.println(ai_id);
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "gpt-4o-mini");
            requestBody.put("store", true);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_completion_tokens", 256);

            JSONArray messages = new JSONArray();
            AI ai = aiRepository.findById(ai_id).orElseThrow(() -> new RuntimeException("AI not found"));
            messages.put(new JSONObject().put("role", "system").put("content", ai.getPrompt()));
            List<MessageDto> messageDtoList = groupService.getAllMessages(accessCode);
            if (!messageDtoList.isEmpty()) {
                for (MessageDto messageDto: messageDtoList) {
                    messages.put(new JSONObject().put("role", "user").put("content", messageDto.getText()));
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

    public String changeAIPrompt(Long aiId, String text) {
        AI ai = aiRepository.findById(aiId).orElseThrow(() -> new RuntimeException("AI not found"));
        ai.setPrompt(text);
        aiRepository.save(ai);
        return text;
    }
    public String getAIPrompt(Long aiId) {
        AI ai = aiRepository.findById(aiId).orElseThrow(() -> new RuntimeException("AI not found"));
        return ai.getPrompt();
    }
}
