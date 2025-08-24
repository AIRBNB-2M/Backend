package project.airbnb.clone.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import project.airbnb.clone.common.jwt.JwtProvider;
import project.airbnb.clone.repository.redis.RedisRepository;

@Disabled
public abstract class ControllerTestSupport {

    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected MockMvc mockMvc;

    @MockitoBean protected RedisRepository redisRepository;
    @MockitoBean protected JwtProvider jwtProvider;

    protected String creatJson(Object dto) throws JsonProcessingException {
        return objectMapper.writeValueAsString(dto);
    }
}
