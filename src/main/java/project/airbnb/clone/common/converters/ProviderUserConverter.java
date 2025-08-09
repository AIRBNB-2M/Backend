package project.airbnb.clone.common.converters;

import project.airbnb.clone.model.ProviderUser;

public interface ProviderUserConverter<T extends ProviderUserRequest, R extends ProviderUser> {
    R converter(T t);
}
