package interestingideas.brainchatserver.respreq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SendMessageRequest {
    private String message;
    private String chatName;
    private Long id;
    private String name;
}
