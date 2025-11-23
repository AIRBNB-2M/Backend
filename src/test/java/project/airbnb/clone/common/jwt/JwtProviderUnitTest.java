package project.airbnb.clone.common.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import project.airbnb.clone.common.jwt.JwtProperties.TokenProperties;
import project.airbnb.clone.config.security.jwt.JwtAuthenticationToken;
import project.airbnb.clone.consts.Role;
import project.airbnb.clone.entity.Member;
import project.airbnb.clone.model.AuthProviderUser;
import project.airbnb.clone.model.PrincipalUser;
import project.airbnb.clone.repository.jpa.MemberRepository;

import javax.crypto.SecretKey;
import java.util.Base64;

import static io.jsonwebtoken.io.Decoders.BASE64;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtProvider 단위 테스트")
class JwtProviderUnitTest {

    @Mock
    MemberRepository memberRepository;

    JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecretKey(Base64.getEncoder().encodeToString("test-secret-key".repeat(10).getBytes()));

        TokenProperties accessToken = new TokenProperties();
        accessToken.setExpiration(1000);
        jwtProperties.setAccessToken(accessToken);

        TokenProperties refreshToken = new TokenProperties();
        refreshToken.setExpiration(1000);
        jwtProperties.setRefreshToken(refreshToken);

        this.jwtProvider = new JwtProvider(memberRepository, jwtProperties);
    }

    @Nested
    @DisplayName("토큰 생성 시 (헤더.페이로드.시그니처) 형태로 토큰을 생성한다.")
    class TokenGenerate {

        @Test
        @DisplayName("액세스 토큰 생성")
        void generateAccessToken() {
            //given
            Member member = mock(Member.class);
            String principalName = "principal";

            given(member.getId()).willReturn(1L);

            //when
            String result = jwtProvider.generateAccessToken(member, principalName);

            //then
            assertThat(result).isNotBlank();
            assertThat(result.split("\\.")).hasSize(3);
        }

        @Test
        @DisplayName("리프레시 토큰 생성")
        void generateRefreshToken() {
            //given
            Member member = mock(Member.class);
            String principalName = "principal";

            given(member.getId()).willReturn(1L);

            //when
            String result = jwtProvider.generateRefreshToken(member, principalName);

            //then
            assertThat(result).isNotBlank();
            assertThat(result.split("\\.")).hasSize(3);
        }
    }

    @Nested
    @DisplayName("토큰 검증에 실패하는 경우")
    class TokenValidateFailed {

        @Test
        @DisplayName("만료된 토큰 검증 시 ExpiredJwtException 예외가 발생한다.")
        void expiredToken() {
            //given
            String expiredToken = ReflectionTestUtils.invokeMethod(jwtProvider, "generateToken", 1L, -1, "principal");

            //when
            //then
            assertThatThrownBy(() -> jwtProvider.validateToken(expiredToken))
                    .isInstanceOf(JwtException.class)
                    .hasCauseInstanceOf(ExpiredJwtException.class)
                    .hasMessage("Token is invalid");
        }

        @Test
        @DisplayName("형식이 잘못된 토큰 검증 시 MalformedJwtException 예외가 발생한다.")
        void invalidToken() {
            //given
            String invalidToken = "invalid-token";

            //when
            //then
            assertThatThrownBy(() -> jwtProvider.validateToken(invalidToken))
                    .isInstanceOf(JwtException.class)
                    .hasCauseInstanceOf(MalformedJwtException.class)
                    .hasMessage("Token is invalid");
        }

        @Test
        @DisplayName("다른 Key로 서명된 토큰 검증 시 SignatureException 예외가 발생한다.")
        void invalidSignatureToken() {
            //given
            SecretKey anotherKey = Keys.hmacShaKeyFor(
                    BASE64.decode(
                            Base64.getEncoder().encodeToString("another-secret-key".repeat(10).getBytes())
                    )
            );

            //when
            String invalidToken = Jwts.builder()
                                      .signWith(anotherKey)
                                      .compact();

            //then
            assertThatThrownBy(() -> jwtProvider.validateToken(invalidToken))
                    .isInstanceOf(JwtException.class)
                    .hasCauseInstanceOf(SignatureException.class)
                    .hasMessage("Token is invalid");
        }

        @Test
        @DisplayName("빈 토큰 검증 시 IllegalArgumentException 예외가 발생한다.")
        void emptyToken() {
            //given
            String invalidToken = "";

            //when
            //then
            assertThatThrownBy(() -> jwtProvider.validateToken(invalidToken))
                    .isInstanceOf(JwtException.class)
                    .hasCauseInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Token is invalid");
        }
    }

    @Test
    @DisplayName("생성된 토큰에서 Id와 PrincipalName을 추출할 수 있다.")
    void parseClaims() {
        //given
        Member member = mock(Member.class);
        String inputPrincipalName = "principal";
        Long inputId = 1L;

        given(member.getId()).willReturn(inputId);

        String token = jwtProvider.generateAccessToken(member, inputPrincipalName);

        //when
        Long outputId = jwtProvider.getId(token);
        String outputPrincipalName = jwtProvider.getPrincipalName(token);

        //then
        assertThat(outputId).isEqualTo(inputId);
        assertThat(outputPrincipalName).isEqualTo(inputPrincipalName);
    }

    @Nested
    @DisplayName("토큰으로 인증 객체를 생성하는 경우")
    class Auth {

        @Test
        @DisplayName("성공 케이스")
        void success() {
            //given
            Member member = mock(Member.class);
            given(member.getId()).willReturn(1L);
            given(member.getRole()).willReturn(Role.GUEST);
            given(memberRepository.getMemberById(1L)).willReturn(member);

            String token = jwtProvider.generateAccessToken(member, "principal");

            //when
            Authentication authentication = jwtProvider.getAuthentication(token);

            //then
            assertThat(authentication).isInstanceOf(JwtAuthenticationToken.class);
            assertThat(authentication.getPrincipal()).isInstanceOf(PrincipalUser.class);

            PrincipalUser principal = (PrincipalUser) authentication.getPrincipal();
            assertThat(principal.providerUser()).isInstanceOf(AuthProviderUser.class);
        }

        @Test
        @DisplayName("실패 케이스")
        void fail() {
            //given
            Member member = mock(Member.class);
            given(member.getId()).willReturn(1L);
            given(memberRepository.getMemberById(1L)).willThrow(EntityNotFoundException.class);

            String token = jwtProvider.generateAccessToken(member, "principal");

            //when
            //then
            assertThatThrownBy(() -> jwtProvider.getAuthentication(token))
                    .isInstanceOf(JwtException.class)
                    .hasCauseInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Cannot found guest for token subject: ");
        }
    }
}