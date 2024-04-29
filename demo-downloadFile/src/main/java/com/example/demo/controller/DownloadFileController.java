package com.example.demo.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.entity.Person;
import com.example.demo.myEnum.MyMediaType;
import com.example.demo.service.PersonService;
import com.example.demo.utils.DownloadFileUtil;

@Controller
public class DownloadFileController extends DownloadFileUtil {
	
	@Autowired
	private PersonService personService;
	
	
	@GetMapping({"/", "/downloadPage"})
	public String toDownloadPage() {
		return "downloadPage";
	}
	
	/**
     * 匯出 XLSX
     * 需先製作模板，且預設每頁限制排數，模板排來源及為其的下一排
     * 
     * @throws Exception 
     */
	@GetMapping("/downloadXLSX")
	public ResponseEntity<InputStreamResource> downloadXLSX() throws Exception {
		
	    // 指定Excel模板路徑，resources下方
	    String templatePath = "static/excelTemplate/testDownload_1.xlsx";
	    // 獲取要填充到Excel中的數據
	    List<Person> dataList = personService.findAll();
	    // 獲取需要的getter列表
	    List<String> getterListForExcel = personService.getterListForExcel();
	    // 模板首頁的標題排數
	    templateTitleRows = 4;
	    
	    // 使用Excel工具類根據模板和數據生成XSSFWorkbook
	    simpleExcelMaker(templatePath, dataList, 5, 1, getterListForExcel, templateTitleRows, 8);
//	    dfu.simpleExcelMaker(templatePath, dataList, 5, 2, getterListForExcel, templateTitleRows, 8);
	    setValueToCell(1, 2, 1, "日期測試 : yyyy-MM-dd tt");
	    
	    // 將需要的 XSSFWorkbook，轉換為 InputStreamResource，以便返回到前端
	    InputStreamResource isr = convertXlsxToISR();
	    
	    String fileName = "myFileName.xlsx";
	    
	    // 創建ResponseEntity並返回，設置必要的HTTP標頭
	    return ResponseEntity.ok()
	            .header("Access-Control-Expose-Header", "Content-Disposition") // 允許訪問Content-Disposition標頭
	            .contentType(MyMediaType.XLSX.getMediaType()) // 設置Content-Type為自定義的XLSX類型
				.header("Content-Disposition", "attachment; filename=" + fileName)
	            .body(isr);
	}
	
	/**
     * 匯出 CSV，必需先製作 XLSX
     * 
     * @throws Exception 
     */
	@GetMapping("/downloadCSV")
    public ResponseEntity<InputStreamResource> downloadCsv() throws Exception {
    	
    	// 先製作 Excel
    	downloadXLSX();
    	
    	// 傳入需要的 XSSFWorkbook 以及頁數索引，並轉換為 Csv
    	InputStreamResource isr = convertXlsxToCsv();
        
    	String fileName = "myFileName.csv";

    	return ResponseEntity.ok()
	            .header("Access-Control-Expose-Header", "Content-Disposition") // 允許訪問Content-Disposition標頭
	            .contentType(MyMediaType.CSV.getMediaType()) // 設置Content-Type為自定義的CSV類型
				.header("Content-Disposition", "attachment; filename=" + fileName)
	            .body(isr);
	}
	
	//TODO 產出PDF浮水印未解決
	/**
	 * 匯出 PDF，必需先製作 XLSX
	 * 
	 * @throws Exception 
	 */
	@GetMapping("/downloadPDF")
	public ResponseEntity<InputStreamResource> downloadPdf() throws Exception {
		
		// 先製作 Excel
		downloadXLSX();
		
		String currentTime = currentTime_yyyyMMddHHmmssSSS();
		
		// 定義 Excel 檔案和 PDF 檔案的路徑
//		String xlsxPath = "C:/Users/user/Desktop/xlsx_" + currentTime + ".xlsx";
//		String pdfPath = "C:/Users/user/Desktop/pdf_" + currentTime + ".pdf";
		String xlsxPath = "C:/Users/Hyweber/Desktop/xlsx_" + currentTime + ".xlsx";
		String pdfPath = "C:/Users/Hyweber/Desktop/pdf_" + currentTime + ".pdf";
		
		// 將XSSFWorkbook 轉換為 Pdf
		InputStreamResource isr = convertXlsxToPdf(xlsxPath, pdfPath);
		
		// 刪除 Excel 檔案
		deleteFile(xlsxPath);
		
		String fileName = "myFileName.pdf";
		
		return ResponseEntity.ok()
				.header("Access-Control-Expose-Header", "Content-Disposition") // 允許訪問Content-Disposition標頭
				.contentType(MyMediaType.PDF.getMediaType()) // 設置Content-Type為自定義的PDF類型
				.header("Content-Disposition", "attachment; filename=" + fileName)
				.body(isr);
	}
	
	/*
	 * 獲取當前時間，yyyyMMddHHmmssSSS 格式
	 */
	private String currentTime_yyyyMMddHHmmssSSS() {
		Date currentTime = new Date();
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		return sdf.format(currentTime);
	}
	
}
