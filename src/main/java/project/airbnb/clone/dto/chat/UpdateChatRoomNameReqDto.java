package project.airbnb.clone.dto.chat;

import jakarta.validation.constraints.NotBlank;

public record UpdateChatRoomNameReqDto(@NotBlank String customName, Long otherGuestId) {
}
