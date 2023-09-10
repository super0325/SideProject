package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LogController {
	
	@GetMapping("/login.html")
	public String toLoginPage() {
		return "login";
	}
	
	@PostMapping("/loginCheck")
	public String toLoginCheck() {
		return "/";
	}
	
	@GetMapping("/403")
	public String to403Page() {
		return "403";
	}
	
	@GetMapping("/loginFail")
	public String toLoginFail() {
		return "loginFail";
	}
	
}
