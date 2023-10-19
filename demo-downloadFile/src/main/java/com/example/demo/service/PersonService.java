package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Person;
import com.example.demo.repository.PersonRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class PersonService {
	
	@Autowired
	private Person person;
	
	@Autowired
	private PersonRepository personRepository;
	
	
	public List<Person> findAll() {
		return personRepository.findAll();
	}
	
	public List<String> getterListForExcel() {
		return person.getterListForExcel();
	}

}
