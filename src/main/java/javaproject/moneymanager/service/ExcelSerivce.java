package javaproject.moneymanager.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import javaproject.moneymanager.dto.ExpenseDTO;
import javaproject.moneymanager.dto.IncomeDTO;

//not used yet, currently use in IncomeService and ExpenseService directly
@Service
public class ExcelSerivce {
  public void writeIncomesToExcel(OutputStream os, List<IncomeDTO> incomes) throws IOException {
    // Implementation for writing incomes to Excel
    try(Workbook workbook = new XSSFWorkbook()) {
      Sheet sheet = workbook.createSheet("Incomes");
      Row headerRow = sheet.createRow(0);
      headerRow.createCell(0).setCellValue("S.No");
      headerRow.createCell(1).setCellValue("Name");
      headerRow.createCell(2).setCellValue("Category");
      headerRow.createCell(3).setCellValue("Amount");
      headerRow.createCell(4).setCellValue("Date");
      IntStream.range(0, incomes.size())
        .forEach(i -> {
          IncomeDTO income = incomes.get(i);
          Row row = sheet.createRow(i + 1);
          row.createCell(0).setCellValue(i + 1);
          row.createCell(1).setCellValue(income.getName() != null ? income.getName() : "N/A");
          row.createCell(2).setCellValue(income.getCategoryId() != null ? income.getCategoryName() : "N/A");
          row.createCell(3).setCellValue(income.getAmount() != null ? income.getAmount().doubleValue() : 0.0);
          row.createCell(4).setCellValue(income.getDate().toString() != null ? income.getDate().toString() : "N/A");
        });
      workbook.write(os);
    }
  }

  public void writeExpensesToExcel(OutputStream os, List<ExpenseDTO> expenses) throws IOException {
    // Implementation for writing expenses to Excel
    try(Workbook workbook = new XSSFWorkbook()) {
      Sheet sheet = workbook.createSheet("Expenses");
      Row headerRow = sheet.createRow(0);
      headerRow.createCell(0).setCellValue("S.No");
      headerRow.createCell(1).setCellValue("Name");
      headerRow.createCell(2).setCellValue("Category");
      headerRow.createCell(3).setCellValue("Amount");
      headerRow.createCell(4).setCellValue("Date");
      IntStream.range(0, expenses.size())
        .forEach(i -> {
          ExpenseDTO expense = expenses.get(i);
          Row row = sheet.createRow(i + 1);
          row.createCell(0).setCellValue(i + 1);
          row.createCell(1).setCellValue(expense.getName() != null ? expense.getName() : "N/A");
          row.createCell(2).setCellValue(expense.getCategoryId() != null ? expense.getCategoryName() : "N/A");
          row.createCell(3).setCellValue(expense.getAmount() != null ? expense.getAmount().doubleValue() : 0.0);
          row.createCell(4).setCellValue(expense.getDate().toString() != null ? expense.getDate().toString() : "N/A");
        });
      workbook.write(os);
    }
  }
}
