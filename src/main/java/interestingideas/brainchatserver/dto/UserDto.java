package interestingideas.brainchatserver.dto;

import interestingideas.brainchatserver.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@Schema(name = "New user trough email ", description = "Register new user via email")
public class UserDto {

    @Schema(description = "User ID", example = "1")
    private Long id;
    @Schema(description = "User full name", example = "Iana Makhonko")
    private String name;
    @Schema(description = "e-mail", example = "janaM2@gmail.com")
    private String email;
    @Schema(description = "User role", example = "USER")
    private String role;
    @Schema(description = "User state", example = "NOT_CONFIRMED")
    private String state;


    public static UserDto from(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .state(user.getState().name())
                .build();
    }

    public static List<UserDto> from(List<User> users) {
        return users.stream()
                .map(UserDto::from)
                .collect(Collectors.toList());
    }
}
