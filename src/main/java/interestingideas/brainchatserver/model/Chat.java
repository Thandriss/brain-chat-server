package interestingideas.brainchatserver.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "chats")
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "chat_name")
    private String chatName;
    @Column(name = "topic")
    private String topic;
    @Column(name = "uuid-chat")
    private String UuidChat;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
