package interestingideas.brainchatserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
@Slf4j
@RequiredArgsConstructor
@Service
public class MessageProducerService {
    private final RabbitTemplate rabbitTemplate;

    public void sendMessage(String message, String queueName) {
        rabbitTemplate.convertAndSend("group_" + queueName, message);
    }
}
