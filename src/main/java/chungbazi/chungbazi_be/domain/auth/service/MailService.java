package chungbazi.chungbazi_be.domain.auth.service;

import chungbazi.chungbazi_be.domain.user.entity.User;
import chungbazi.chungbazi_be.domain.user.support.UserHelper;
import chungbazi.chungbazi_be.global.apiPayload.code.status.ErrorStatus;
import chungbazi.chungbazi_be.global.apiPayload.exception.handler.BadRequestHandler;
import jakarta.activation.DataSource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Random;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MailService {

    private static final String AUTH_ATTEMPT_PREFIX = "auth:attempt:";
    private static final int MAX_ATTEMPTS = 5;
    private static final Duration ATTEMPT_TTL = Duration.ofMinutes(30);

    private final RedisTemplate<String, String> redisTemplate;

    private static final String AUTH_CODE_PREFIX = "AuthCode ";
    private static final long authCodeExpirationMillis = 1000 * 60 * 30;

    private final TokenAuthService tokenAuthService;
    private final UserHelper userHelper;
    private final JavaMailSender emailSender;

    public void sendCodeToEmailWithNoAuthorization(String email) {
        String title = "청바지 이메일 인증 번호";
        String authCode = createCode();

        try {
            sendHtmlEmailWithCode(email, title, authCode);
        } catch (Exception e) {
            log.error("이메일 전송 실패: {}", e.getMessage());
            throw new BadRequestHandler(ErrorStatus.UNABLE_TO_SEND_EMAIL);
        }

        tokenAuthService.setAuthCode(
                AUTH_CODE_PREFIX + email,
                authCode,
                Duration.ofMillis(authCodeExpirationMillis)
        );
    }

    public void sendCodeToEmail() {
        User user = userHelper.getAuthenticatedUser();
        String toEmail = user.getEmail();
        String title = "청바지 이메일 인증 번호";
        String authCode = createCode();

        try {
            sendHtmlEmailWithCode(toEmail, title, authCode);
        } catch (Exception e) {
            log.error("이메일 전송 실패: {}", e.getMessage());
            throw new BadRequestHandler(ErrorStatus.UNABLE_TO_SEND_EMAIL);
        }

        tokenAuthService.setAuthCode(
                AUTH_CODE_PREFIX + toEmail,
                authCode,
                Duration.ofMillis(authCodeExpirationMillis)
        );
    }

    private void sendHtmlEmailWithCode(String toEmail, String title, String authCode)
            throws MessagingException, IOException {

        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(toEmail);
        helper.setSubject(title);

        String html = getHtmlContentWithCode(authCode);
        helper.setText(html, true);

        addInlineResource(helper, "emailCharacter", "static/images/emailCharacter.png", "image/png");

        emailSender.send(message);
    }

    private String getHtmlContentWithCode(String authCode) {
        try {
            ClassPathResource resource = new ClassPathResource("templates/email.html");
            String html = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            return html.replace("${code}", authCode);
        } catch (IOException e) {
            log.error("이메일 템플릿 로딩 실패: {}", e.getMessage());
            throw new BadRequestHandler(ErrorStatus.UNABLE_TO_READ_EMAIL_TEMPLATE);
        }
    }

    private void addInlineResource(MimeMessageHelper helper, String contentId, String classpathLocation, String mimeType)
            throws IOException, MessagingException {

        ClassPathResource resource = new ClassPathResource(classpathLocation);
        DataSource dataSource = new DataSource() {
            @Override
            public InputStream getInputStream() throws IOException {
                return resource.getInputStream();
            }

            @Override
            public OutputStream getOutputStream() throws IOException {
                throw new IOException("OutputStream not supported");
            }

            @Override
            public String getContentType() {
                return mimeType;
            }

            @Override
            public String getName() {
                return resource.getFilename();
            }
        };

        helper.addInline(contentId, dataSource);
    }

    private String createCode() {
        int length = 6;
        try {
            Random random = SecureRandom.getInstanceStrong();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < length; i++) {
                builder.append(random.nextInt(10));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new BadRequestHandler(ErrorStatus.NO_SUCH_ALGORITHM);
        }
    }

    public void verifiedCode(String dtoEmail, String authCode) {
        String email = dtoEmail;

        String key = AUTH_ATTEMPT_PREFIX + email;

        Integer attempts = redisTemplate.opsForValue().get(key) != null ?
                Integer.parseInt(redisTemplate.opsForValue().get(key)) : 0;

        if (attempts >= MAX_ATTEMPTS) {
            throw new BadRequestHandler(ErrorStatus.ACCOUNT_LOCKED);
        }

        String redisAuthCode = tokenAuthService.getAuthCode(AUTH_CODE_PREFIX + email);
        boolean authResult = redisAuthCode != null && redisAuthCode.equals(authCode);

        if (!authResult) {
            redisTemplate.opsForValue().set(key, String.valueOf(attempts + 1), ATTEMPT_TTL);
            throw new BadRequestHandler(ErrorStatus.INVALID_AUTHCODE);
        }

        redisTemplate.delete(key);
    }
}
