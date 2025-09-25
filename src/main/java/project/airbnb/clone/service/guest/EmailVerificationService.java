package project.airbnb.clone.service.guest;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.airbnb.clone.entity.Guest;
import project.airbnb.clone.repository.jpa.GuestRepository;
import project.airbnb.clone.repository.redis.RedisRepository;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailVerificationService {

    private final JavaMailSender javaMailSender;
    private final GuestRepository guestRepository;
    private final RedisRepository redisRepository;

    public void sendEmail(Long guestId) {
        Guest guest = guestRepository.findById(guestId)
                                     .orElseThrow(() -> new EntityNotFoundException("Guest with id " + guestId + "cannot be found"));

        String token = UUID.randomUUID().toString();
        String key = getRedisKey(token);

        //TODO : 배포 주소 연결
        String link = "http://localhost:8081/api/auth/email/verify?token=" + token;
        String subject = "[Airbnb-2M] 이메일 인증을 완료해주세요.";
        String html = generateHtml(link);

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(guest.getEmail());
            helper.setSubject(subject);
            helper.setText(html, true);
            helper.setReplyTo("no-reply@airbnb-2m.com");

            javaMailSender.send(mimeMessage);
            redisRepository.setValue(key, guestId.toString(), Duration.ofHours(1));

        } catch (MessagingException e) {
            throw new MailSendException("메일 전송 과정에서 오류가 발생했습니다, " + e.getMessage(), e);
        }
    }

    @Transactional
    public boolean verifyToken(String token) {
        String key = getRedisKey(token);
        String value = redisRepository.getValue(key);

        if (value == null) {
            return false;
        }

        Long guestId = Long.valueOf(value);
        Guest guest = guestRepository.findById(guestId)
                                     .orElseThrow(() -> new EntityNotFoundException("Guest with id " + guestId + "cannot be found"));
        guest.verifyEmail();
        redisRepository.deleteValue(key);

        return true;
    }

    private String getRedisKey(String token) {
        return "email:verify:" + token;
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
                            <p>회원가입을 완료하시려면 아래 버튼을 클릭해 이메일 인증을 진행해주세요.</p>
                            <p style="text-align: center;">
                                <a href="%s" class="button">이메일 인증하기</a>
                            </p>
                            <p>만약 버튼이 작동하지 않는다면, 아래 링크를 복사해서 브라우저에 붙여넣어주세요:</p>
                            <p><a href="%s">%s</a></p>
                            <p style="color:#555; font-size:13px;">※ 본 메일은 발신 전용으로 회신이 불가합니다.</p>
                        </div>
                        <div class="footer">
                            &copy; 2025 Airbnb-2M. All rights reserved.
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(link, link, link);
    }
}
