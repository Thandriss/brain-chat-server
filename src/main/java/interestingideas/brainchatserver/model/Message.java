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
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "content")
    private String content;
    @Column(name = "timestamp")
    private LocalDateTime timestamp;
    @Column(name = "chat_id")
    private Long chatId;
    @Column(name = "name")
    private String name;

}
