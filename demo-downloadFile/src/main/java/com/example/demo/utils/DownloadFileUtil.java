package com.example.demo.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import com.example.demo.myEnum.MergeType;
import com.opencsv.CSVWriter;
import com.spire.xls.FileFormat;
import com.spire.xls.Workbook;


@Component
public class DownloadFileUtil {
	
	@Autowired
	private ResourceLoader resourceLoader;
	
	// 共用 Excel 工作簿，必先執行產生 Excel
	private XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
	
	
	/**
	 * 根據資源路徑獲取 InputStream。
	 * 
	 * @param templatePath 模板檔案的資源路徑，使用spring的ResourceLoader，路徑會在主目錄類下
	 * 
	 * @return InputStream 物件，如果資源不存在則返回 null
	 * 
	 * @throws Exception
	 */
	private InputStream getTemplateAsInputStream(String templatePath) throws Exception {
	    InputStream is = null;
	    try {
//	        // 獲取ServletContext
//	        ServletContext servletContext = getServletContext();
//	        // 使用ServletContext的getResourceAsStream方法獲取資源流
//	        templateStream = servletContext.getResourceAsStream(templatePath);
	    	
	    	// 假設使用Spring的ResourceLoader來獲取資源流，會位於resources主目錄類下方
	    	String resourcePath = "classpath:" + templatePath;
	    	Resource resource = resourceLoader.getResource(resourcePath);
	    	is = resource.getInputStream();
	        
	    } catch (IOException e) {
	    	String errorMessage = "ResourceLoader 無法獲取資源: " + e.getMessage();
	    	System.err.println(errorMessage);
        	throw e;
	    }
	    
	    return is;
	}

	/**
	 * 使用 apache poi，根據自定義模板檔案，以及資料列表生成 Excel。
	 * 模板檔案的第一列，須給樣板樣式
	 * 
	 * @param <T>
	 * @param templatePath          模板檔案的資源路徑
	 * @param pageIndex             工作表對象，代表 Excel 中的頁
	 * @param dataList              包含資料的列表
	 * @param targetRowIndex        目標"列"的起始索引，上方，工作表中的特定位置
	 * @param columnIndexLeft       目標"欄"的起始索引，左側，工作表中的特定位置
	 * @param getterListForExcel    包含需要的getter列表
	 * 
	 * @throws Exception 
	 */
	public <T> void simpleExcelMaker(String templatePath, int pageIndex, List<T> dataList, int targetRowIndex, int columnIndexLeft, List<String> getterListForExcel) throws Exception {
	    // 獲取模板文件的 InputStream
		try (InputStream is = getTemplateAsInputStream(templatePath);) {
				
			// 基於模板，創建一個新的 XSSFWorkbook 對象，代表 Excel 工作簿
			xssfWorkbook = new XSSFWorkbook(is);
			// 獲取真實工作表對象，代表 Excel 中的頁
	    	int realPageIndex = pageIndex -1;
            XSSFSheet sheet = xssfWorkbook.getSheetAt(realPageIndex);
            // 模板中的第一列，來源列，即樣板樣式
            int sourceRowIndex = 1;
            
            // 使用dataList物件，根據資料數量 動態產生targetRow，並複製樣式，同時設置儲存格的值
            for (int i = 0; i < dataList.size(); i++) {
            	T data = dataList.get(i);
            	copyCellStyleAndHeightAndSetValue(sheet, data, sourceRowIndex, i + targetRowIndex, columnIndexLeft, getterListForExcel);
            }
	            
		} catch (IOException e) {
			String errorMessage = "simpleExcelMaker 錯誤: " + e.getMessage();
            System.err.println(errorMessage);
        	throw e;
		}
	}
	
