package com.example.demo.controller;

import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.entity.Person;
import com.example.demo.service.PersonService;
import com.example.demo.utils.CustomMediaTypes;
import com.example.demo.utils.ExcelUtil;

@Controller
public class ExcelController {
	
	@Autowired
	private PersonService personService;
	
	@Autowired
	private ExcelUtil excelUtil;
	
	
	@GetMapping({"/", "/downloadPage"})
	public String toDownloadPage() {
		return "downloadPage";
	}
	
	
	@GetMapping("/downloadExcel")
	public ResponseEntity<InputStreamResource> downloadExcel() throws Exception {
		
	    // 指定Excel模板路徑，resources下方
	    String templatePath = "static/excelTemplate/testDownload.xlsx";
	    // 獲取要填充到Excel中的數據
	    List<Person> dataList = personService.findAll();
	    // 獲取需要的getter列表
	    List<String> getterListForExcel = personService.getterListForExcel();
	    
	    // 使用Excel工具類根據模板和數據生成XSSFWorkbook
	    XSSFWorkbook xssfWorkbook = excelUtil.simpleExcelMaker(templatePath, dataList, 6, 1, getterListForExcel);
	    excelUtil.setValueToCell(xssfWorkbook, 3, 1, "日期測試 : yyyy-MM-dd tt");
	    // 將XSSFWorkbook轉換為InputStreamResource，以便返回到前端
	    InputStreamResource resultStream = excelUtil.convertExcelToInputStreamReasource(xssfWorkbook);
	    
	    String customFileName = "yourCustomFileName.xlsx";
	    
	    // 創建ResponseEntity並返回，設置必要的HTTP標頭
	    return ResponseEntity.ok()
	            .header("Access-Control-Expose-Header", "Content-Disposition") // 允許訪問Content-Disposition標頭
//	            .contentType(MediaType.APPLICATION_OCTET_STREAM)
	            .contentType(CustomMediaTypes.APPLICATION_XLSX) // 設置Content-Type為自定義的XLSX類型
				.header("Content-Disposition", "attachment; filename=" + customFileName)
	            .body(resultStream);
	}


}
