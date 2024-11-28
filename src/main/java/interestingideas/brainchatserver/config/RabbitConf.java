package interestingideas.brainchatserver.config;


import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class RabbitConf {
    private final ConnectionFactory connectionFactory;

    public RabbitConf(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Bean
    @Lazy
    public RabbitAdmin rabbitAdmin() {
        return new RabbitAdmin(connectionFactory);
    }

    public void createExchange(String exchangeName) {
        Exchange exchange = ExchangeBuilder.fanoutExchange(exchangeName).durable(true).build();
        rabbitAdmin().declareExchange(exchange);
    }

    public void createQueue(String queueName) {
        Queue queue = new Queue(queueName, true);
        rabbitAdmin().declareQueue(queue);
    }

    public void bindQueueToExchange(String queueName, String exchangeName, String routingKey) {
        Binding binding = BindingBuilder.bind(new Queue(queueName))
                .to(new FanoutExchange(exchangeName));
        rabbitAdmin().declareBinding(binding);
    }
}
