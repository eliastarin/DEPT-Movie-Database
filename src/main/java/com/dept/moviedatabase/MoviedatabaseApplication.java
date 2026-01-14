package com.dept.moviedatabase;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class MoviedatabaseApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoviedatabaseApplication.class, args);
	}

}
