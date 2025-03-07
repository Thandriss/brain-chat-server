package interestingideas.brainchatserver.respreq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateChatRequest {
    private String chatName;
    private String topic;
    private String prompt;
    private String aiName;
    private String time;
    private Long numberParticipants;
    private String anonymity;
    private String mode;
}
