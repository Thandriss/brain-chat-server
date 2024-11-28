package interestingideas.brainchatserver.service;

import interestingideas.brainchatserver.config.RabbitConf;
import interestingideas.brainchatserver.dto.ChatDto;
import interestingideas.brainchatserver.dto.UserDto;
import interestingideas.brainchatserver.exception.RestException;
import interestingideas.brainchatserver.model.Chat;
import interestingideas.brainchatserver.model.ChatParticipants;
import interestingideas.brainchatserver.model.User;
import interestingideas.brainchatserver.repository.ChatParticipantsRepository;
import interestingideas.brainchatserver.repository.ChatsRepository;
import interestingideas.brainchatserver.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public List<ChatDto> getAllChats(Authentication authentication){
        System.out.println(authentication.getName());
        User user = getByLogin(authentication.getName());
        List<ChatParticipants> allChatsWithPart = chatParticipantsRepository.findChatParticipantsById(user.getId());
        List<Chat> res = new ArrayList<Chat>();
        for (ChatParticipants chatP: allChatsWithPart) {
            Chat chat = chatsRepository.getReferenceById(chatP.getChatId());
            res.add(chat);
        }
        return ChatDto.from(res);
    }
    public User getByLogin(String login) {
        return userRepository.findByEmail(login).orElseThrow(
                () -> new RestException(HttpStatus.NOT_FOUND,
                        "User with login <" + login + "> not found"));
    }
    @Transactional
    public String createGroup(String groupName) {
        String accessCode = RandomStringUtils.randomAlphanumeric(10).toUpperCase();
        Chat group = Chat.builder()
                .chatName(groupName)
                .createdAt(LocalDateTime.now())
                .UuidChat(accessCode)
                .topic("")
                .build();

        chatsRepository.save(group);

        // RabbitMQ setup
        rabbitMQConfig.createExchange(accessCode);
        String queueName = "group_" + accessCode;
//        + "_user_" + creator.getId()
        rabbitMQConfig.createQueue(queueName);
//        + ".user." + creator.getId()
        rabbitMQConfig.bindQueueToExchange(queueName, accessCode, "");

        return accessCode;
    }

    @Transactional
    public ChatDto joinGroup(String accessCode, Authentication authentication) {
        User user = getByLogin(authentication.getName());
        Chat group = chatsRepository.findByUuid(accessCode)
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        List<ChatParticipants> listOfUsers = chatParticipantsRepository.findByChatId(group.getId());

        if (listOfUsers.size() < 5 && !checkUserInList(listOfUsers, user.getId())) {
            ChatParticipants newMember = ChatParticipants.builder()
                            .joinedAt(LocalDateTime.now())
                            .chatId(group.getId())
                            .userId(user.getId())
                            .build();
            chatParticipantsRepository.save(newMember);

            String queueName = "group_" + group.getUuidChat();
//            + "_user_" + user.getId()
//            rabbitMQConfig.createQueue(queueName);
            rabbitMQConfig.bindQueueToExchange(queueName, group.getUuidChat(), "");
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
}
