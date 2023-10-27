package com.example.demo.config.security.handler;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class MyLoginSuccessHandler implements AuthenticationSuccessHandler {

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		//下方為實為 UserDetails，取得用戶的詳細訊息 
		//Object principal = authentication.getPrincipal();
		//UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		
		response.sendRedirect("/");
		
	}

}
