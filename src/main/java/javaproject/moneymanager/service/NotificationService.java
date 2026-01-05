package javaproject.moneymanager.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javaproject.moneymanager.dto.ExpenseDTO;
import javaproject.moneymanager.entity.ProfileEntity;
import javaproject.moneymanager.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
  
  private final EmailService emailService;
  private final ProfileRepository profileRepository;
  private final ExpenseService expenseService;

  @Value("${money.manager.frontend.url}")
  private String frontendUrl;

  // @Scheduled(cron = "0 * * * * *", zone = "Asia/Jakarta") //every day at 10 PM Jakarta time
  // @Scheduled(cron = "0 0 22 * * *", zone = "Asia/Jakarta") //every day at 10 PM Jakarta time
  public void sendDailyIncomeExpenseReminder(){

    log.info("Job started: sendDailyIncomeExpenseReminder");
    List<ProfileEntity> profiles = profileRepository.findAll();
    for(ProfileEntity profile : profiles) {
      String body = "Hi " + profile.getFullName() + ",<br><br>"
          + "This is a friendly reminder to log your daily income and expenses in the Money Manager application. <br><br>"
          + "<a href=" + frontendUrl +  "style='display: inline-block; padding: 10px 20px; font-size: 16px; color: #ffffff; background-color #4CAF50; text-decoration: none; border-radius: 5px; font-weight: bold;'>Go to Money Manager</a>"
          + "<br><br>Best regards,<br>Money Manager Team";
      emailService.sendEmail(profile.getEmail(), "Daily Reminder: Add Your Income and Expenses", body);
    }
    log.info("Job completed: sendDailyIncomeExpenseReminder()");
  }

  // @Scheduled(cron = "0 * * * * *", zone = "Asia/Jakarta") //every day at 10 PM Jakarta time
  // @Scheduled(cron = "0 0 23 * * *", zone = "Asia/Jakarta") //every day at 10 PM Jakarta time
  public void sendDailyExpenseSummary (){
    log.info("Job started: sendDailyExpenseSummary");
    List<ProfileEntity> profiles = profileRepository.findAll();
    for(ProfileEntity profile : profiles) {
      List<ExpenseDTO> todaysExpenses = expenseService.getExpenseForUserOnDate(profile.getId(), LocalDate.now(ZoneId.of("Asia/Jakarta")));
      if (!todaysExpenses.isEmpty())  {
        StringBuilder table = new StringBuilder();
        table.append("<table sytle='border-collapse: collapse; width: 100%;'>");
        table.append("<tr><th style='border: 1px solid #dddddd; text-align: left; padding: 8px;'>No<th style='border: 1px solid #dddddd; text-align: left; padding: 8px;'>Name</th><th style='border: 1px solid #dddddd; text-align: left; padding: 8px;'>Amount</th><th style='border: 1px solid #dddddd; text-align: left; padding: 8px;'>Category</th></tr>");
      // 1px solid #ddd;padding8px;'>Category</th><th style ='border:1px solid #ddd:padding:8px;'>Date</th></tr>");'
        int i = 1;
        for(ExpenseDTO expense : todaysExpenses){
          table.append("<tr>");
          table.append("<td style='border: 1px solid #dddddd; text-align: left; padding: 8px;'>").append(i++).append("</td>");
          table.append("<td style='border: 1px solid #dddddd; text-align: left; padding: 8px;'>").append(expense.getName()).append("</td>");
          table.append("<td style='border: 1px solid #dddddd; text-align: left; padding: 8px;'>").append(expense.getAmount()).append("</td>");
          table.append("<td style='border: 1px solid #dddddd; text-align: left; padding: 8px;'>").append(expense.getCategoryId() != null ? expense.getCategoryName() : "N/A").append("</td>");
          // table.append("<td style='border: 1px solid #dddddd; text-align: left; padding: 8px;'>" + expenseDTO.getCategoryName() + "</td>");
        }
        table.append("</table>");
        String body = "Hi " + profile.getFullName() + ",<br><br>"
            + "Here is the summary of your expenses for today:<br><br>"
            + table
            + "<br>Best regards,<br>Money Manager Team";
        emailService.sendEmail(profile.getEmail(), "Daily Expense Summary", body);
      }
    }
    log.info("Job completed: sendDailyIncomeExpenseReminder()");
  }
}
