package freelancer_platform.service;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender, "noreply@freelancerplatform.com");
    }

    @Test
    @DisplayName("sendLoginNotification: successfully composes and sends security notification email")
    void testSendLoginNotificationSuccess() throws Exception {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        LocalDateTime now = LocalDateTime.of(2026, 9, 17, 14, 30, 0);
        emailService.sendLoginNotification("alice@example.com", "Alice Smith", now);

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender, times(1)).send(captor.capture());

        MimeMessage sentMessage = captor.getValue();
        assertEquals("New Login to Your Freelancer Platform Account", sentMessage.getSubject());
        assertEquals(1, sentMessage.getAllRecipients().length);
        assertEquals("alice@example.com", sentMessage.getAllRecipients()[0].toString());
    }

    @Test
    @DisplayName("sendLoginNotification: handles null mailSender gracefully without throwing exception")
    void testSendLoginNotificationWithNullMailSender() {
        EmailService serviceWithoutSender = new EmailService(null, "noreply@freelancerplatform.com");
        assertDoesNotThrow(() -> serviceWithoutSender.sendLoginNotification(
                "bob@example.com", "Bob", LocalDateTime.now()));
    }

    @Test
    @DisplayName("sendLoginNotification: safely handles MailException without throwing")
    void testSendLoginNotificationMailExceptionHandled() {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailSendException("SMTP server connection failed"))
                .when(mailSender).send(any(MimeMessage.class));

        assertDoesNotThrow(() -> emailService.sendLoginNotification(
                "charlie@example.com", "Charlie", LocalDateTime.now()));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("sendLoginNotification: handles null name and null loginTime safely")
    void testSendLoginNotificationNullParameters() {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertDoesNotThrow(() -> emailService.sendLoginNotification("user@example.com", null, null));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }
}
