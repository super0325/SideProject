package com.example.demo.config.security;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class SpringUserService implements UserDetailsService {
	
	private static final Logger logger = LogManager.getLogger(SpringUserService.class);

	@Autowired
	private UserRepository userRepository;

	/*
	 * 自訂義邏輯驗證 (數據庫)
	 * @param	username
	 * @return	Spring Security 使用的用戶物件
	 * @throws	UsernameNotFoundException
	 */
	@Override
	public UserDetails loadUserByUsername(String account) throws UsernameNotFoundException {

		try {
			User user = userRepository.findAccount(account);	
			
			String userAccount = user.getAccount();
			String encodePassword = new BCryptPasswordEncoder().encode(user.getPassword());
						
			// 創建一個存儲用戶角色的權限列表
	        List<GrantedAuthority> authorities = new ArrayList<>();
	        // 添加使用者的角色作為權限
	        authorities.add(new SimpleGrantedAuthority(user.getRole().getName()));

			return new org.springframework.security.core.userdetails.User(userAccount, encodePassword, authorities);
		
		} catch (Exception e) {
			logger.error("SpringUserService錯誤 => ", e.getMessage());
			throw new UsernameNotFoundException("Username is wrong.");
		}
	}

}
