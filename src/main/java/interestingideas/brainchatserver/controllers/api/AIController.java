package interestingideas.brainchatserver.controllers.api;

import interestingideas.brainchatserver.respreq.AIServiceReq;
import interestingideas.brainchatserver.service.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {
    private final AIService aiService;
    @PostMapping("/changePrompt")
    public ResponseEntity<String> changePrompt(@RequestBody AIServiceReq request, Authentication authentication) {
        String newPrompt = aiService.changeAIPrompt(request.getAiId(), request.getText());
        return ResponseEntity.ok(newPrompt);
    }

    @GetMapping("/getPrompt/{aiId}")
    public ResponseEntity<String> getPrompt(@PathVariable("aiId") Long aiId, Authentication authentication) {
        String newPrompt = aiService.getAIPrompt(aiId);
        return ResponseEntity.ok(newPrompt);
    }

}
