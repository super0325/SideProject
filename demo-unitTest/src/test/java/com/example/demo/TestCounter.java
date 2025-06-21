package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestCounter {
	
	@Autowired
	private Counter counter;
	
	@Test
	public void testSomething() {
		
		assertEquals("1 2 clap 4 ha clap 7 8 clap ha", counter.getCount(10));
		assertEquals("1 2 clap 4 ha clap 7 8 clap ha 11 clap 13 14 clapha", counter.getCount(15));
		assertEquals("", counter.getCount(0));
		assertEquals("", counter.getCount(-1));
	}

}
