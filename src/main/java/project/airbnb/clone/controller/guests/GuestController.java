package project.airbnb.clone.controller.guests;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import project.airbnb.clone.common.annotations.CurrentGuestId;
import project.airbnb.clone.dto.guest.ChatGuestsSearchResDto;
import project.airbnb.clone.dto.guest.DefaultProfileResDto;
import project.airbnb.clone.dto.guest.EditProfileReqDto;
import project.airbnb.clone.dto.guest.EditProfileResDto;
import project.airbnb.clone.service.guest.GuestService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/guests")
public class GuestController {

    private final GuestService guestService;

    @GetMapping("/me")
    public ResponseEntity<DefaultProfileResDto> getMyProfile(@CurrentGuestId Long guestId) {
        DefaultProfileResDto response = guestService.getDefaultProfile(guestId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<EditProfileResDto> editMyProfile(@CurrentGuestId Long guestId,
                                                           @RequestPart(value = "profileImage", required = false) MultipartFile imageFile,
                                                           @Valid @RequestPart("editProfileRequest") EditProfileReqDto profileReqDto) {
        EditProfileResDto response = guestService.editMyProfile(guestId, imageFile, profileReqDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<ChatGuestsSearchResDto> findGuestsByName(@RequestParam("name") String name) {
        ChatGuestsSearchResDto response = guestService.findGuestsByName(name);
        return ResponseEntity.ok(response);
    }
}
