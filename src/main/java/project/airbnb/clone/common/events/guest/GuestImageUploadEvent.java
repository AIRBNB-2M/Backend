package project.airbnb.clone.common.events.guest;

public record GuestImageUploadEvent(Long guestId, String imageUrl) {
}
