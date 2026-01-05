package javaproject.moneymanager.controller;

import java.io.IOException;
import java.io.ByteArrayInputStream;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;

import jakarta.mail.MessagingException;
import javaproject.moneymanager.entity.ProfileEntity;
import javaproject.moneymanager.service.EmailService;
import javaproject.moneymanager.service.ExcelSerivce;
import javaproject.moneymanager.service.ExpenseService;
import javaproject.moneymanager.service.IncomeService;
import javaproject.moneymanager.service.ProfileService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/email")
// @CrossOrigin(origins = "*", allowedHeaders = "*")
public class EmailController {
  
  private final EmailService emailService;
  private final IncomeService incomeService;
  private final ExpenseService expenseService;
  private final ExcelSerivce excelSerivce;
  private final ProfileService profileService;

  @GetMapping("/income-excel")
  public ResponseEntity<Void> emailIncomeExcel(
    @RequestParam(required = false) Integer year,
    @RequestParam(required = false) Integer month
  ) throws IOException, MessagingException {
    LocalDate now = LocalDate.now();
    int targetYear = year != null ? year : now.getYear();
    int targetMonth = month != null ? month : now.getMonthValue();

    ProfileEntity profile = profileService.getCurrentProfile();
      ByteArrayInputStream in = incomeService.generateIncomeExcelForMonth(targetYear, targetMonth);
    emailService.sendEmailWithAttachment(
      profile.getEmail(),
      String.format("Income Excel Report - %04d-%02d", targetYear, targetMonth),
      String.format(
        "Please find attached your income report for %04d-%02d.\n\nBest regards,\nMoney Manager Team",
        targetYear,
        targetMonth
      ),
        in.readAllBytes(),
      String.format("income_report_%04d_%02d.xlsx", targetYear, targetMonth)
    );
    return ResponseEntity.ok().build();
  }

  @GetMapping("/expense-excel")
  public ResponseEntity<Void> emailExpenseExcel(
    @RequestParam(required = false) Integer year,
    @RequestParam(required = false) Integer month
  ) throws IOException, MessagingException {
    LocalDate now = LocalDate.now();
    int targetYear = year != null ? year : now.getYear();
    int targetMonth = month != null ? month : now.getMonthValue();

    ProfileEntity profile = profileService.getCurrentProfile();
    ByteArrayInputStream in = expenseService.generateExpenseExcelForMonth(targetYear, targetMonth);
    emailService.sendEmailWithAttachment(
      profile.getEmail(),
      String.format("Expense Excel Report - %04d-%02d", targetYear, targetMonth),
      String.format(
        "Please find attached your expense report for %04d-%02d.\n\nBest regards,\nMoney Manager Team",
        targetYear,
        targetMonth
      ),
      in.readAllBytes(),
      String.format("expense_report_%04d_%02d.xlsx", targetYear, targetMonth)
    );
    return ResponseEntity.ok().build();
  }
    
  // @PostMapping("/excel-income")
  // public ResponseEntity<String> emailIncome_Excel(
  //   @RequestParam(required = false) Integer year,
  //   @RequestParam(required = false) Integer month
  // ) {
  //   try {
  //     LocalDate now = LocalDate.now();
  //     int targetYear = year != null ? year : now.getYear();
  //     int targetMonth = month != null ? month : now.getMonthValue();

  //     emailService.sendIncomeExcelReport(targetYear, targetMonth);
  //     return ResponseEntity.ok("Income details emailed successfully");
  //   } catch (Exception e) {
  //     throw new RuntimeException("Failed to send income report: " + e.getMessage());
  //   }
  // }
}
