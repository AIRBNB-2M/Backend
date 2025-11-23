package project.airbnb.clone.service.member;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.airbnb.clone.entity.Member;
import project.airbnb.clone.repository.dto.EmailVerification;
import project.airbnb.clone.repository.jpa.MemberRepository;
import project.airbnb.clone.repository.redis.EmailVerificationRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailVerificationService {

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frondEndUrl;

    @Value("${app.base-url:http://localhost:8081}")
    private String baseUrl;

    private final JavaMailSender javaMailSender;
    private final MemberRepository memberRepository;
    private final EmailVerificationRepository emailVerificationRepository;

    @Async
    @Retryable(retryFor = MailSendException.class, backoff = @Backoff(delay = 1000))
    public void sendEmail(Long memberId) {
        Member member = memberRepository.findById(memberId)
                                        .orElseThrow(() -> new EntityNotFoundException("Guest with id " + memberId + "cannot be found"));

        String token = UUID.randomUUID().toString();

        String link = baseUrl + "/api/auth/email/verify?token=" + token;
        String subject = "[Airbnb-2M] 이메일 인증을 완료해주세요.";
        String html = generateHtml(link);
        String email = member.getEmail();

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(html, true);
            helper.setReplyTo("no-reply@airbnb-2m.com");

            int retryCount = RetrySynchronizationManager.getContext().getRetryCount();
            log.debug("이메일 인증 링크 전송: {}, 시도 횟수 {}", email, retryCount);

            javaMailSender.send(mimeMessage);
            emailVerificationRepository.save(new EmailVerification(token, memberId));

            log.debug("이메일 인증 링크 전송 성공: {}", email);

        } catch (MessagingException e) {
            log.warn("이메일 인증 링크 전송 실패: {}", email);
            throw new MailSendException("메일 전송 과정에서 오류가 발생했습니다, " + e.getMessage(), e);
        }
    }

    @Recover
    public void recoverSendEmail(MailException ex) {
        log.error("[메일 전송 실패]", ex);
    }

    @Transactional
    public String verifyToken(String token) {
        EmailVerification verification = emailVerificationRepository.findById(token).orElse(null);

        String redirectUrl = frondEndUrl + "/users/profile?emailVerify=";

        if (verification == null) {
            return redirectUrl + "failed";
        }

        Long memberId = verification.getMemberId();
        Member member = memberRepository.findById(memberId)
                                        .orElseThrow(() -> new EntityNotFoundException("Guest with id " + memberId + "cannot be found"));
        member.verifyEmail();
        emailVerificationRepository.delete(verification);

        return redirectUrl + "success";
    }

    private String generateHtml(String link) {
        return """
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            background-color: #f7f7f7;
                            margin: 0;
                            padding: 0;
                        }
                        .container {
                            max-width: 600px;
                            margin: 30px auto;
                            background: #ffffff;
                            border-radius: 10px;
                            overflow: hidden;
                            box-shadow: 0 4px 10px rgba(0,0,0,0.1);
                        }
                        .header {
                            background-color: #FF5A5F;
                            color: white;
                            padding: 20px;
                            text-align: center;
                            font-size: 24px;
                            font-weight: bold;
                        }
                        .content {
                            padding: 30px;
                            color: #333;
                        }
                        .button {
                            display: inline-block;
                            padding: 12px 24px;
                            margin: 20px 0;
                            background-color: #FF5A5F;
                            color: #fff !important;
                            text-decoration: none;
                            border-radius: 6px;
                            font-size: 16px;
                        }
                        .footer {
                            padding: 20px;
                            font-size: 12px;
                            text-align: center;
                            color: #888;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">Airbnb-2M</div>
                        <div class="content">
                            <p>안녕하세요,</p>
                            <p>인증을 완료하시려면 아래 버튼을 클릭해 인증을 진행해주세요.</p>
                            <p style="text-align: center;">
                                <a href="%s" class="button">인증하기</a>
                            </p>
                            <p style="color:#555; font-size:13px;">※ 본 메일은 발신 전용으로 회신이 불가합니다.</p>
                        </div>
                        <div class="footer">
                            &copy; 2025 Airbnb-2M. All rights reserved.
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(link);
    }
}
