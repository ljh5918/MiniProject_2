package com.mycom.myapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class MiniProject2Application {

	public static void main(String[] args) {
		SpringApplication.run(MiniProject2Application.class, args);
	}

}
