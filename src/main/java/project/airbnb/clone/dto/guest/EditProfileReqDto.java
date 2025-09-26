package project.airbnb.clone.dto.guest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EditProfileReqDto(
        @NotBlank
        String name,
        @Size(max = 500)
        String aboutMe,
        @NotNull
        Boolean isProfileImageChanged) {
}
