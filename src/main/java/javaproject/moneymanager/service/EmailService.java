package javaproject.moneymanager.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

  private static final Logger log = LoggerFactory.getLogger(EmailService.class);

  private final JavaMailSender mailSender;

  @Value("${spring.mail.properties.mail.smtp.from:no-reply@money-manager.local}")
  private String fromEmail;

  @Value("${app.mail.enabled:true}")
  private boolean mailEnabled;

  public void sendEmail(String to, String subject, String body) {
    if (!mailEnabled) {
      log.info("Email sending disabled (app.mail.enabled=false). Skipping send to {} (subject={})", to, subject);
      return;
    }

    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(fromEmail);
      message.setTo(to);
      message.setSubject(subject);
      message.setText(body);
      mailSender.send(message);
      log.info("Email sent to {} (subject={})", to, subject);
    } catch (Exception e) {
      // Log and continue — do not fail the application flow because of mail problems
      log.error("Failed to send email to {} (subject={}). Error: {}", to, subject, e.getMessage());
    }
  }

}
