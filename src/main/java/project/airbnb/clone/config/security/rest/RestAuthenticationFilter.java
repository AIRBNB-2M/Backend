package project.airbnb.clone.config.security.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import project.airbnb.clone.dto.member.LoginRequestDto;

import java.io.IOException;
import java.util.regex.Pattern;

public class RestAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^.*(?=^.{8,15}$)(?=.*\\d)(?=.*[a-zA-Z])(?=.*[!@#$%^&+=]).*$");
    //숫자, 문자, 특수문자 포함 8~15자리

    private final ObjectMapper objectMapper;

    public RestAuthenticationFilter(ObjectMapper objectMapper) {
        super("/api/auth/login");
        this.objectMapper = objectMapper;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {

        if (!HttpMethod.POST.name().equals(request.getMethod())) {
            throw new IllegalArgumentException("Authentication method is not supported");
        }

        LoginRequestDto loginRequest = objectMapper.readValue(request.getReader(), LoginRequestDto.class);

        String email = loginRequest.email();
        String password = loginRequest.password();

        if (!EMAIL_PATTERN.matcher(email).matches() || !PASSWORD_PATTERN.matcher(password).matches()) {
            throw new BadCredentialsException("Email or Password is not provided");
        }

        RestAuthenticationToken authenticationToken =
                RestAuthenticationToken.unauthenticated(email, password);

        return getAuthenticationManager().authenticate(authenticationToken);
    }
}
