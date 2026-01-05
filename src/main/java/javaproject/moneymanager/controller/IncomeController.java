package javaproject.moneymanager.controller;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javaproject.moneymanager.dto.IncomeDTO;
import javaproject.moneymanager.service.IncomeService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/incomes")
public class IncomeController {
    private final IncomeService incomeService;

  @PostMapping
  public ResponseEntity<IncomeDTO> addIncome(@RequestBody IncomeDTO dto){
    IncomeDTO saved = incomeService.addIncome(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);

  }  

  @GetMapping
  public ResponseEntity<List<IncomeDTO>> getIncomes(){
    List<IncomeDTO> incomes = incomeService.getCurrentMonthIncomesForCurrentUser();
    return ResponseEntity.ok(incomes);

  }

  @GetMapping("/by-month")
  public ResponseEntity<List<IncomeDTO>> getIncomesByMonth(
    @RequestParam int year,
    @RequestParam int month
  ){
    List<IncomeDTO> incomes = incomeService.getIncomesForMonth(year, month);
    return ResponseEntity.ok(incomes);

  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteIncome(@PathVariable Long id){
    incomeService.deleteIncome(id);
    return ResponseEntity.noContent().build();

  }

  @GetMapping("/download/excel")
  public ResponseEntity<InputStreamResource> downloadIncomeExcel(
    @RequestParam(required = false) Integer year,
    @RequestParam(required = false) Integer month
  ){
    try {
      LocalDate now = LocalDate.now();
      int targetYear = year != null ? year : now.getYear();
      int targetMonth = month != null ? month : now.getMonthValue();

      ByteArrayInputStream in = incomeService.generateIncomeExcelForMonth(targetYear, targetMonth);
      
      HttpHeaders headers = new HttpHeaders();
      headers.add("Content-Disposition", "attachment; filename=income_details.xlsx");
      
      return ResponseEntity.ok()
        .headers(headers)
        .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .body(new InputStreamResource(in));
    } catch (Exception e) {
      throw new RuntimeException("Failed to generate Excel file: " + e.getMessage());
    }
  }
}
