package project.airbnb.clone.repository.dto.redis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@Builder
@AllArgsConstructor
@RedisHash(value = "payment:temp", timeToLive = 600)
public class TempPayment {

    @Id
    private String orderId;

    private Integer amount;

    public boolean notEqualsAmount(Integer amount) {
        return !this.amount.equals(amount);
    }
}
