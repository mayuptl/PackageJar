package core.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

public class ExcelReader {
    /**
     * Reads a specific row of test data from an Excel file based on the unique
     * test case identifier found in the first column (index 0) of the sheet.
     * @param excelFilePath The full path to the Excel (.xlsx) file.
     * @param sheetName     The name of the sheet containing the test data.
     * @param testCaseName  The unique ID/Name of the test case to look for in column A.
     * @return A {@link java.util.List} of {@code String} values representing the data cells
     * from the found row, starting from the second column (index 1).
     * @throws IOException             If the file cannot be opened, read, or is corrupt.
     * @throws NoSuchElementException  If the sheet or the test case ID is not found.
     * @throws IllegalStateException   If the sheet is found but contains no data rows.
     * <p>
     * This is the primary static method for test scripts to retrieve formatted data.
     * </p>
     *
     * <p><b>Example Data Structure:</b></p>
     * <p><b>Sheet Name: LoginData </b></p>
     * <p><b>In excel Col A should be test case name or test case id only.</b></p>
     * <table>
     * <thead>
     * <tr>
     * <th>&nbsp;</th>
     * <th><b>Column A</b></th>
     * <th><b>Column B</b></th>
     * <th><b>Column C</b></th>
     * <th><b>Column D</b></th>
     * <th>&nbsp;</th>
     * <th>&nbsp;</th>
     * </tr>
     * <tr>
     * <td colspan="7"><hr></td>
     * </tr>
     * <tr>
     * <td><b>1</b></td>
     * <td><b>TC id / Name</b></td>
     * <td><b>UN</b></td>
     * <td><b>PWD</b></td>
     * <td colspan="3">&nbsp;</td>
     * </tr>
     * <tr>
     * <td colspan="7"><hr></td>
     * </tr>
     * <tr>
     * <td><b>2</b></td>
     * <td><b>To verify user login</b></td>
     * <td><b>Mayur</b></td>
     * <td><b>M*1234</b></td>
     * <td colspan="3">&nbsp;</td>
     * </tr>
     * <tr>
     * <td colspan="7"><hr></td>
     * </tr>
     * <tr>
     * <td><b>3</b></td>
     * <td>&nbsp;</td>
     * <td><b>String</b></td>
     * <td><b>Number</b></td>
     * <td><b>Decimal</b></td>
     * <td><b>Bool</b></td>
     * <td><b>Date</b></td>
     * <td><b>Blank</b></td>
     * </tr>
     * <tr>
     * <td colspan="7"><hr></td>
     * </tr>
     * </thead>
     * <tbody>
     * <tr>
     * <td><b>4</b></td>
     * <td><b>TC02</b></td>
     * <td><b>ABC</b></td>
     * <td><b>123</b></td>
     * <td><b>10.99</b></td>
     * <td><b>TRUE</b></td>
     * <td><b>12-10-25</b></td>
     * <td>&nbsp;</td>
     * </tr>
     * <tr>
     * <td colspan="7"><hr></td>
     * </tr>
     * <tr>
     * <td><b>5</b></td>
     * <td>&nbsp;</td>
     * <td><b>TestData1</b></td>
     * <td><b>TestData2</b></td>
     * <td colspan="4">&nbsp;</td>
     * </tr>
     * <tr>
     * <td colspan="7"><hr></td>
     * </tr>
     * <tr>
     * <td><b>6</b></td>
     * <td><b>TC03</b></td>
     * <td><b>xyz</b></td>
     * <td><b>9028</b></td>
     * <td colspan="4">&nbsp;</td>
     * </tr>
     * <tr>
     * <td colspan="7"><hr></td>
     * </tr>
     * </tbody>
     * </table>
     *  <p><b>Example Usage with TestNG DataProvider:</b></p>
     *
     * <pre>
     * {@code
     * String EXCEL_FILE_PATH = "<File Path>";
     * String SHEET_NAME = "LoginData";
     * String TEST_CASE_ID_Name = "To verify user login";
     * ExcelReader excelReader = new ExcelReader();
     *
     * @Test(dataProvider = "getData")
     * public void test(List<String> input) {
     *     System.out.println("Username: " + input.get(0));
     *     System.out.println("Password: " + input.get(1));
     * }
     * @DataProvider
     * public static Object[][] getData() throws IOException {
     * 	 List<String> data = excelReader.getTestInput(EXCEL_FILE_PATH, SHEET_NAME, TEST_CASE_ID_Name);
     *   return new Object [][] {{data}};
     *   }
     * }
     * </pre>
     */
    public List<String> getTestInput(String excelFilePath, String sheetName, String testCaseName) throws IOException {
        Workbook workbook;
        // 1. Try-with-resources for automatic closing of FileInputStream
        try (FileInputStream fis = new FileInputStream(excelFilePath)) {
            workbook = new XSSFWorkbook(fis);
        }
        // 2. Error Check: Sheet existence
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            throw new NoSuchElementException("Sheet '" + sheetName + "' not found in the workbook: " + excelFilePath);
        }
        // 3. Checking total rows count
        int totalRows = sheet.getLastRowNum();
        if (totalRows < 0) {
            throw new IllegalStateException("Empty rows found in the sheet: '" + sheetName + "'. Cannot proceed without data.");
        }
        // 4. Loop through rows to find the test case
        Row row = validateTestCaseExists(sheet, testCaseName);
        return extractRowData(row);
    }

    private Row validateTestCaseExists(Sheet sheet, String testCaseName) {
        boolean flag = false; // used for test case name check
        int cellCount = 0;
        int totalRows = sheet.getLastRowNum();

        for (int j = 0; j <= totalRows; j++) {
            Row row = sheet.getRow(j);
            if (row != null) {
                if (row.getCell(0) != null) {
                    if (row.getCell(0).getStringCellValue().equalsIgnoreCase(testCaseName)) {
                        flag = true;
                        row = sheet.getRow(row.getRowNum());
                        // it will give total cells count
                        cellCount = row.getLastCellNum();
                        if (cellCount == 1) {
                            throw new NoSuchElementException("Test data not found for the given testcase:- " + testCaseName);
                        }
                        return row;
                    } // end of test case name check
                } // end of row.getCell(0) !=null
            } // end of row !=null
        } // end of for loop (int j = 0; j <= totalRows; j++)
        if (flag != true) {
            throw new NoSuchElementException("Test case '" + testCaseName + "' not found in sheet '" + sheet.getSheetName() + "'");
        }
        return null;
    }

    private List<String> extractRowData(Row row) {
        List<String> inputDataList = new ArrayList<>();
        int cellCount = row.getLastCellNum();
        cellCount--;
        for (int i = 0; i <= cellCount; i++) {
            if (row.getCell(i) != null) // it will check undefined cell
            {
                if (row.getCell(i).getColumnIndex() != 0) // it will ignore 1st column (i.e A column)
                {
                    CellType type = row.getCell(i).getCellType(); // it will cell type
                    switch (type) {
                        case STRING:
                            inputDataList.add(row.getCell(i).getStringCellValue());
                            break;
                        case NUMERIC:
                            if (DateUtil.isCellDateFormatted(row.getCell(i))) {
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                                Date date = row.getCell(i).getDateCellValue();
                                inputDataList.add(dateFormat.format(date).toString());
                                break;
                            } else {
                                double numericValue = row.getCell(i).getNumericCellValue();
                                if (numericValue == (long) numericValue) {
                                    inputDataList.add(String.valueOf((long) numericValue));
                                } else {
                                    inputDataList.add(String.valueOf(numericValue));
                                }
                                break;
                            }
                        case BOOLEAN:
                            inputDataList.add(String.valueOf(row.getCell(i).getBooleanCellValue()));
                            break;
                        case FORMULA:
                            inputDataList.add(String.valueOf(row.getCell(i).getCellFormula()));
                            break;
                        case BLANK:
                            inputDataList.add("");
                            break;
                        case ERROR:
                            inputDataList.add("");
                            break;
                        case _NONE:
                        default:
                            inputDataList.add(row.getCell(i).getStringCellValue());
                            break;
                    }
                } // end of (row.getCell(i).getColumnIndex() != 0) // it will ignore 1st column (i.e A column)
            } // end of if (row.getCell(i) != null)
        } // end of for loop for (int i = 0; i <= cellCount; i++)
        return inputDataList;
    }
}
