package javaproject.moneymanager.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javaproject.moneymanager.dto.ExpenseDTO;
import javaproject.moneymanager.entity.CategoryEntity;
import javaproject.moneymanager.entity.ExpenseEntity;
import javaproject.moneymanager.entity.ProfileEntity;
import javaproject.moneymanager.repository.CategoryRepository;
import javaproject.moneymanager.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExpenseService {

  private final CategoryRepository categoryRepository;
  private final ExpenseRepository expenseRepository;
  private final ProfileService profileService;

  //ad a new expense to the db
  public ExpenseDTO addExpense(ExpenseDTO dto){
    ProfileEntity profile = profileService.getCurrentProfile();
    CategoryEntity category = categoryRepository.findById(dto.getCategoryId())
      .orElseThrow(()-> new RuntimeException("Category not found"));
    ExpenseEntity newExpense = toEntity(dto, profile, category);
    newExpense = expenseRepository.save(newExpense);
    return toDTO(newExpense);
  }

  // retrieves all expenses for current month/based on the start date and end date
  public List<ExpenseDTO> getCurrentMonthExpensesForCurrentUser(){
    ProfileEntity profile = profileService.getCurrentProfile();
    LocalDate now = LocalDate.now();
    LocalDate startDate = now.withDayOfMonth(1);
    LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());
    List<ExpenseEntity> list = expenseRepository.findByProfileIdAndDateBetween(
      profile.getId(), 
      startDate, 
      endDate
    );
    return list.stream().map(this::toDTO).toList();
  }

  //delete expenses by id for current user
  public void deleteExpense(Long expenseId){
    ProfileEntity profile = profileService.getCurrentProfile();
    ExpenseEntity entity = expenseRepository.findById(expenseId)
      .orElseThrow(()-> new RuntimeException("Expense not found"));
    if(!entity.getProfile().getId().equals(profile.getId())){
      throw new RuntimeException("Unauthorized to delete this expense");
    }
    
    expenseRepository.delete(entity);
  }

  //get latest 5 expenses for current user
  public List<ExpenseDTO> getLatest5ExpensesForCurrentUser(){
    ProfileEntity profile = profileService.getCurrentProfile();
    List<ExpenseEntity> list = expenseRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
    return list.stream().map(this::toDTO).toList();
  }

  // get total expenses for current user
  public BigDecimal getTotalExpensesForCurrentUser(){
    ProfileEntity profile = profileService.getCurrentProfile();
    BigDecimal total = expenseRepository.findTotalExpenseByProfileId(profile.getId());
    
    return total != null ? total : BigDecimal.ZERO;
  }

  // filter expenses
  public List<ExpenseDTO> filterExpenses(
    LocalDate startDate, 
    LocalDate endDate, 
    String keyword, 
    Sort sort
  ){
    ProfileEntity profile = profileService.getCurrentProfile();
    List<ExpenseEntity> list = expenseRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(
      profile.getId(),
      startDate,
      endDate,
      keyword,
      sort
    );
    return list.stream().map(this::toDTO).toList();
  }

  //noticeable 
  public List<ExpenseDTO> getExpenseForUserOnDate(Long profile, LocalDate date){
    List<ExpenseEntity> list = expenseRepository.findByProfileIdAndDate(profile, date);
    return list.stream().map(this::toDTO).toList();
  }

  //helper
  private ExpenseEntity toEntity(ExpenseDTO dto, ProfileEntity profile, CategoryEntity category){
    return ExpenseEntity.builder()
      .name(dto.getName())
      .icon(dto.getIcon())
      .amount(dto.getAmount())
      .date(dto.getDate())
      .profile(profile)
      .category(category)
      .build();
  }

  private ExpenseDTO toDTO(ExpenseEntity entity){
    return ExpenseDTO.builder()
      .id(entity.getId())
      .name(entity.getName())
      .icon(entity.getIcon())
      .categoryId(entity.getCategory() != null ? entity.getCategory().getId(): null)
      .categoryName(entity.getCategory() != null ? entity.getCategory().getName(): "N/A")
      .amount(entity.getAmount())
      .date(entity.getDate())
      .createdAt(entity.getCreatedAt())
      .updatedAt(entity.getUpdatedAt())
      .build();
  }
  
}
