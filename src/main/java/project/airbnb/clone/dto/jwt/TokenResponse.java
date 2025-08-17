package project.airbnb.clone.dto.jwt;

public record TokenResponse(
        String accessToken,
        String refreshToken)
{
}
