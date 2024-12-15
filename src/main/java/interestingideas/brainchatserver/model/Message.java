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
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_id", nullable = true)
    private User userId;
    @Column(name = "content")
    private String content;
    @Column(name = "timestamp")
    private LocalDateTime timestamp;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_id", nullable = true)
    private Chat chatId;
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "ai_id", nullable = true)
    private AI aiId;
    @Column(name = "name")
    private String name;

}
