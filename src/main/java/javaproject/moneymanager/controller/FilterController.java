package javaproject.moneymanager.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javaproject.moneymanager.dto.ExpenseDTO;
import javaproject.moneymanager.dto.FilterDTO;
import javaproject.moneymanager.dto.IncomeDTO;
import javaproject.moneymanager.service.ExpenseService;
import javaproject.moneymanager.service.IncomeService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/filter")
public class FilterController {

  private final IncomeService incomeService;
  private final ExpenseService expenseService;

  @PostMapping
  public ResponseEntity<?> filterTransactions(@RequestBody FilterDTO filter){

    //preparing the data or validation
    LocalDate startDate = filter.getStartDate() != null ? filter.getStartDate() : LocalDate.of(1900, 1, 1);
    LocalDate endDate = filter.getEndDate() != null ? filter.getEndDate() : LocalDate.now();
    String keyword = filter.getKeyword() != null ? filter.getKeyword() : "";
    String sortField = filter.getSortField() != null ? filter.getSortField() : "date";
    Sort.Direction sortDirection = "desc".equalsIgnoreCase(filter.getSortOrder()) ? Sort.Direction.DESC : Sort.Direction.ASC;
    Sort sort = Sort.by(sortDirection, sortField);
    if("income".equals(filter.getType())){
      List<IncomeDTO> incomes = incomeService.filterIncomes(startDate, endDate, keyword, sort);
      return ResponseEntity.ok(incomes);
    } else if ("expense".equals(filter.getType())){
      List<ExpenseDTO> expenses = expenseService.filterExpenses(startDate, endDate, keyword, sort);
      return ResponseEntity.ok(expenses);
    } else {
      return ResponseEntity.badRequest().body("Invalid type. Must be 'income' or 'expense'.");
    }

  }
  
}
