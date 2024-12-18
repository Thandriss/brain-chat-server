package interestingideas.brainchatserver.dto;

import interestingideas.brainchatserver.model.Chat;
import interestingideas.brainchatserver.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@Schema(name = "Chat", description = "Chat")
public class ChatDto {
    @Schema(description = "User ID", example = "1")
    private Long id;
    @Schema(description = "User full name", example = "Iana Makhonko")
    private String chatName;
    @Schema(description = "e-mail", example = "jana.M@mail.com")
    private String topic;
    @Schema(description = "User role", example = "USER")
    private String accessCode;
    @Schema(description = "time in minutes and seconds", example = "22:30")
    private String time;

    public static ChatDto from(Chat chat) {
        return ChatDto.builder()
                .id(chat.getId())
                .chatName(chat.getChatName())
                .topic(chat.getTopic())
                .accessCode(chat.getUuidChat())
                .time(chat.getTime())
                .build();
    }

    public static List<ChatDto> from(List<Chat> chats) {
        return chats.stream()
                .map(ChatDto::from)
                .collect(Collectors.toList());
    }
}
