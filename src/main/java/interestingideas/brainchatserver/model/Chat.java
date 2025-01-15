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

    public enum Status {
        DRAFT, ACTIVE, CANCELLED
    }

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
    @Column(name = "end_at")
    private LocalDateTime endAt;
    @Column(name = "time")
    private String time;
    @Column(name = "status")
    private Chat.Status status;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admin_id", nullable = false)
    private User adminId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ai_id", nullable = false)
    private AI aiId;
    @Column(name = "number_participants")
    private Long numberParticipants;
    @Column(name = "current_participants")
    private Long currentParticipants;
}
