package br.com.velsis.SpeedViolationService.adapter.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Speed Violation Service")
                        .description("Microserviço para avaliação de infrações de excesso de velocidade " +
                                "com base nos arts. 218-I, 218-II e 218-III do CTB " +
                                "(Código de Trânsito Brasileiro).")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Velsis"))
                        .license(new License()
                                .name("Proprietary")));
    }
}
