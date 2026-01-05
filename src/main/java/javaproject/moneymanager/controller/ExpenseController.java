package javaproject.moneymanager.controller;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javaproject.moneymanager.dto.ExpenseDTO;
import javaproject.moneymanager.service.ExpenseService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/expenses")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ExpenseController {

  private final ExpenseService expenseService;

  @PostMapping
  public ResponseEntity<ExpenseDTO> addExpense(@RequestBody ExpenseDTO dto){
    ExpenseDTO saved = expenseService.addExpense(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);

  }  
  @GetMapping
  public ResponseEntity<List<ExpenseDTO>> getExpenses(){
    List<ExpenseDTO> expenses = expenseService.getCurrentMonthExpensesForCurrentUser();
    return ResponseEntity.ok(expenses);

  }

  @GetMapping("/by-month")
  public ResponseEntity<List<ExpenseDTO>> getExpensesByMonth(
    @RequestParam int year,
    @RequestParam int month
  ){
    List<ExpenseDTO> expenses = expenseService.getExpensesForMonth(year, month);
    return ResponseEntity.ok(expenses);

  }

  @GetMapping("/download/excel")
  public ResponseEntity<InputStreamResource> downloadExpenseExcel(
    @RequestParam(required = false) Integer year,
    @RequestParam(required = false) Integer month
  ){
    try {
      LocalDate now = LocalDate.now();
      int targetYear = year != null ? year : now.getYear();
      int targetMonth = month != null ? month : now.getMonthValue();

      ByteArrayInputStream in = expenseService.generateExpenseExcelForMonth(targetYear, targetMonth);

      HttpHeaders headers = new HttpHeaders();
      headers.add("Content-Disposition", "attachment; filename=expense_details.xlsx");

      return ResponseEntity.ok()
        .headers(headers)
        .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .body(new InputStreamResource(in));
    } catch (Exception e) {
      throw new RuntimeException("Failed to generate Excel file: " + e.getMessage());
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteExpense(@PathVariable Long id){
    expenseService.deleteExpense(id);
    return ResponseEntity.noContent().build();

  }
}
