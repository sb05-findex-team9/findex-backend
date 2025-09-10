package com.codeit.findex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class FindexApplication {

	public static void main(String[] args) {

		SpringApplication.run(FindexApplication.class, args);
		System.out.println("***************Index Service Started*****************");
	}

}
