package freelancer_platform.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final JavaMailSender mailSender;
    private final String fromEmail;

    public EmailService(
            @Autowired(required = false) JavaMailSender mailSender,
            @Value("${app.mail.from:noreply@freelancerplatform.com}") String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    /**
     * Sends a security notification email upon a successful login.
     * Guaranteed never to throw exceptions that disrupt user authentication.
     *
     * @param recipientEmail user's registered email address
     * @param recipientName  user's name
     * @param loginTime      timestamp of the successful login
     */
    public void sendLoginNotification(String recipientEmail, String recipientName, LocalDateTime loginTime) {
        if (mailSender == null) {
            logger.info("JavaMailSender is not configured; skipping login security notification for: {}", maskEmail(recipientEmail));
            return;
        }

        try {
            String safeName = (recipientName != null && !recipientName.isBlank()) ? recipientName.trim() : "User";
            LocalDateTime effectiveTime = (loginTime != null) ? loginTime : LocalDateTime.now();
            String formattedTime = effectiveTime.format(DATE_TIME_FORMATTER) + " UTC";

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(recipientEmail);
            helper.setSubject("New Login to Your Freelancer Platform Account");

            String textContent = buildPlainTextBody(safeName, recipientEmail, formattedTime);
            String htmlContent = buildHtmlBody(safeName, recipientEmail, formattedTime);

            helper.setText(textContent, htmlContent);

            mailSender.send(message);
            logger.info("Login security notification email sent successfully to {}", maskEmail(recipientEmail));
        } catch (MailException | MessagingException e) {
            logger.warn("Failed to send login security notification email to {}: {}", maskEmail(recipientEmail), e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error occurred while dispatching login security notification to {}: {}", maskEmail(recipientEmail), e.getMessage());
        }
    }

    private String buildPlainTextBody(String name, String email, String timestamp) {
        return "Hello " + name + ",\n\n"
                + "A new successful login to your Freelancer Platform account has occurred.\n\n"
                + "Login Details:\n"
                + "- Account Email: " + email + "\n"
                + "- Login Date & Time: " + timestamp + "\n\n"
                + "Security Notice:\n"
                + "If you did not perform this login, please change your password immediately.\n\n"
                + "Best regards,\n"
                + "Freelancer Platform Security Team";
    }

    private String buildHtmlBody(String name, String email, String timestamp) {
        return "<!DOCTYPE html>\n"
                + "<html lang=\"en\">\n"
                + "<head>\n"
                + "<meta charset=\"UTF-8\">\n"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                + "<title>Security Alert: New Login</title>\n"
                + "<style>\n"
                + "  body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f4f7f6; margin: 0; padding: 24px; color: #1e293b; }\n"
                + "  .email-container { max-width: 580px; margin: 0 auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08); border: 1px solid #e2e8f0; }\n"
                + "  .header { background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%); padding: 32px 28px; text-align: center; color: #ffffff; }\n"
                + "  .header h1 { margin: 0; font-size: 22px; font-weight: 700; letter-spacing: -0.5px; }\n"
                + "  .content { padding: 32px 28px; line-height: 1.6; }\n"
                + "  .greeting { font-size: 18px; font-weight: 600; margin-bottom: 16px; color: #0f172a; }\n"
                + "  .details-card { background: #f8fafc; border-left: 4px solid #3b82f6; border-radius: 6px; padding: 18px; margin: 20px 0; }\n"
                + "  .detail-row { margin-bottom: 8px; font-size: 14px; }\n"
                + "  .detail-row:last-child { margin-bottom: 0; }\n"
                + "  .detail-label { font-weight: 600; color: #475569; width: 140px; display: inline-block; }\n"
                + "  .detail-value { color: #0f172a; font-family: monospace, Consolas, sans-serif; }\n"
                + "  .warning-box { background: #fef2f2; border: 1px solid #fecaca; border-radius: 8px; padding: 16px; margin-top: 24px; color: #991b1b; font-size: 14px; }\n"
                + "  .warning-title { font-weight: 700; margin-bottom: 4px; display: flex; align-items: center; }\n"
                + "  .footer { padding: 20px 28px; background: #f8fafc; border-top: 1px solid #e2e8f0; font-size: 12px; color: #64748b; text-align: center; }\n"
                + "</style>\n"
                + "</head>\n"
                + "<body>\n"
                + "<div class=\"email-container\">\n"
                + "  <div class=\"header\">\n"
                + "    <h1>Security Alert: New Login</h1>\n"
                + "  </div>\n"
                + "  <div class=\"content\">\n"
                + "    <p class=\"greeting\">Hello " + escapeHtml(name) + ",</p>\n"
                + "    <p>A new successful login to your Freelancer Platform account has occurred.</p>\n"
                + "    <div class=\"details-card\">\n"
                + "      <div class=\"detail-row\"><span class=\"detail-label\">Registered Email:</span> <span class=\"detail-value\">" + escapeHtml(email) + "</span></div>\n"
                + "      <div class=\"detail-row\"><span class=\"detail-label\">Login Time:</span> <span class=\"detail-value\">" + escapeHtml(timestamp) + "</span></div>\n"
                + "    </div>\n"
                + "    <div class=\"warning-box\">\n"
                + "      <div class=\"warning-title\">⚠️ Important Security Notice</div>\n"
                + "      <div>If you did not perform this login, please change your password immediately.</div>\n"
                + "    </div>\n"
                + "  </div>\n"
                + "  <div class=\"footer\">\n"
                + "    &copy; Freelancer Platform &bull; Automated Security Notification &bull; Do not reply directly to this email\n"
                + "  </div>\n"
                + "</div>\n"
                + "</body>\n"
                + "</html>";
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "unknown";
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 2) {
            return "***" + email.substring(atIndex);
        }
        return email.substring(0, 2) + "***" + email.substring(atIndex);
    }
}