	/**
	 * 複製來源列的儲存格高度以及樣式(包括字體)至目標列的儲存格，同時設置值。
	 *
	 * @param <T>                 泛型型別，表示傳入的 data 對象的型別
	 * @param sheet               工作表對象，代表 Excel 中的頁
	 * @param data                包含資料的對象
	 * @param sourceRowIndex      來源列的索引
	 * @param targetRowIndex      目標列的索引
	 * @param columnIndexLeft     最左側儲存格的索引
	 * @param getterListForExcel  包含需要的getter列表
	 * 
	 * @throws Exception 
	 */
    private <T> void copyCellStyleAndHeightAndSetValue(XSSFSheet sheet, T data, int sourceRowIndex, int targetRowIndex, int columnIndexLeft, List<String> getterListForExcel) throws Exception {
    	int realSourceRowIndex = sourceRowIndex - 1; // 真實來源列索引
        int realTargetRowIndex = targetRowIndex - 1; // 真實目標列索引
        
        // 取得真實來源列，無樣式時 啟動創建
        XSSFRow sourceRow = sheet.getRow(realSourceRowIndex) != null ? sheet.getRow(realSourceRowIndex) : sheet.createRow(realSourceRowIndex);
        // 取得真實目標列，無樣式時 啟動創建
        XSSFRow targetRow = sheet.getRow(realTargetRowIndex) != null ? sheet.getRow(realTargetRowIndex) : sheet.createRow(realTargetRowIndex);
        
        // 複製來源儲存格的高度
        copyCellHeight(sourceRow, targetRow);
        
        // 計算 Excel 表格左側到右側的列數
		int columnLeftToRight = getterListForExcel.size();
        // columnIndexLeft最左側行 至 columnIndexRight最右側行 的流程 (儲存格)
        for (int i = 0; i < columnLeftToRight; i++) {
        	// 真實列索引 (儲存格)，由最左側開始
        	int realColumnIndex = (columnIndexLeft - 1) + i; 
        	// 取得真實來源儲存格，如無目標 創建真實來源儲存格
        	XSSFCell sourceCell = sourceRow.getCell(realColumnIndex) != null ? sourceRow.getCell(realColumnIndex) : sourceRow.createCell(realColumnIndex);
        	// 取得真實目標儲存格，如無目標 創建真實目標儲存格
        	XSSFCell targetCell = targetRow.getCell(realColumnIndex) != null ? targetRow.getCell(realColumnIndex) : targetRow.createCell(realColumnIndex);

            // 複製來源儲存格的樣式(包括字體)到目標儲存格
        	copyCellStyle(sheet, sourceCell, targetCell);
        	
            // 相對應的getterMethodName
    	    String getterMethodName = getterListForExcel.get(i);
            // 設置目標儲存格的值
            targetCell.setCellValue(invokeGetterFromDataAndReturnStringResult(getterMethodName, data));
        }
    }
    
    /**
     * 複製來源儲存格的高度至目標儲存格。
     *
     * @param sourceCell 來源儲存格
     * @param targetCell 目標儲存格
     */
    private void copyCellHeight(XSSFRow sourceRow, XSSFRow targetRow) {
    	// 複製來源儲存格的高度
        float sourceCellHeight = sourceRow.getHeightInPoints(); 
        // 設置目標儲存格的高度以匹配來源儲存格
        targetRow.setHeightInPoints(sourceCellHeight);
    }
    
    /**
     * 複製來源儲存格的樣式(包括字體)到目標儲存格
     *
     * @param sheet      工作表對象，代表 Excel 中的頁
     * @param sourceCell 來源儲存格
     * @param targetCell 目標儲存格
     */
    private void copyCellStyle(XSSFSheet sheet,XSSFCell sourceCell, XSSFCell targetCell) {
        // 創建全新樣式
        CellStyle targetCellStyle = sheet.getWorkbook().createCellStyle(); 
        // 從來源儲存格複製樣式(包括字體)到目標儲存格
        targetCellStyle.cloneStyleFrom(sourceCell.getCellStyle());
        // 設定真實目標儲存格樣式
        targetCell.setCellStyle(targetCellStyle); 
    }
    
