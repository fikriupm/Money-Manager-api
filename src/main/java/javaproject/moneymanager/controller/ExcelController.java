package javaproject.moneymanager.controller;

import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import javaproject.moneymanager.service.ExcelSerivce;
import javaproject.moneymanager.service.ExpenseService;
import javaproject.moneymanager.service.IncomeService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/excel")
@RequiredArgsConstructor
public class ExcelController {

  private final ExcelSerivce excelSerivce;
  private final IncomeService incomeService;
  private final ExpenseService expenseService;

  @GetMapping("/download/income")
  public void downloadIncomeExcel(HttpServletResponse response) throws IOException {
    // Implementation for downloading Excel report
    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setHeader("Content-Disposition", "attachment; filename=income_report.xlsx");
    excelSerivce.writeIncomesToExcel(response.getOutputStream(), incomeService.getCurrentMonthIncomesForCurrentUser());
  }

  @GetMapping("/download/expense")
  public void downloadExpenseExcel(HttpServletResponse response) throws IOException {
    // Implementation for downloading Excel report
    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setHeader("Content-Disposition", "attachment; filename=expense_report.xlsx");
    excelSerivce.writeExpensesToExcel(response.getOutputStream(), expenseService.getCurrentMonthExpensesForCurrentUser());
  }
}
