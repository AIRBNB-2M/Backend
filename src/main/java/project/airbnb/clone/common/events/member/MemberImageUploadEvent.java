package project.airbnb.clone.common.events.member;

public record MemberImageUploadEvent(Long memberId, String imageUrl) {
}
