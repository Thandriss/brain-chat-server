package interestingideas.brainchatserver.controllers.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import interestingideas.brainchatserver.dto.ChatDto;
import interestingideas.brainchatserver.dto.MessageDto;
import interestingideas.brainchatserver.dto.UserDto;
import interestingideas.brainchatserver.respreq.CreateChatRequest;
import interestingideas.brainchatserver.respreq.JoinRequest;
import interestingideas.brainchatserver.respreq.SendMessageRequest;
import interestingideas.brainchatserver.service.GroupService;
import interestingideas.brainchatserver.service.MessageConsumerService;
import interestingideas.brainchatserver.service.MessageProducerService;
import jakarta.validation.Valid;
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

    private final GroupService groupService;
    private final MessageProducerService messageProducer;
    private final MessageConsumerService messageConsumerService;
    @PostMapping("/create")
    public ResponseEntity<String> createGroup(@RequestBody CreateChatRequest request) {
        String accessCode = groupService.createGroup(request.getChatName());
        return ResponseEntity.ok(accessCode);
    }

    @MessageMapping("/send")
    @SendTo("/topic/group-messages")
    public void sendMessageToGroup(@RequestBody SendMessageRequest request) throws JsonProcessingException {
        System.out.println("sending");
        messageProducer.sendMessage(request.getMessage(), request.getChatName(), request.getId(), request.getName());
    }
    @PostMapping("/join/{accessCode}")
    public ResponseEntity<ChatDto> joinGroup(@PathVariable("accessCode") String accessCode, Authentication authentication) {
        ChatDto group = groupService.joinGroup(accessCode, authentication);
        return ResponseEntity.ok(group);
    }

    @GetMapping("/bindToChat/{accessCode}")
    public void bindToChat(@PathVariable("accessCode") String accessCode, Authentication authentication) {
        messageConsumerService.bindUserToChat(accessCode, authentication);
    }


    @GetMapping("/allChats")
    public List<ChatDto> getAllChats(Authentication authentication) {
        List<ChatDto> res = groupService.getAllChats(authentication);
        return res;
    }
    @GetMapping("/getMessages/{accessCode}")
    public List<MessageDto> getMessages(@PathVariable("accessCode") String accessCode, Authentication authentication) {
        List<MessageDto> res = groupService.getAllMessages(accessCode, authentication);
        return res;
    }

}
