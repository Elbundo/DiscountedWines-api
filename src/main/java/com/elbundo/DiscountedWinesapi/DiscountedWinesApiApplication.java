package com.elbundo.DiscountedWinesapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DiscountedWinesApiApplication {
	public static final int MinPrice = 600;

	public static void main(String[] args) {
		SpringApplication.run(DiscountedWinesApiApplication.class, args);
	}

}
