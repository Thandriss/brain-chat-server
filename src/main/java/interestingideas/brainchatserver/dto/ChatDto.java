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
    @Schema(description = "User full name", example = "Max Musterman")
    private String chatName;
    @Schema(description = "e-mail", example = "m.musterman@gmx.de")
    private String topic;
    @Schema(description = "User role", example = "USER")
    private String accessCode;

    public static ChatDto from(Chat chat) {
        return ChatDto.builder()
                .id(chat.getId())
                .chatName(chat.getChatName())
                .topic(chat.getTopic())
                .accessCode(chat.getUuidChat())
                .build();
    }

    public static List<ChatDto> from(List<Chat> chats) {
        return chats.stream()
                .map(ChatDto::from)
                .collect(Collectors.toList());
    }
}
