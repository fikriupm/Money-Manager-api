package javaproject.moneymanager.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javaproject.moneymanager.dto.IncomeDTO;
import javaproject.moneymanager.entity.CategoryEntity;
import javaproject.moneymanager.entity.IncomeEntity;
import javaproject.moneymanager.entity.ProfileEntity;
import javaproject.moneymanager.repository.CategoryRepository;
import javaproject.moneymanager.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IncomeService {

  private final CategoryRepository categoryRepository;
  private final IncomeRepository incomeRepository; 
  private final ProfileService profileService;

  //add a new income to the db
  public IncomeDTO addIncome(IncomeDTO dto){
    ProfileEntity profile = profileService.getCurrentProfile();
    CategoryEntity category = categoryRepository.findById(dto.getCategoryId())
      .orElseThrow(()-> new RuntimeException("Category not found"));
    IncomeEntity newIncome = toEntity(dto, profile, category);
    newIncome = incomeRepository.save(newIncome);
    return toDTO(newIncome);
  }

  // retrieves all expenses for current month/based on the start date and end date
  public List<IncomeDTO> getCurrentMonthIncomesForCurrentUser(){
    ProfileEntity profile = profileService.getCurrentProfile();
    LocalDate now = LocalDate.now();
    LocalDate startDate = now.withDayOfMonth(1);
    LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());
    List<IncomeEntity> list = incomeRepository.findByProfileIdAndDateBetween(
      profile.getId(), 
      startDate, 
      endDate
    );
    return list.stream().map(this::toDTO).toList();
  }

  //delete incomes by id for current user
  public void deleteIncome(Long incomeId){
    ProfileEntity profile = profileService.getCurrentProfile();
    IncomeEntity entity = incomeRepository.findById(incomeId)
      .orElseThrow(()-> new RuntimeException("Income not found"));
    if(!entity.getProfile().getId().equals(profile.getId())){
      throw new RuntimeException("Unauthorized to delete this income");
    }
    
    incomeRepository.delete(entity);
  }

  //get latest 5 incomes for current user
  public List<IncomeDTO> getLatest5IncomesForCurrentUser(){
    ProfileEntity profile = profileService.getCurrentProfile();
    List<IncomeEntity> list = incomeRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
    return list.stream().map(this::toDTO).toList();
  }

  // get total incomes for current user
  public BigDecimal getTotalIncomesForCurrentUser(){
    ProfileEntity profile = profileService.getCurrentProfile();
    BigDecimal total = incomeRepository.findTotalIncomeByProfileId(profile.getId());
    
    return total != null ? total : BigDecimal.ZERO;
  }

  // filter incomes
  public List<IncomeDTO> filterIncomes(
    LocalDate startDate, 
    LocalDate endDate, 
    String keyword, 
    Sort sort
  ){
    ProfileEntity profile = profileService.getCurrentProfile();
    List<IncomeEntity> list = incomeRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(
      profile.getId(),
      startDate,
      endDate,
      keyword,
      sort
    );
    return list.stream().map(this::toDTO).toList();
  }

  //helper
  private IncomeEntity toEntity(IncomeDTO dto, ProfileEntity profile, CategoryEntity category){
    return IncomeEntity.builder()
      .name(dto.getName())
      .icon(dto.getIcon())
      .amount(dto.getAmount())
      .date(dto.getDate())
      .profile(profile)
      .category(category)
      .build();
  }

  private IncomeDTO toDTO(IncomeEntity entity){
    return IncomeDTO.builder()
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
