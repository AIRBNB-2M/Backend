package project.airbnb.clone.service.member;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import project.airbnb.clone.common.events.member.MemberImageUploadEvent;
import project.airbnb.clone.common.events.member.MemberProfileImageChangedEvent;
import project.airbnb.clone.common.exceptions.BusinessException;
import project.airbnb.clone.common.exceptions.ErrorCode;
import project.airbnb.clone.common.exceptions.factory.MemberExceptions;
import project.airbnb.clone.consts.SocialType;
import project.airbnb.clone.dto.PageResponseDto;
import project.airbnb.clone.dto.member.*;
import project.airbnb.clone.entity.Member;
import project.airbnb.clone.model.ProviderUser;
import project.airbnb.clone.repository.dto.DefaultProfileQueryDto;
import project.airbnb.clone.repository.jpa.MemberRepository;
import project.airbnb.clone.repository.query.MemberQueryRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final MemberQueryRepository memberQueryRepository;

    /**
     * OAuth 가입
     */
    @Transactional
    public void register(ProviderUser providerUser) {
        String email = providerUser.getEmail();
        SocialType socialType = SocialType.from(providerUser.getProvider());

        if (memberRepository.existsByEmailAndSocialType(email, socialType)) {
            return;
        }

        validateExistsEmail(email);

        String encodePassword = encodePassword(providerUser.getPassword());
        Member member = providerUser.toEntity(encodePassword);

        memberRepository.save(member);

        if (providerUser.getImageUrl() != null) {
            eventPublisher.publishEvent(new MemberImageUploadEvent(member.getId(), providerUser.getImageUrl()));
        }
    }

    /**
     * REST 가입
     */
    @Transactional
    public void register(SignupRequestDto signupRequestDto) {
        validateExistsEmail(signupRequestDto.email());

        String encodePassword = encodePassword(signupRequestDto.password());
        Member member = signupRequestDto.toEntity(encodePassword);

        memberRepository.save(member);
    }

    @Transactional
    public EditProfileResDto editMyProfile(Long memberId, MultipartFile imageFile, EditProfileReqDto profileReqDto) {
        Member member = memberRepository.findById(memberId)
                                        .orElseThrow(() -> MemberExceptions.notFoundById(memberId));

        if (profileReqDto.isProfileImageChanged()) {
            eventPublisher.publishEvent(new MemberProfileImageChangedEvent(memberId, member.getProfileUrl(), imageFile));
        }

        member.updateProfile(profileReqDto.name(), profileReqDto.aboutMe());
        return new EditProfileResDto(member.getName(), member.getProfileUrl(), member.getAboutMe());
    }

    public DefaultProfileResDto getDefaultProfile(Long memberId) {
        DefaultProfileQueryDto profileQueryDto = memberQueryRepository.getDefaultProfile(memberId)
                                                                      .orElseThrow(() -> MemberExceptions.notFoundById(memberId));
        return DefaultProfileResDto.from(profileQueryDto);
    }

    public ChatMembersSearchResDto findMembersByName(String name) {
        List<ChatMemberSearchDto> members = memberQueryRepository.findMembersByName(name);
        return new ChatMembersSearchResDto(members);
    }

    public PageResponseDto<TripHistoryResDto> getTripsHistory(Long memberId, Pageable pageable) {
        Page<TripHistoryResDto> result = memberQueryRepository.getTripsHistory(memberId, pageable);

        return PageResponseDto.<TripHistoryResDto>builder()
                              .contents(result.getContent())
                              .pageNumber(pageable.getPageNumber())
                              .pageSize(pageable.getPageSize())
                              .total(result.getTotalElements())
                              .build();
    }

    private void validateExistsEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }

    private String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