    /**
     * 根據 data 調用對應的 getter 方法，獲取值
     *
     * @param <T>                             泛型型別，表示傳入的 data 對象的型別
     * 
     * @param getterMethodName                與屬性相對應的 getter 方法名稱
     * @param data                            包含資料的對象
     * 
     * @return                                轉換後的屬性值，以字串形式返回；如果 getter 方法不存在或結果為 null，則回傳空字串
     * 
     * @throws NoSuchMethodException          如果指定的 getter 方法不存在
     * @throws ReflectiveOperationException   如果在反射操作期間發生異常
     */
    private <T> String invokeGetterFromDataAndReturnStringResult(String getterMethodName, T data) throws Exception {
    	// 傳入為空字串，代表預期結果為在工作表上中留空
        if (getterMethodName.equals("")) {
            return ""; // 空字串
        }

        try {
            // 透過 Java 反射機制獲取指定的 getter 方法
            Method method = data.getClass().getMethod(getterMethodName);

            // 通過反射機制調用 getter 方法，獲取屬性值
            Object result = method.invoke(data);

            // 如果結果不為 null，回傳其字串表示
            if (result != null) {
                return result.toString();
            }
            
        } catch (NoSuchMethodException e) {
        	// 方法不存在的處理
            String errorMessage = "無此getter: " + getterMethodName;
            System.out.println(errorMessage);
            throw e;
        } catch (ReflectiveOperationException e) {
            // 反射操作異常的處理
            String errorMessage = "反射操作異常: " + e.getMessage();
            System.err.println(errorMessage);
            throw e;
        }

        // 無法成功獲取屬性值或結果為 null，回傳空字串
        return ""; // 預設回傳空字串
    }

    /**
     * 此方法用於設定目標儲存格的值。
     * 
     * @param pageIndex            工作表對象，代表 Excel 中的頁
     * @param rowIndex             目標儲存格所在的行索引
     * @param columnIndex          目標儲存格所在的列索引
     * @param stringValue          要設定的目標儲存格值 (字串)
     */
    public void setValueToCell(int pageIndex, int rowIndex, int columnIndex, String stringValue) {
    	// 獲取真實工作表對象，代表 Excel 中的頁
    	int realPageIndex = pageIndex -1;
    	XSSFSheet sheet = xssfWorkbook.getSheetAt(realPageIndex);
    	
    	//真實第幾行和第幾列
        int realRowIndex = rowIndex - 1;
        int realColumnIndex = columnIndex - 1;

        // 取得真實指定行，無樣式時 啟動創建
        XSSFRow row = sheet.getRow(realRowIndex) != null ? sheet.getRow(realRowIndex) : sheet.createRow(realRowIndex);
        // 取得真實指定儲存格，無樣式時 啟動創建
        XSSFCell cell = row.getCell(realColumnIndex) != null ? row.getCell(realColumnIndex) : row.createCell(realColumnIndex);
        
        // 設定真實目標儲存格值 (字串)
        cell.setCellValue(stringValue); 
    }
    
    /**
     *  先獲取工作表後，刪除第一列模板來源列，並向上移動所有列
     *  將 XSSFWorkbook 對象轉換為 InputStreamResource 並設定相關屬性。
     * 
     * @param pageIndex      工作表對象，代表 Excel 中的頁
     * 
     * @return InputStreamResource 物件，包含轉換後的資源
     * 
     * @throws Exception 
     */
    public InputStreamResource convertXlsxToISR(int pageIndex) throws Exception {
    	
    	ByteArrayOutputStream baos = new ByteArrayOutputStream(); // 創建一個字節陣列輸出流
    	InputStreamResource isr = null; // 這個物件可用於後續操作，例如設定 HTTP 響應的主體部分
    	
    	try {
    		// 獲取真實工作表對象，代表 Excel 中的頁
        	int realPageIndex = pageIndex -1;
        	XSSFSheet sheet = xssfWorkbook.getSheetAt(realPageIndex);
    		// 刪除第一列（索引為0），並向上移動所有列
    		sheet.shiftRows(1, sheet.getLastRowNum(), -1); 
    		// 將 XSSFWorkbook 寫入到字節陣列輸出流中
    		xssfWorkbook.write(baos);
    		
    		// 將字節陣列轉換為 ByteArrayInputStream 並設定為 InputStream
    		InputStream is = new ByteArrayInputStream(baos.toByteArray());
    		// 創建一個 InputStreamResource 物件，用於封裝 InputStream
    		isr = new InputStreamResource(is);
    		
    	} catch (IOException e) {
    		String errorMessage = "無XSSFWorkbook資源: " + e.getMessage();
    		System.err.println(errorMessage);
    		throw e;
    	}
    	
    	return isr;
    }
    
