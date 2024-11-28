package interestingideas.brainchatserver.respreq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    private final String type = "Bearer";

    private String accessToken;

    private String refreshToken;

    private Boolean adminToken;


}
