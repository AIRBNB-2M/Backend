package project.airbnb.clone.common.converters.impls;

import project.airbnb.clone.common.converters.ProviderUserConverter;
import project.airbnb.clone.common.converters.ProviderUserRequest;
import project.airbnb.clone.entity.Guest;
import project.airbnb.clone.model.ProviderUser;
import project.airbnb.clone.model.RestUser;

public class RestProviderUserConverter implements ProviderUserConverter<ProviderUserRequest, ProviderUser> {

    @Override
    public ProviderUser converter(ProviderUserRequest providerUserRequest) {

        Guest guest = providerUserRequest.guest();

        if (guest == null) {
            return null;
        }

        return RestUser.builder()
                       .username(guest.getEmail())
                       .email(guest.getEmail())
                       .password(guest.getPassword())
                       .provider("none")
                       .build();
    }
}
