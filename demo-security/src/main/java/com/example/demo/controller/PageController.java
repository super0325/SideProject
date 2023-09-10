package com.example.demo.controller;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.config.security.SpringUserService;

@Controller
public class PageController {
	
	@Autowired
	private SpringUserService springUserService;
	
	@GetMapping({"/", "/index"})
	public String toIndexPage() {
		return "index";
	}
	
	@GetMapping("/public")
	@ResponseBody
	public Object toPublic() {
		
		ArrayList<UserDetails> list = new ArrayList<>();

		UserDetails userDetails1 = springUserService.loadUserByUsername("jack");
		UserDetails userDetails2 = springUserService.loadUserByUsername("andy");
		
		list.add(userDetails1);
		list.add(userDetails2);
		
		return list;
		
	}
	
	@GetMapping("/private")
	public Object toPrivate() {
		
		return "privatePage";
	}

}
