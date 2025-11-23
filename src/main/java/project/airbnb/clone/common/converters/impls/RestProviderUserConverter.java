package project.airbnb.clone.common.converters.impls;

import project.airbnb.clone.common.converters.ProviderUserConverter;
import project.airbnb.clone.common.converters.ProviderUserRequest;
import project.airbnb.clone.entity.Member;
import project.airbnb.clone.model.ProviderUser;
import project.airbnb.clone.model.RestUser;

public class RestProviderUserConverter implements ProviderUserConverter<ProviderUserRequest, ProviderUser> {

    @Override
    public ProviderUser converter(ProviderUserRequest providerUserRequest) {

        Member member = providerUserRequest.member();

        if (member == null) {
            return null;
        }

        return RestUser.builder()
                       .username(member.getEmail())
                       .email(member.getEmail())
                       .password(member.getPassword())
                       .provider("none")
                       .build();
    }
}
