package com.example.demo.entity;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "person")
@Component
public class Person {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "PID")
	private int pId;

	@Column(name = "NAME")
	private String name;
	
	@Column(name = "SEX")
	private String sex;
	
	@Column(name = "MONEY")
	private BigDecimal money;
	
	@Column(name = "BIRTHDATE")
	private Date birthdate;


	public int getpId() {
		return pId;
	}
	
	public void setpId(int pId) {
		this.pId = pId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public BigDecimal getMoney() {
		return money;
	}

	public void setMoney(BigDecimal money) {
		this.money = money;
	}

	public Date getBirthdate() {
		return birthdate;
	}

	public void setBirthdate(Date birthdate) {
		this.birthdate = birthdate;
	}
	
	
	// for getterListForExcel
	public String getpIdStringForExcel() {
		return String.valueOf(pId);
	}
	// for getterListForExcel
	public String getMoneyStingForExcel() {
        // BigDecimal 類型轉換為字串，可以根據需要指定精確度
		return money.toPlainString(); // 或者使用其他適當的方法進行轉換;
	}
	// for getterListForExcel
	public String getBirthdateStringForExcel() {
		// Date 類型轉換為指定格式的字串，這裡使用 yyyy-MM-dd 作為日期格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(birthdate);
	}

	
	// 自訂義需要的getter列表，且回傳值必須為String
	public List<String> getterListForExcel() {
		List<String> list = new ArrayList<>();

		list.add("getpIdStringForExcel");
		list.add("getName");
		list.add("getSex");
		list.add("getMoneyStingForExcel");
		list.add("getBirthdateStringForExcel");
	    
		return list;
	}
	
}
