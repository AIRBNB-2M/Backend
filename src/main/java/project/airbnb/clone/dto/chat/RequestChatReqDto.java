package project.airbnb.clone.dto.chat;

import jakarta.validation.constraints.NotNull;

public record RequestChatReqDto(@NotNull Long receiverId) {
}
