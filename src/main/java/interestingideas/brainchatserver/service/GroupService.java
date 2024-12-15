package interestingideas.brainchatserver.service;

import interestingideas.brainchatserver.config.RabbitConf;
import interestingideas.brainchatserver.dto.ChatDto;
import interestingideas.brainchatserver.dto.MessageDto;
import interestingideas.brainchatserver.exception.RestException;
import interestingideas.brainchatserver.model.*;
import interestingideas.brainchatserver.repository.*;
import interestingideas.brainchatserver.respreq.CreateChatRequest;
import interestingideas.brainchatserver.respreq.GetChatRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class GroupService {
    private final RabbitConf rabbitMQConfig;
    private final ChatsRepository chatsRepository;
    private final ChatParticipantsRepository chatParticipantsRepository;
    private final UsersRepository userRepository;
    private final MessagesRepository messagesRepository;
    private final AIRepository aiRepository;
    private static final String RABBITMQ_API_URL = "http://localhost:15672/api/exchanges/%2F/{exchange}/bindings/source";
    private static final String USERNAME = "guest";
    private static final String PASSWORD = "guest";

    public List<ChatDto> getAllChats(Authentication authentication){
        System.out.println(authentication.getName());
        User user = getByLogin(authentication.getName());
        List<ChatParticipants> allChatsWithPart = chatParticipantsRepository.findChatParticipantsById(user);
        List<Chat> ownChats = chatsRepository.findByAdminId(user);
        List<Chat> res = new ArrayList<Chat>();
        for (ChatParticipants chatP: allChatsWithPart) {
            Chat chat = chatsRepository.getReferenceById(chatP.getChatId().getId());
            res.add(chat);
        }
        res.addAll(ownChats);
        return ChatDto.from(res);
    }
    public User getByLogin(String login) {
        return userRepository.findByEmail(login).orElseThrow(
                () -> new RestException(HttpStatus.NOT_FOUND,
                        "User with login <" + login + "> not found"));
    }
    @Transactional
    public String createGroup(CreateChatRequest request, Authentication authentication) {
        User user = getByLogin(authentication.getName());
        String accessCode = RandomStringUtils.randomAlphanumeric(10).toUpperCase();
        AI ai_assistant = AI.builder()
                .prompt(request.getPrompt())
                .createdAt(LocalDateTime.now())
                .name(request.getAiName())
                .build();
        aiRepository.save(ai_assistant);
        Chat group = Chat.builder()
                .chatName(request.getChatName())
                .createdAt(LocalDateTime.now())
                .UuidChat(accessCode)
                .topic(request.getTopic())
                .adminId(user)
                .status(Chat.Status.DRAFT)
                .aiId(ai_assistant)
                .time(request.getTime())
                .numberParticipants(request.getNumberParticipants())
                .currentParticipants(1L)
                .build();

        chatsRepository.save(group);

        // RabbitMQ setup
        rabbitMQConfig.createExchange(accessCode);
        String queueName = "group_" + accessCode;
//        + "_user_" + creator.getId()
//        rabbitMQConfig.createQueue(queueName);
//        + ".user." + creator.getId()
//        rabbitMQConfig.bindQueueToExchange(queueName, accessCode, "");

        return accessCode;
    }

    @Transactional
    public ChatDto joinGroup(String accessCode, Authentication authentication) {
        User user = getByLogin(authentication.getName());
        Chat group = chatsRepository.findByUuid(accessCode)
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        if (group.getCurrentParticipants()+1 <= group.getNumberParticipants()+1 || Objects.equals(group.getAdminId().getId(), user.getId())) {
            Long curPart = group.getCurrentParticipants() + 1;
            group.setCurrentParticipants(curPart);
            chatsRepository.save(group);
            List<ChatParticipants> listOfUsers = chatParticipantsRepository.findByChatId(group);
            if (!checkUserInList(listOfUsers, user.getId())) {
                if (!Objects.equals(group.getAdminId().getId(), user.getId())) {
                    ChatParticipants newMember = ChatParticipants.builder()
                            .joinedAt(LocalDateTime.now())
                            .chatId(group)
                            .userId(user)
                            .build();
                    chatParticipantsRepository.save(newMember);
                }
                String queueName = "user_" + user.getId() + "_group_" + group.getUuidChat();
                rabbitMQConfig.createQueue(queueName);
                rabbitMQConfig.bindQueueToExchange(queueName, group.getUuidChat(), "");
            }
        }

        return ChatDto.from(group);
    }
    private boolean checkUserInList(List<ChatParticipants> list, Long userId) {
        for (ChatParticipants participant: list) {
            if (Objects.equals(participant.getUserId(), userId)) {
                return true;
            }
        }
        return false;
    }

    public List<MessageDto> getAllMessages(String accessCode, Authentication authentication) {
        if (authentication.isAuthenticated()) {
            Chat group = chatsRepository.findByUuid(accessCode)
                    .orElseThrow(() -> new RuntimeException("Chat not found"));
            List<Message> result = messagesRepository.findByChatId(group);
            return MessageDto.from(result);
        }
        return null;
    }

    public ChatDto getChat(GetChatRequest request) {
        Chat group = chatsRepository.findById(request.getChatId())
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        return ChatDto.from(group);
    }

    public int getBindingsCount(String exchange) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(USERNAME, PASSWORD);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Object[]> response = restTemplate.exchange(
                RABBITMQ_API_URL,
                HttpMethod.GET,
                entity,
                Object[].class,
                exchange
        );

        return response.getBody().length;
    }
}
