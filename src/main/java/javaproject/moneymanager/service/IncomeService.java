package javaproject.moneymanager.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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

  // retrieves all incomes for the given month/year for current user
  public List<IncomeDTO> getIncomesForMonth(int year, int month){
    if(month < 1 || month > 12){
      throw new IllegalArgumentException("Month must be between 1 and 12");
    }

    ProfileEntity profile = profileService.getCurrentProfile();
    LocalDate startDate = LocalDate.of(year, month, 1);
    LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

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

  // Generate Excel file for current month
  public ByteArrayInputStream generateIncomeExcel() {
    LocalDate now = LocalDate.now();
    return generateIncomeExcelForMonth(now.getYear(), now.getMonthValue());
  }

  // Generate Excel file for a specific month/year
  public ByteArrayInputStream generateIncomeExcelForMonth(int year, int month) {
    if (month < 1 || month > 12) {
      throw new IllegalArgumentException("Month must be between 1 and 12");
    }

    ProfileEntity profile = profileService.getCurrentProfile();
    LocalDate startDate = LocalDate.of(year, month, 1);
    LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
    Sort sort = Sort.by(Sort.Direction.DESC, "date");

    List<IncomeEntity> incomes = incomeRepository.findByProfileIdAndDateBetween(
      profile.getId(),
      startDate,
      endDate
    );

    return buildIncomeExcel(incomes);
  }

  private ByteArrayInputStream buildIncomeExcel(List<IncomeEntity> incomes) {
    try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Sheet sheet = workbook.createSheet("Income Details");

      // Create header style
      CellStyle headerStyle = workbook.createCellStyle();
      Font headerFont = workbook.createFont();
      headerFont.setBold(true);
      headerFont.setColor(IndexedColors.WHITE.getIndex());
      headerStyle.setFont(headerFont);
      headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
      headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

      // Create header row
      Row headerRow = sheet.createRow(0);
      String[] headers = {"No", "Name", "Category", "Amount", "Date"};
      for (int i = 0; i < headers.length; i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(headers[i]);
        cell.setCellStyle(headerStyle);
      }

      // Create data rows
      DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
      
      int rowIdx = 1;
      int serial = 1;
      BigDecimal totalAmount = BigDecimal.ZERO;
      
      for (IncomeEntity income : incomes) {
        Row row = sheet.createRow(rowIdx++);
        
        row.createCell(0).setCellValue(serial++);
        row.createCell(1).setCellValue(income.getName());
        row.createCell(2).setCellValue(income.getCategory() != null ? income.getCategory().getName() : "N/A");
        row.createCell(3).setCellValue(income.getAmount().doubleValue());
        row.createCell(4).setCellValue(income.getDate() != null ? income.getDate().format(dateFormatter) : "");
        
        totalAmount = totalAmount.add(income.getAmount());
      }

      // Add total row
      Row totalRow = sheet.createRow(rowIdx);
      Cell totalLabelCell = totalRow.createCell(2);
      totalLabelCell.setCellValue("Total:");
      
      CellStyle totalStyle = workbook.createCellStyle();
      Font totalFont = workbook.createFont();
      totalFont.setBold(true);
      totalStyle.setFont(totalFont);
      totalLabelCell.setCellStyle(totalStyle);
      
      Cell totalAmountCell = totalRow.createCell(3);
      totalAmountCell.setCellValue(totalAmount.doubleValue());
      totalAmountCell.setCellStyle(totalStyle);

      // Auto-size columns
      for (int i = 0; i < headers.length; i++) {
        sheet.autoSizeColumn(i);
      }

      workbook.write(out);
      return new ByteArrayInputStream(out.toByteArray());
    } catch (IOException e) {
      throw new RuntimeException("Failed to generate Excel file: " + e.getMessage());
    }
  }
  
}
