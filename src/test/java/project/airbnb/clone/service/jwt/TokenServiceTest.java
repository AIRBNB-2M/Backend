package project.airbnb.clone.service.jwt;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import project.airbnb.clone.TestContainersConfig;
import project.airbnb.clone.common.events.logout.OAuthLogoutEvent;
import project.airbnb.clone.common.jwt.JwtProperties;
import project.airbnb.clone.common.jwt.JwtProperties.TokenProperties;
import project.airbnb.clone.common.jwt.JwtProvider;
import project.airbnb.clone.dto.jwt.TokenResponse;
import project.airbnb.clone.entity.Guest;
import project.airbnb.clone.repository.jpa.GuestRepository;
import project.airbnb.clone.repository.redis.RedisRepository;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Transactional
class TokenServiceTest extends TestContainersConfig {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private RedisRepository redisRepository;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private JwtProvider jwtProvider;

    Guest guest;

    @BeforeEach
    void setUp() {
        jwtProperties.setSecretKey(Base64.getEncoder().encodeToString("test-secret-key".repeat(10).getBytes()));

        TokenProperties accessToken = new TokenProperties();
        accessToken.setExpiration(300_000);
        jwtProperties.setAccessToken(accessToken);

        TokenProperties refreshToken = new TokenProperties();
        refreshToken.setExpiration(600_000);
        jwtProperties.setRefreshToken(refreshToken);

        guest = Guest.builder().name("Jessica Bala").email("test@email.com").password("858d2781-2a13-4d26-b3c9-7b84b214f82f").build();
        guestRepository.saveAndFlush(guest);
    }

    @AfterEach
    void tearDown() {
        redisRepository.deleteValue(String.valueOf(guest.getId()));
    }

    @Test
    @DisplayName("토큰 생성 후 액세스 토큰은 헤더로 전달되고, 리프레시 토큰은 쿠키 전달과 함께 레디스에 저장된다.")
    void generateAndSendToken() {
        //given
        String email = guest.getEmail();
        String principalName = "principal";
        MockHttpServletResponse response = new MockHttpServletResponse();

        //when
        TokenResponse tokenResponse = tokenService.generateAndSendToken(email, principalName, response);

        //then
        assertThat(tokenResponse).isNotNull();

        String accessToken = tokenResponse.accessToken();
        String refreshToken = tokenResponse.refreshToken();

        assertThat(accessToken).isNotBlank();
        assertThat(refreshToken).isNotBlank();

        String savedRefreshToken = redisRepository.getValue(String.valueOf(guest.getId()));
        assertThat(savedRefreshToken).isEqualTo(refreshToken);

        String authHeader = response.getHeader("Authorization");
        assertThat(authHeader).isEqualTo("Bearer " + accessToken);

        String setCookie = response.getHeader("Set-Cookie");
        assertThat(setCookie).contains("RefreshToken=" + refreshToken);
    }

    @Test
    @DisplayName("액세스 토큰 갱신 - 레디스에 저장된 값과 일치하는 경우")
    void refreshAccessToken_success() {
        //given
        String token = jwtProvider.generateRefreshToken(guest, "a6025936-1554-4f45-8601-a107576eb9d8");
        String key = String.valueOf(guest.getId());
        redisRepository.setValue(key, token);

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpServletRequest request = new MockHttpServletRequest();

        //when
        tokenService.refreshAccessToken(token, response, request);

        //then
        //1. 레디스에는 새로운 리프레시 토큰 값이 저장되어야 한다.
        String newSavedRefreshToken = redisRepository.getValue(key);
        assertThat(token).isNotEqualTo(newSavedRefreshToken);

        //2. 예외 상황에 대비해 request에 key가 저장되어야 한다.
        assertThat(request.getAttribute("key")).isEqualTo(key);

        //3. 정상적으로 액세스 토큰과 리프레시 토큰이 전달된다.
        String authHeader = response.getHeader("Authorization");
        assertThat(authHeader).isNotBlank().contains("Bearer ");

        String setCookie = response.getHeader("Set-Cookie");
        assertThat(setCookie).isNotBlank().contains("RefreshToken=");
    }

    @Test
    @DisplayName("액세스 토큰 갱신 - 레디스에 저장된 값과 일치하지 않는 경우")
    void refreshAccessToken_fail() {
        //given
        String token = jwtProvider.generateRefreshToken(guest, "5e21afe5-3e80-4f9d-9f37-a41d309dcca9");
        String key = String.valueOf(guest.getId());
        redisRepository.setValue(key, "other-wrong-token");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpServletRequest request = new MockHttpServletRequest();

        //when
        assertThatThrownBy(() -> tokenService.refreshAccessToken(token, response, request))
                .isInstanceOf(JwtException.class)
                .hasMessageContaining("Refresh Token is invalid: ");
    }

    @Test
    @DisplayName("로그아웃 처리 시 액세스 토큰 블랙리스트 추가, 리프레시 토큰 삭제, 로그아웃 이벤트가 발행된다.")
    void logoutProcess() {
        // given
        String principalName = "principal";
        String accessToken = "Bearer " + jwtProvider.generateAccessToken(guest, principalName);
        String refreshToken = jwtProvider.generateRefreshToken(guest, principalName);

        // Redis에 refresh token 저장
        redisRepository.setValue(String.valueOf(guest.getId()), refreshToken);

        // 이벤트 리스너를 Mock 처리
        ApplicationEventPublisher mockPublisher = mock(ApplicationEventPublisher.class);
        ReflectionTestUtils.setField(tokenService, "eventPublisher", mockPublisher);

        // when
        tokenService.logoutProcess(accessToken, refreshToken);

        // then
        // 1. 액세스 토큰이 블랙리스트에 추가됐는지 확인
        boolean isBlackListed = tokenService.containsBlackList(accessToken.substring("Bearer ".length()));
        assertThat(isBlackListed).isTrue();

        // 2. 리프레시 토큰이 삭제됐는지 확인
        String savedRefreshToken = redisRepository.getValue(String.valueOf(guest.getId()));
        assertThat(savedRefreshToken).isNull();

        // 3. 로그아웃 이벤트 발행 확인
        verify(mockPublisher, times(1)).publishEvent(any(OAuthLogoutEvent.class));
    }
}