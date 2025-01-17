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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatService {
    private final RabbitConf rabbitMQConfig;
    private final ChatsRepository chatsRepository;
    private final ChatParticipantsRepository chatParticipantsRepository;
    private final UsersRepository userRepository;
    private final MessagesRepository messagesRepository;
    private final AIRepository aiRepository;
    private final ChatSessionManager chatSessionManager;
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
    public String createChat(CreateChatRequest request, Authentication authentication) {
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
                .endAt(null)
                .numberParticipants(request.getNumberParticipants())
                .currentParticipants(1L)
                .anonymity(request.isAnonymity())
                .mode(request.getMode())
                .build();

        chatsRepository.save(group);

        rabbitMQConfig.createExchange(accessCode);
        String queueName = "group_" + accessCode;
//        + "_user_" + creator.getId()
//        rabbitMQConfig.createQueue(queueName);
//        + ".user." + creator.getId()
//        rabbitMQConfig.bindQueueToExchange(queueName, accessCode, "");

        return accessCode;
    }

    @Transactional
    public ChatDto joinChat(String accessCode, Authentication authentication) {
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

    public List<MessageDto> getAllMessages(String accessCode) {
        Chat group = chatsRepository.findByUuid(accessCode)
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        List<Message> result = messagesRepository.findByChatId(group);
        return MessageDto.from(result);
    }

    public ChatDto getChat(GetChatRequest request) {
        Chat chat = chatsRepository.findById(request.getChatId())
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        return ChatDto.from(chat);
    }

    public ChatDto closeChat(GetChatRequest request) {
        System.out.println("Closing " + request.getChatId());
        Chat chat = chatsRepository.findById(request.getChatId())
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        chat.setStatus(Chat.Status.CANCELLED);
        chatSessionManager.endSession(request.getChatId());
        chatsRepository.save(chat);
        return ChatDto.from(chat);
    }

    public ChatDto openChat(GetChatRequest request) {
        Chat chat = chatsRepository.findById(request.getChatId())
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        if (chat.getStatus().equals(Chat.Status.DRAFT)) {
            System.out.println("Opening " + request.getChatId());
            chat.setStatus(Chat.Status.ACTIVE);
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime newNow = LocalDateTime.now();
            String[] timeParts = chat.getTime().split(":");
            int minutes = Integer.parseInt(timeParts[0]);
            int seconds = Integer.parseInt(timeParts[1]);
            LocalDateTime updatedTime = newNow.plusMinutes(minutes).plusSeconds(seconds);
            chat.setEndAt(updatedTime);
            chatSessionManager.startSession(request.getChatId(), now);
            chat = chatsRepository.save(chat);
        }

        return ChatDto.from(chat);
    }
    public String getTime(GetChatRequest request) {
        Chat chat = chatsRepository.findById(request.getChatId())
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        LocalDateTime now = LocalDateTime.now();
        Duration timeRemaining = Duration.between(now, chat.getEndAt());
        long minutes = timeRemaining.toMinutes();
        long seconds = timeRemaining.minusMinutes(minutes).getSeconds();
        String formattedTime = String.format("%02d:%02d", minutes, seconds);
        return formattedTime;
    }

    public int getBindingsCount(String exchange) {
        HttpURLConnection connection = null;
        try {

            URL url = new URL("http://localhost:15672/api/exchanges/%2F/" + exchange  + "/bindings/source");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.setRequestProperty("Content-Language", "en-US");
            String userpass =   "guest:guest";
            String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
            connection.setRequestProperty ("Authorization", basicAuth);

            connection.setUseCaches(false);
            connection.setDoOutput(true);

            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            ArrayList<String> response = new ArrayList<>();
            String line;
            while ((line = rd.readLine()) != null) {
                response.add(line);
            }
            rd.close();

            System.out.println(response.size());
            return response.size();

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
//        System.out.println(exchange);
//        RestTemplate restTemplate = new RestTemplate();
//        HttpHeaders headers = new HttpHeaders();
////        headers.setBasicAuth(USERNAME, PASSWORD);
//        headers.set("Accept", "application/json");
//        headers.setBasicAuth(USERNAME, PASSWORD);
//
//        HttpEntity<String> entity = new HttpEntity<>(headers);
//
//        ResponseEntity<Object[]> response = restTemplate.exchange(
//                "http://localhost:15672/api/exchanges/%2F/SJQDW5FXR2/bindings/source",
//                HttpMethod.GET,
//                entity,
//                Object[].class
//        );
//        System.out.println("Response Code: " + response.getStatusCode());
//        System.out.println(Arrays.toString(response.getBody()));
//        return Objects.requireNonNull(response.getBody()).length;
        return 0;
    }

    public void endSession(Long chatId) {
        ConcurrentHashMap<Long, LocalDateTime> activeSessions = chatSessionManager.getAllChatActivities();
        if (activeSessions.containsKey(chatId)) {
            Chat chat = chatsRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Chat not found"));
            chat.setStatus(Chat.Status.CANCELLED);
            chatsRepository.save(chat);
            chatSessionManager.endSession(chatId);
        }
    }
}
