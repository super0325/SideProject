package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {
	
	@Query(value = "select * from userAccount where account = :account", nativeQuery = true)
	User findAccount(@Param("account") String account);

}
