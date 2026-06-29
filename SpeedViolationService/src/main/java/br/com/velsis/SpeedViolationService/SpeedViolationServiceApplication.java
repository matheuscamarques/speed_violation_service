package br.com.velsis.SpeedViolationService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SpeedViolationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpeedViolationServiceApplication.class, args);
	}

}
