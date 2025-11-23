package project.airbnb.clone.dto.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import project.airbnb.clone.entity.Member;

import java.time.LocalDate;

public record SignupRequestDto(
        @NotBlank
        String name,

        @NotBlank
        @Email
        String email,

        @Pattern(regexp = "^010\\d{8}$", message = "올바른 형식의 전화번호여야 합니다.(하이픈(-) 제외)")
        String number,

        @Past
        LocalDate birthDate,

        @NotBlank
        @Pattern(regexp = "^.*(?=^.{8,15}$)(?=.*\\d)(?=.*[a-zA-Z])(?=.*[!@#$%^&+=]).*$", message = "올바른 형식의 비밀번호여야 합니다.")
        String password
) {
    public Member toEntity(String encodedPassword) {
        return Member.createForRest(name, email, number, birthDate, encodedPassword);
    }
}
