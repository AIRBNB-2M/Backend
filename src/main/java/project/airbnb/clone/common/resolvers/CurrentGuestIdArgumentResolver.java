package project.airbnb.clone.common.resolvers;

import org.springframework.core.MethodParameter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import project.airbnb.clone.common.annotations.CurrentGuestId;
import project.airbnb.clone.model.AuthProviderUser;
import project.airbnb.clone.model.PrincipalUser;

public class CurrentGuestIdArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentGuestId.class) &&
                Long.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        CurrentGuestId annotation = parameter.getParameterAnnotation(CurrentGuestId.class);
        Assert.notNull(annotation, "Cannot be empty @CurrentGuestId");

        Authentication authentication = SecurityContextHolder.getContextHolderStrategy()
                                                             .getContext()
                                                             .getAuthentication();
        boolean required = annotation.required();

        if (authentication == null) {
            if (required) {
                throw new AuthenticationCredentialsNotFoundException("Authentication is required but not found");
            }
            return null;
        }

        if (authentication.getPrincipal() instanceof PrincipalUser principalUser) {
            if (principalUser.providerUser() instanceof AuthProviderUser authProviderUser) {
                return authProviderUser.getId();
            }
        }

        if (required) {
            throw new AccessDeniedException("Principal type is invalid");
        }

        return null;
    }
}
