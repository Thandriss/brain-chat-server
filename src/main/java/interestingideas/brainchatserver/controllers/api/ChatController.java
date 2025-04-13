package interestingideas.brainchatserver.controllers.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import interestingideas.brainchatserver.dto.ChatDto;
import interestingideas.brainchatserver.dto.MessageDto;
import interestingideas.brainchatserver.respreq.*;
import interestingideas.brainchatserver.service.ChatService;
import interestingideas.brainchatserver.service.MessageConsumerService;
import interestingideas.brainchatserver.service.MessageProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final MessageProducerService messageProducer;
    private final MessageConsumerService messageConsumerService;
    @PostMapping("/create")
    public ResponseEntity<String> createChat(@RequestBody CreateChatRequest request, Authentication authentication) {
        String accessCode = chatService.createChat(request, authentication);
        return ResponseEntity.ok(accessCode);
    }

    @MessageMapping("/send")
    @SendTo("/topic/group-messages")
    public void sendMessageToChat(@RequestBody SendMessageRequest request) throws JsonProcessingException {
        System.out.println("sending");
        messageProducer.sendMessage(request.getMessage(), request.getChatName(), request.getId(), request.getName());
    }
    @PostMapping("/join/{accessCode}")
    public ResponseEntity<ChatDto> joinChat(@PathVariable("accessCode") String accessCode, Authentication authentication) {
        ChatDto group = chatService.joinChat(accessCode, authentication);
        return ResponseEntity.ok(group);
    }

    @GetMapping("/bindToChat/{accessCode}")
    public void bindToChat(@PathVariable("accessCode") String accessCode, Authentication authentication) {
        messageConsumerService.bindUserToChat(accessCode, authentication);
    }

    @PostMapping("/getChatData")
    public ResponseEntity<ChatDto> getChat( @RequestBody GetChatRequest request, Authentication authentication) {
        ChatDto group = chatService.getChat(request);
        return ResponseEntity.ok(group);
    }

    @PostMapping("/closeChat")
    public ResponseEntity<ChatDto> closeChat( @RequestBody GetChatRequest request, Authentication authentication) {
        ChatDto group = chatService.closeChat(request);
        return ResponseEntity.ok(group);
    }

    @PostMapping("/getTime")
    public ResponseEntity<TimeResp> getTime( @RequestBody GetChatRequest request, Authentication authentication) {
        String time = chatService.getTime(request);
        return ResponseEntity.ok(new TimeResp(time));
    }

    @PostMapping("/openChat")
    public ResponseEntity<ChatDto> openChat( @RequestBody GetChatRequest request, Authentication authentication) {
        ChatDto group = chatService.openChat(request);
        return ResponseEntity.ok(group);
    }

    @GetMapping("/count/{accessCode}")
    public ResponseEntity<BindingCountResp> getBindingsCount(@PathVariable("accessCode") String accessCode) {
        int count = chatService.getBindingsCount(accessCode);
        return ResponseEntity.ok(new BindingCountResp(count));
    }

//    endSession not need
//    @PostMapping("/endSession")
//    public void endChat( @RequestBody GetChatRequest request, Authentication authentication) {
//        chatService.endSession(request.getChatId());
//    }

    @GetMapping("/allChats")
    public List<ChatDto> getAllChats(Authentication authentication) {
        List<ChatDto> res = chatService.getAllChats(authentication);
        return res;
    }
    @GetMapping("/getMessages/{accessCode}")
    public List<MessageDto> getMessages(@PathVariable("accessCode") String accessCode, Authentication authentication) {
        List<MessageDto> res = chatService.getAllMessages(accessCode, authentication);
        return res;
    }

}
