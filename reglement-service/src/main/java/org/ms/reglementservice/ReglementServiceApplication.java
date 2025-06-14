package org.ms.reglementservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableMethodSecurity (prePostEnabled = true, securedEnabled = true)

public class ReglementServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReglementServiceApplication.class, args);
	}

}
