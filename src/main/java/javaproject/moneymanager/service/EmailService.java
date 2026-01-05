package javaproject.moneymanager.service;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import javaproject.moneymanager.entity.ProfileEntity;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

  private final JavaMailSender mailSender;
  private final ProfileService profileService;
  private final IncomeService incomeService;

  @Value("${spring.mail.properties.mail.smtp.from}")
  private String fromEmail;

  public void sendEmail(String to, String subject, String body) {
    // Implementation for sending email
    try{
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(fromEmail);
      message.setTo(to);
      message.setSubject(subject);
      message.setText(body);
      mailSender.send(message);

    }catch(Exception e){
      throw new RuntimeException(e.getMessage());
    }
  }

  public void sendEmailWithAttachment(String to, String subject, String body, byte[] attachment, String filename) throws MessagingException  {
      
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true);
    helper.setFrom(fromEmail);
    helper.setTo(to);
    helper.setSubject(subject);
    helper.setText(body);
    helper.addAttachment(filename, new ByteArrayResource(attachment));
    mailSender.send(message);
    
  }

  public void sendIncomeExcelReport() {
    LocalDate now = LocalDate.now();
    sendIncomeExcelReport(now.getYear(), now.getMonthValue());
  }

  public void sendIncomeExcelReport(int year, int month) {
    try {
      ProfileEntity profile = profileService.getCurrentProfile();
      String userEmail = profile.getEmail();
      
      ByteArrayInputStream excelFile = incomeService.generateIncomeExcelForMonth(year, month);
      
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      
      helper.setFrom(fromEmail);
      helper.setTo(userEmail);
      helper.setSubject(String.format("Your Income Excel Report - %04d-%02d", year, month));
      helper.setText(String.format(
        "Please find attached your income report for %04d-%02d.\n\nBest regards,\nMoney Manager Team",
        year,
        month
      ));
      
      ByteArrayResource attachment = new ByteArrayResource(excelFile.readAllBytes());
      helper.addAttachment(String.format("income_details_%04d_%02d.xlsx", year, month), attachment);
      
      mailSender.send(message);
      
    } catch (Exception e) {
      throw new RuntimeException("Failed to send email with Excel attachment: " + e.getMessage());
    }
  }
  
}
