package project.airbnb.clone.consts;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {

    GUEST("ROLE_GUEST"),
    HOST("ROLE_HOST"),
    ADMIN("ROLE_ADMIN");

    private final String roleName;
}
