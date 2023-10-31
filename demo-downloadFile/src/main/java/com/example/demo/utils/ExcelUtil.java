package com.example.demo.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class ExcelUtil {
	
	@Autowired
	private ResourceLoader resourceLoader;
	
	
	/**
	 * 根據資源路徑獲取 InputStream。
	 * 
	 * @param templatePath 模板檔案的資源路徑，使用spring的ResourceLoader，路徑會在主目錄類下
	 * 
	 * @return InputStream 物件，如果資源不存在則返回 null
	 */
	private InputStream getTemplateAsInputStream(String templatePath) {
	    InputStream templateStream = null;
	    try {
//	        // 獲取ServletContext
//	        ServletContext servletContext = getServletContext();
//	        // 使用ServletContext的getResourceAsStream方法獲取資源流
//	        templateStream = servletContext.getResourceAsStream(templatePath);
	    	
	    	// 假設使用Spring的ResourceLoader來獲取資源流，會位於resources主目錄類下方
	    	String resourcePath = "classpath:" + templatePath;
	    	Resource resource = resourceLoader.getResource(resourcePath);
	    	templateStream = resource.getInputStream();
	        
	    } catch (Exception e) {
	    	System.out.println("無法獲取ServletContext資源: ");
        	e.printStackTrace();
	    }
	    
	    return templateStream;
	}

	/**
	 * 根據自定義模板檔案，以及資料列表生成 XSSFWorkbook（Excel 工作簿）。
	 * 模板檔案的第一列，須給樣板樣式
	 * @param <T>
	 * 
	 * @param templatePath          模板檔案的資源路徑
	 * @param dataList              包含資料的列表
	 * @param targetRowIndex        目標"列"的起始索引，上方，工作表中的特定位置
	 * @param columnIndexLeft       目標"欄"的起始索引，左側，工作表中的特定位置
	 * @param getterListForExcel    包含需要的getter列表
	 * 
	 * @return XSSFWorkbook 物件，代表生成的 Excel 工作簿
	 * 
	 * @throws Exception 
	 */
	public <T> XSSFWorkbook simpleExcelMaker(String templatePath, List<T> dataList, int targetRowIndex, int columnIndexLeft, List<String> getterListForExcel) throws Exception {
	    // 獲取模板文件的 InputStream
		try (InputStream templateStream = getTemplateAsInputStream(templatePath);) {
				
			// 基於模板，創建一個新的 XSSFWorkbook 對象，代表 Excel 工作簿
			XSSFWorkbook xssfWorkbook = new XSSFWorkbook(templateStream);
			// 獲取工作表對象，代表 Excel 中的頁
            XSSFSheet sheet = xssfWorkbook.getSheetAt(0);
            // 模板中的第一列，來源列，即樣板樣式
            int sourceRowIndex = 1;
            
            // 使用dataList物件，根據資料數量 動態產生，並複製樣式，同時設置儲存格的值
            for (int i = 0; i < dataList.size(); i++) {
            	T data = dataList.get(i);
            	copyCellStyleAndHeightAndSetValue(sheet, data, sourceRowIndex, i + targetRowIndex, columnIndexLeft, getterListForExcel);
            }
            return xssfWorkbook;
	            
		} catch (IOException e) {
			System.out.println("exportByCustomizationTemplate錯誤: ");
        	e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 複製來源列的儲存格高度以及樣式(包括字體)至目標列的儲存格，同時設置值。
	 *
	 * @param <T>                 泛型型別，表示傳入的 data 對象的型別
	 * 
	 * @param sheet               工作表對象，代表 Excel 中的頁
	 * @param data                包含資料的對象
	 * @param sourceRowIndex      來源列的索引
	 * @param targetRowIndex      目標列的索引
	 * @param columnIndexLeft     最左側儲存格的索引
	 * @param getterListForExcel  包含需要的getter列表
	 * 
	 * @throws AfCommonException 
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
        
        // 計算 Excel 表格右側的列數
		int columnIndexRight = getterListForExcel.size();
        // columnIndexLeft最左側行 至 columnIndexRight最右側行 (儲存格)
        for (int i = columnIndexLeft; i <= columnIndexRight; i++) {
        	// 真實列索引 (儲存格)
        	int realColumnIndex = i - 1; 
        	// 取得真實來源儲存格，如無目標 創建真實來源儲存格
        	XSSFCell sourceCell = sourceRow.getCell(realColumnIndex) != null ? sourceRow.getCell(realColumnIndex) : sourceRow.createCell(realColumnIndex);
        	// 取得真實目標儲存格，如無目標 創建真實目標儲存格
        	XSSFCell targetCell = targetRow.getCell(realColumnIndex) != null ? targetRow.getCell(realColumnIndex) : targetRow.createCell(realColumnIndex);

            // 複製來源儲存格的樣式(包括字體)到目標儲存格
        	copyCellStyle(sheet, sourceCell, targetCell);
        	
            // 根據realColumnIndex 找到相對應的getterMethodName
    	    String getterMethodName = getterListForExcel.get(realColumnIndex);
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
    public <T> String invokeGetterFromDataAndReturnStringResult(String getterMethodName, T data) throws Exception {
    	// 傳入為空字串，代表預期結果為在工作表上中留空
        if (getterMethodName == "") {
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
            e.printStackTrace();
            throw new NoSuchMethodException(errorMessage);
        } catch (ReflectiveOperationException e) {
            // 反射操作異常的處理
            String errorMessage = "反射操作異常: ";
        	e.printStackTrace();
            throw new ReflectiveOperationException(errorMessage);
        }

        // 無法成功獲取屬性值或結果為 null，回傳空字串
        return ""; // 預設回傳空字串
    }

    /**
     * 此方法用於設定目標儲存格的值。
     * 
     * @param xssfWorkbook         XSSFWorkbook 對象，代表 Excel 工作簿
     * @param rowIndex             目標儲存格所在的行索引
     * @param columnIndex          目標儲存格所在的列索引
     * @param stringValue          要設定的目標儲存格值 (字串)
     */
    public void setValueToCell(XSSFWorkbook xssfWorkbook, int rowIndex, int columnIndex, String stringValue) {
    	// 獲取工作表
    	XSSFSheet sheet = xssfWorkbook.getSheetAt(0);
    	
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
     * @param xssfWorkbook   要轉換的 XSSFWorkbook 對象
     * @return InputStreamResource 物件，包含轉換後的資源
     */
    public InputStreamResource convertExcelToInputStreamReasource(XSSFWorkbook xssfWorkbook) {
    	
    	ByteArrayOutputStream baos = new ByteArrayOutputStream(); // 創建一個字節陣列輸出流
    	InputStreamResource inputStreamResource = null; // 這個物件可用於後續操作，例如設定 HTTP 響應的主體部分
    	
    	try {
    		// 獲取工作表
    		XSSFSheet sheet = xssfWorkbook.getSheetAt(0);
    		// 刪除第一列（索引為0），並向上移動所有列
    		sheet.shiftRows(1, sheet.getLastRowNum(), -1); 
    		// 將 XSSFWorkbook 寫入到字節陣列輸出流中
    		xssfWorkbook.write(baos);
    		
    		// 將字節陣列轉換為 ByteArrayInputStream 並設定為 InputStream
//    		this.setFileInputStream(new ByteArrayInputStream(baos.toByteArray()));
    		// 設定匯出檔案名稱，使用特定格式的日期時間作為一部分
//    		this.setExportFileName("Case0240_" + DateFormatUtils.format(new Date(), DateFormatPattern.DATETIME_NOSECOND_PATTERN_STD));
    		
//    		setDownloadFileTokenCookie();
    		
    		// 將字節陣列轉換為 ByteArrayInputStream 並設定為 InputStream
    		InputStream inputStream = new ByteArrayInputStream(baos.toByteArray());
    		// 創建一個 InputStreamResource 物件，用於封裝 InputStream
    		inputStreamResource = new InputStreamResource(inputStream);
    		
    	} catch (IOException e) {
    		System.out.println("無XSSFWorkbook資源: ");
        	e.printStackTrace();
    	}
    	
    	return inputStreamResource;
    }

}
