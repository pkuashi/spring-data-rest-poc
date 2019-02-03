package com.example.springdatarestpoc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@SpringBootApplication(exclude={RepositoryRestMvcAutoConfiguration.class})
@Configuration
public class SpringDataRestPocApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringDataRestPocApplication.class, args);
	}
}

