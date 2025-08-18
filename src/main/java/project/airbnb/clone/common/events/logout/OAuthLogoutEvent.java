package project.airbnb.clone.common.events.logout;

import project.airbnb.clone.consts.SocialType;

public record OAuthLogoutEvent(SocialType socialType) {
}