    /**
     *  將 XSSFWorkbook 對象轉換為 InputStream
     * 
     * @param pageIndex      工作表對象，代表 Excel 中的頁
     * 
     * @return InputStream 流，包含轉換後的資源
     * 
     * @throws Exception 
     */
    public ByteArrayInputStream convertXlsxToIS() throws Exception {
    	
    	ByteArrayOutputStream baos = new ByteArrayOutputStream(); // 創建一個字節陣列輸出流
    	ByteArrayInputStream is = null; 
    	
    	try {
//    		// 獲取真實工作表對象，代表 Excel 中的頁
//    		int realPageIndex = pageIndex -1;
//    		XSSFSheet sheet = xssfWorkbook.getSheetAt(realPageIndex);
    		xssfWorkbook.write(baos);
    		
    		is = new ByteArrayInputStream(baos.toByteArray());
    		
    	} catch (IOException e) {
    		String errorMessage = "無XSSFWorkbook資源: " + e.getMessage();
    		System.err.println(errorMessage);
    		throw e;
    	}
    	
    	return is;
    }
    
    /**
     * 使用 apache poi，將 Excel 轉換為 CSV。
     * 
     * @param pageIndex            獲取工作表對象，代表 Excel 中的頁
     * 
     * @return 轉換後的 CSV 字節陣列
     * 
     * @throws Exception
     */
    public InputStreamResource convertXlsxToCsv(int pageIndex) throws Exception {
        // 創建CSVWriter
        StringWriter sw = new StringWriter();

        try (CSVWriter csvWriter = new CSVWriter(sw, ',', '\"', '\\', "\r\n")) {
            // 獲取真實工作表
            int realPageIndex = pageIndex - 1;
            XSSFSheet sheet = xssfWorkbook.getSheetAt(realPageIndex);

            for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex); // 獲取工作表的行

                if (row != null) {
                    List<String> rowData = new ArrayList<>(); // Csv 資料

                    for (int colIndex = 0; colIndex < row.getLastCellNum(); colIndex++) {
                        Cell cell = row.getCell(colIndex); // 獲取儲存格

                        String cellValue = ""; // 儲存格的預設值
                        boolean isMergedCell = cellIsPartOfMergeRegion(sheet, cell); // 判斷是否為合併儲存格

                        if (cell != null) {
                            // 如果是合併儲存格
                            if (isMergedCell) {
                                cellValue = getMergedRegionValue(sheet, cell); // 獲取合併區域的值
                            } else {
                                cellValue = cell.toString(); // 獲取儲存格的字串值
                            }
                        }

                        // 將值根據合併區域放置在最左、最上、或最左上
                        if (cell != null && isMergedCell) {
                            CellRangeAddress region = getMergedRegion(sheet, cell); // 獲取合併區域的資訊
                            placeValueBasedOnMergeDirection(cell, isMergedCell, rowIndex, colIndex, rowData, cellValue, region);
                        } else {
                            rowData.add(cellValue); // 非合併區域的儲存格，直接添加值到 rowData
                        }
                    }

                    csvWriter.writeNext(rowData.toArray(new String[0])); // 將 rowData 寫入 CSVWriter
                }
            }
        } catch (IOException e) {
            String errorMessage = "無法關閉 CSVWriter: " + e.getMessage();
            System.err.println(errorMessage);
            throw e; 
        }

        byte[] csvBytes = sw.toString().getBytes(StandardCharsets.UTF_8);
        // 將 byte array 轉換為 InputStreamResource
        InputStreamResource isr = new InputStreamResource(new ByteArrayInputStream(csvBytes));

        return isr;
    }


    /**
     * 檢查儲存格是否屬於合併區域。
     * 
     * @param sheet     工作表
     * @param cell      欲檢查的儲存格
     * 
     * @return 若儲存格屬於合併區域則返回 true，否則返回 false
     */
    private boolean cellIsPartOfMergeRegion(XSSFSheet sheet, Cell cell) {
    	int rowIndex = cell.getRowIndex();     // 儲存格所在的列索引
        int colIndex = cell.getColumnIndex();  // 儲存格所在的行索引
        
    	// 遍歷所有的合併區域
        for (CellRangeAddress region : sheet.getMergedRegions()) {
            // 檢查儲存格是否在合併區域內
            if (region.isInRange(rowIndex, colIndex)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 獲取合併區域的值。
     * 
     * @param sheet 工作表
     * @param cell  儲存格
     * 
     * @return 合併區域的值
     */
    private String getMergedRegionValue(XSSFSheet sheet, Cell cell) {
        int rowIndex = cell.getRowIndex();     // 儲存格所在的列索引
        int colIndex = cell.getColumnIndex();  // 儲存格所在的行索引

        // 遍歷所有合併區域
        for (CellRangeAddress region : sheet.getMergedRegions()) {
            // 檢查儲存格是否在合併區域內
            if (region.isInRange(rowIndex, colIndex)) {
                Row firstRow = sheet.getRow(region.getFirstRow()); // 合併區域的起始列
                Cell firstCellOfRegion = firstRow.getCell(region.getFirstColumn()); // 合併區域的起始行
                return firstCellOfRegion.toString(); // 返回合併區域的值
            }
        }
        return ""; // 若不在合併區域內則返回空字串
    }

    /**
     * 獲取儲存格所在的合併區域。
     * 
     * @param sheet   工作表
     * @param cell    儲存格
     * 
     * @return 儲存格所在的合併區域
     */
    private CellRangeAddress getMergedRegion(XSSFSheet sheet, Cell cell) {
        // 遍歷所有合併區域
        for (CellRangeAddress region : sheet.getMergedRegions()) {
            // 檢查儲存格是否在合併區域內
            if (region.isInRange(cell.getRowIndex(), cell.getColumnIndex())) {
                return region; // 若在合併區域內則返回該合併區域
            }
        }
        return null; // 若不在合併區域內則返回空
    }
    
    /**
     * 根據合併儲存格和索引位置將值放在左上方、上方、或左邊
     * 
     * @param cell             儲存格
     * @param isMergedCell     是否為合併儲存格
     * @param rowIndex         目前的列索引
     * @param colIndex         目前的行索引
     * @param rowData          CSV資料
     * @param cellValue        儲存格的值
     * @param region           合併區域
     */
    private void placeValueBasedOnMergeDirection(Cell cell, boolean isMergedCell, int rowIndex, int colIndex, List<String> rowData, String cellValue, CellRangeAddress region) {
        if (cell != null && isMergedCell) {
            int firstRow = region.getFirstRow();  // 合併區域的起始列索引
            int lastRow = region.getLastRow();    // 合併區域的結束列索引
            int firstCol = region.getFirstColumn();// 合併區域的起始行索引
            int lastCol = region.getLastColumn();  // 合併區域的結束行索引

            boolean isHorizontalMerge = lastRow - firstRow <= lastCol - firstCol; // 判斷左右合併
            boolean isVerticalMerge = lastRow - firstRow > lastCol - firstCol;    // 判斷上下合併

            switch (determineMergeType(isHorizontalMerge, isVerticalMerge, rowIndex, colIndex, firstRow, firstCol)) {
                case LEFT_TOP: // 左上角
                    rowData.add(cellValue);
                    break;
                case VERTICAL_MERGE: // 上下合併
                    if (rowIndex == firstRow) {
                        rowData.add(cellValue);
                    } else {
                        rowData.add("");
                    }
                    break;
                case HORIZONTAL_MERGE: // 左右合併
                    if (colIndex == firstCol) {
                        rowData.add(cellValue);
                    } else {
                        rowData.add("");
                    }
                    break;
                default: // 預設為空
                    rowData.add("");
            }
        } else {
            rowData.add(cellValue);
        }
    }

    /**
     * 確定合併類型的方法
     * 。
     * @param isHorizontalMerge 是否為水平合併
     * @param isVerticalMerge 是否為垂直合併
     * @param rowIndex 目前行索引
     * @param colIndex 目前列索引
     * @param firstRow 合併區域的起始行索引
     * @param firstCol 合併區域的起始列索引
     * 
     * @return 合併類型，可能為左上角、垂直合併、水平合併或預設
     */
    private MergeType determineMergeType(boolean isHorizontalMerge, boolean isVerticalMerge, int rowIndex, int colIndex, int firstRow, int firstCol) {
        if (isHorizontalMerge && isVerticalMerge) {
            return (rowIndex == firstRow && colIndex == firstCol) ? MergeType.LEFT_TOP : MergeType.DEFAULT;
        } else if (isVerticalMerge) {
            return (rowIndex == firstRow) ? MergeType.VERTICAL_MERGE : MergeType.DEFAULT;
        } else if (isHorizontalMerge) {
            return (colIndex == firstCol) ? MergeType.HORIZONTAL_MERGE : MergeType.DEFAULT;
        }
        return MergeType.DEFAULT;
    }
    
    /**
     * 將 Excel 檔案轉換為 PDF 並返回 InputStreamResource 的方法。
     *
     * @return 包含 PDF 內容的 InputStreamResource
     * @throws Exception 處理可能的例外
     */
    public InputStreamResource convertXlsxToPdf(String xlsxPath, String pdfPath) throws Exception {

        try {
            // 將 Excel 轉換為 PDF
            excelToPdf(xlsxPath, pdfPath);

            // 等待一段時間以確保寫入完成
            Thread.sleep(1000);

            // 讀取 PDF 文件並包裝為 InputStreamResource
            File pdfFile = new File(pdfPath);
            FileInputStream fileInputStream = new FileInputStream(pdfFile);
            InputStreamResource isr = new InputStreamResource(fileInputStream);

            // 返回 InputStreamResource
            return isr;
        } catch (Exception e) {
            // 處理可能的例外
            e.printStackTrace();
            throw e; // 如果需要將例外傳遞給上層
        }
    }

    /**
     * 將 Excel 檔案轉換為 PDF 檔案的方法。
     *
     * @param xlsxPath 輸入的 Excel 檔案路徑
     * @param pdfPath  輸出的 PDF 檔案路徑
     */
    public void excelToPdf(String xlsxPath, String pdfPath) {
        try {
            // 將 XSSFWorkbook 寫入到 Excel 檔案
            try (FileOutputStream fos = new FileOutputStream(xlsxPath)) {
                xssfWorkbook.write(fos);
                fos.flush(); // 確保寫入操作已完成
            } catch (IOException e) {
                // 處理 IO 例外，並輸出更詳細的錯誤訊息
                System.err.println("寫入 Excel 檔案時發生錯誤: " + e.getMessage());
                e.printStackTrace();
            }

            // 從 Excel 檔案載入 Workbook
//            Workbook workbook = new Workbook(xlsxPath); // aspose-cells
            Workbook wb = new Workbook();                 // spire.xls
            wb.loadFromFile(xlsxPath);                    // spire.xls

            // 將 Workbook 儲存為 PDF 檔案
//            workbook.save(pdfPath, SaveFormat.PDF);     // aspose-cells
            wb.saveToFile(pdfPath, FileFormat.PDF);       // spire.xls
        } catch (Exception e) {
            // 處理其他例外，並輸出更詳細的錯誤訊息
            System.err.println("轉換 Excel 到 PDF 時發生錯誤: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 刪除指定路徑的檔案。
     *
     * @param filePath 欲刪除的檔案路徑
     */
    public void deleteFile(String filePath) {
    	// 建立 Path 物件
        Path path = Paths.get(filePath);

        try {
            // 使用 Files 判斷檔案是否存在並進行刪除
            if (Files.exists(path)) {
                Files.delete(path);
                System.out.println(filePath + " 檔案已刪除");
            } else {
                System.out.println(filePath + " 檔案不存在");
            }
        } catch (Exception e) {
            // 處理例外情況
            System.out.println("無法刪除 " + filePath + " 檔案");
            e.printStackTrace();
        }
    }

}
