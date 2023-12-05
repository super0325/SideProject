package com.example.demo.controller;

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
public class DownloadFileController {
	
	@Autowired
	private PersonService personService;
	
	@Autowired
	private DownloadFileUtil dfu;
	
	
	@GetMapping({"/", "/downloadPage"})
	public String toDownloadPage() {
		return "downloadPage";
	}
	
	
	/**
     * 匯出 XLSX
     * 
     * @throws Exception 
     */
	@GetMapping("/downloadXLSX")
	public ResponseEntity<InputStreamResource> downloadXLSX() throws Exception {
		
	    // 指定Excel模板路徑，resources下方
	    String templatePath = "static/excelTemplate/testDownload_2.xlsx";
	    // 獲取要填充到Excel中的數據
	    List<Person> dataList = personService.findAll();
	    // 獲取需要的getter列表
	    List<String> getterListForExcel = personService.getterListForExcel();
	    
	    // 使用Excel工具類根據模板和數據生成XSSFWorkbook
	    dfu.simpleExcelMaker(templatePath, 1, dataList, 6, 2, getterListForExcel);
	    dfu.setValueToCell(1, 3, 1, "日期測試 : yyyy-MM-dd tt");
	    
	    // 傳入需要的 XSSFWorkbook 及頁面索引，並轉換為 InputStreamResource，以便返回到前端
	    InputStreamResource isr = dfu.convertXlsxToISR(1);
	    
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
    	InputStreamResource isr = dfu.convertXlsxToCsv(1);
        
    	String fileName = "myFileName.csv";

    	return ResponseEntity.ok()
	            .header("Access-Control-Expose-Header", "Content-Disposition") // 允許訪問Content-Disposition標頭
	            .contentType(MyMediaType.CSV.getMediaType()) // 設置Content-Type為自定義的CSV類型
				.header("Content-Disposition", "attachment; filename=" + fileName)
	            .body(isr);
	}
	
}
