package project.airbnb.clone.service.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.airbnb.clone.common.jwt.JwtProvider;
import project.airbnb.clone.dto.jwt.TokenResponse;
import project.airbnb.clone.entity.Guest;
import project.airbnb.clone.repository.guest.GuestRepository;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtProvider jwtProvider;
    private final GuestRepository guestRepository;

    public TokenResponse generateToken(String email) {
        Guest guest = guestRepository.getGuestByEmail(email);

        String accessToken = jwtProvider.generateAccessToken(guest);
        String refreshToken = jwtProvider.generateRefreshToken(guest);

        return new TokenResponse(accessToken, refreshToken);
    }
}
