package com.example.demo.config.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.demo.config.security.handler.MyLoginFailHandler;
import com.example.demo.config.security.handler.MyLoginSuccessHandler;
import com.example.demo.config.security.handler.MyLogoutSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	private static final Logger logger = LogManager.getLogger(SecurityConfig.class);
	
	@Autowired
	private MyLoginSuccessHandler myLoginSuccessHandler;
	
	@Autowired
	private MyLoginFailHandler myLoginFailHandler;
	
	@Autowired
	private MyLogoutSuccessHandler myLogoutSuccessHandler;


    @Bean
    SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
		
		try {
			httpSecurity
			    //前後端分離不需要CSRF（跨站請求偽造）攻擊的防護，關閉
				.csrf((csrf) -> csrf
						.disable()
						)
				//驗證. 權限
				.authorizeHttpRequests((authz) -> authz
						.requestMatchers("/", "/index", "/public").permitAll()
						.requestMatchers("/private/**").hasRole("MASTER")
						.anyRequest().authenticated()
						)
				//自定義登入頁面
				.formLogin((formLogin) -> formLogin
			            .loginPage("/login.html") //自定義登入頁面
			            .loginProcessingUrl("/loginCheck") // 登入路徑，表單會提交到這個路徑，提交後會自動執行 UserDetailsService 的方法
						.successHandler(myLoginSuccessHandler)
						.failureHandler(myLoginFailHandler)
//			            .defaultSuccessUrl("/index")
			            .permitAll()
			            )
//				.rememberMe((rememberMe) -> rememberMe
//						.rememberMeParameter("remember-me")
//						.tokenValiditySeconds(20)
//						.userDetailsService(springUserService))
				.logout((logout) ->logout
						.logoutUrl("/logout")
						.logoutSuccessHandler(myLogoutSuccessHandler)
//						.clearAuthentication(true) // 清除認證狀態，默認為true
//						.invalidateHttpSession(true) // 銷毁HttpSession對象，默認為true
						)
				//沒有權限
		        .exceptionHandling((exceptionHandling) -> exceptionHandling
		        		.accessDeniedPage("/403")
//		        		)
//		        // 前後端分離是無狀態的，不需要session，禁用
//	            .sessionManagement(session -> session
//	            		.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
	            		);
			
			return httpSecurity.build();
			
		} catch (Exception e) {
			logger.error("SecurityConfig錯誤 => ", e.getMessage());
			return null;
		}
	}


    @Bean
    PasswordEncoder password(){
	    return new BCryptPasswordEncoder();
	}

}
