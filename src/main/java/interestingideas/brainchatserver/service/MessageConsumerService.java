package interestingideas.brainchatserver.service;

import interestingideas.brainchatserver.dto.UserDto;
import interestingideas.brainchatserver.exception.RestException;
import interestingideas.brainchatserver.model.User;
import interestingideas.brainchatserver.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
@Slf4j
@RequiredArgsConstructor
@Service
public class MessageConsumerService {
    private final SimpMessagingTemplate messagingTemplate;
    private final RabbitAdmin rabbitAdmin;
    private final UsersRepository userRepository;

    public void bindUserToChat(String accessCode, Authentication authentication) {
        User user = getByLogin(authentication.getName());
        String queueName =  "user_" + user.getId() + "_group_" + accessCode;
        Queue queue = new Queue(queueName, true);
        rabbitAdmin.declareQueue(queue);

        Binding binding = BindingBuilder.bind(queue).to(new FanoutExchange(accessCode));
        rabbitAdmin.declareBinding(binding);
    }

    public User getByLogin(String login) {
        return userRepository.findByEmail(login).orElseThrow(
                () -> new RestException(HttpStatus.NOT_FOUND,
                        "User with login <" + login + "> not found"));
    }

    @RabbitListener
    public void receiveMessage(String message, @Header(AmqpHeaders.CONSUMER_QUEUE) String queue) {
        System.out.println("Received message from queue: " + queue);
        System.out.println("Message: " + message);
        messagingTemplate.convertAndSend("/topic/group-messages", message);
    }
}
