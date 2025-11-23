package project.airbnb.clone.controller.members;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.airbnb.clone.common.annotations.CurrentMemberId;
import project.airbnb.clone.dto.PageResponseDto;
import project.airbnb.clone.dto.member.*;
import project.airbnb.clone.service.member.MemberService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/me")
    public ResponseEntity<DefaultProfileResDto> getMyProfile(@CurrentMemberId Long memberId) {
        DefaultProfileResDto response = memberService.getDefaultProfile(memberId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<EditProfileResDto> editMyProfile(@CurrentMemberId Long memberId,
                                                           @RequestPart(value = "profileImage", required = false) MultipartFile imageFile,
                                                           @Valid @RequestPart("editProfileRequest") EditProfileReqDto profileReqDto) {
        EditProfileResDto response = memberService.editMyProfile(memberId, imageFile, profileReqDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<ChatMembersSearchResDto> findMembersByName(@RequestParam("name") String name) {
        ChatMembersSearchResDto response = memberService.findMembersByName(name);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me/trips/past")
    public ResponseEntity<PageResponseDto<TripHistoryResDto>> getTripsHistory(@CurrentMemberId Long memberId,
                                                                              Pageable pageable) {
        PageResponseDto<TripHistoryResDto> response = memberService.getTripsHistory(memberId, pageable);
        return ResponseEntity.ok(response);
    }
}
