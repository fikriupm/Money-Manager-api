package javaproject.moneymanager.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import javaproject.moneymanager.dto.ExpenseDTO;
import javaproject.moneymanager.dto.IncomeDTO;
import javaproject.moneymanager.dto.RecentTransactionDTO;
import javaproject.moneymanager.entity.ProfileEntity;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

  private final IncomeService incomeService;
  private final ExpenseService expenseService;
  private final ProfileService profileService;

  public Map<String, Object> getDashboardData() {
    ProfileEntity profile = profileService.getCurrentProfile(); // just to ensure the user is authenticated
    Map<String, Object> returnValue = new LinkedHashMap<>();
    List<IncomeDTO> latestIncomes = incomeService.getLatest5IncomesForCurrentUser();
    List<ExpenseDTO> latestExpenses = expenseService.getLatest5ExpensesForCurrentUser();
    List<RecentTransactionDTO> recentTransactions = Stream.concat(
        latestIncomes.stream().map(income -> RecentTransactionDTO.builder()
            .id(income.getId())
            .profileId(profile.getId())
            .icon(income.getIcon())
            .name(income.getName())
            .amount(income.getAmount())
            .date(income.getDate())
            .createdAt(income.getCreatedAt())
            .updatedAt(income.getUpdatedAt())
            .type("income")
            .build()),
        latestExpenses.stream().map(expense -> RecentTransactionDTO.builder()
            .id(expense.getId())
            .profileId(profile.getId())
            .icon(expense.getIcon())
            .name(expense.getName())
            .amount(expense.getAmount())
            .date(expense.getDate())
            .createdAt(expense.getCreatedAt())
            .updatedAt(expense.getUpdatedAt())
            .type("expense")
            .build()))
        .sorted((a,b) -> {
          int cmp = b.getDate().compareTo(a.getDate());
          if(cmp == 0 && a.getCreatedAt() != null && b.getCreatedAt() != null) {
            return b.getCreatedAt().compareTo(a.getCreatedAt());
          }
          return cmp;
        }).collect(Collectors.toList());
    returnValue.put("totalBalance", incomeService.getTotalIncomesForCurrentUser().subtract(expenseService.getTotalExpensesForCurrentUser()) );
    returnValue.put("totalIncomes", incomeService.getTotalIncomesForCurrentUser());
    returnValue.put("totalExpenses", expenseService.getTotalExpensesForCurrentUser());
    returnValue.put("recent5expenses", latestExpenses);
    returnValue.put("recent5incomes", latestIncomes);
    returnValue.put("recentTransactions", recentTransactions);
    return returnValue;
  }

}
