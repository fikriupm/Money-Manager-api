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

  // retrieves all expenses for the given month/year for current user
  public List<ExpenseDTO> getExpensesForMonth(int year, int month){
    if(month < 1 || month > 12){
      throw new IllegalArgumentException("Month must be between 1 and 12");
    }

    ProfileEntity profile = profileService.getCurrentProfile();
    LocalDate startDate = LocalDate.of(year, month, 1);
    LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

    List<ExpenseEntity> list = expenseRepository.findByProfileIdAndDateBetween(
      profile.getId(),
      startDate,
      endDate
    );
    return list.stream().map(this::toDTO).toList();
  }

  // Generate Excel file for current month
  public ByteArrayInputStream generateExpenseExcel() {
    LocalDate now = LocalDate.now();
    return generateExpenseExcelForMonth(now.getYear(), now.getMonthValue());
  }

  // Generate Excel file for a specific month/year
  public ByteArrayInputStream generateExpenseExcelForMonth(int year, int month) {
    if (month < 1 || month > 12) {
      throw new IllegalArgumentException("Month must be between 1 and 12");
    }

    ProfileEntity profile = profileService.getCurrentProfile();
    LocalDate startDate = LocalDate.of(year, month, 1);
    LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
    Sort sort = Sort.by(Sort.Direction.DESC, "date");

    List<ExpenseEntity> expenses = expenseRepository.findByProfileIdAndDateBetween(
      profile.getId(),
      startDate,
      endDate
    );

    return buildExpenseExcel(expenses);
  }

  private ByteArrayInputStream buildExpenseExcel(List<ExpenseEntity> expenses) {
    try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Sheet sheet = workbook.createSheet("Expense Details");

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

      for (ExpenseEntity expense : expenses) {
        Row row = sheet.createRow(rowIdx++);

        row.createCell(0).setCellValue(serial++);
        row.createCell(1).setCellValue(expense.getName());
        row.createCell(2).setCellValue(expense.getCategory() != null ? expense.getCategory().getName() : "N/A");
        row.createCell(3).setCellValue(expense.getAmount().doubleValue());
        row.createCell(4).setCellValue(expense.getDate() != null ? expense.getDate().format(dateFormatter) : "");

        totalAmount = totalAmount.add(expense.getAmount());
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
      throw new RuntimeException("Failed to generate expense Excel file: " + e.getMessage());
    }
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
