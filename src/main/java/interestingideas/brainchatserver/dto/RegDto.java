package interestingideas.brainchatserver.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import org.antlr.v4.runtime.misc.NotNull;

@Data
@Builder
@Schema(name = "New user trough email ", description = "Register new user via email")
public class RegDto {
    @Email
    @NotNull
    @Schema(description = "User email", example = "user@mail.com")
    @Size(max = 200, message = "Size must be in the range from 0 to 200")
    private String email;

    public static RegDto from(String email) {
        return RegDto.builder()
                .email(email)
                .build();
    }
}
