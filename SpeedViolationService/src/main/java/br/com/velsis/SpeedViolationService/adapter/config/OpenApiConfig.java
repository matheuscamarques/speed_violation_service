package br.com.velsis.SpeedViolationService.adapter.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server()
                                .url("https://speedviolationservice-production.up.railway.app")
                                .description("Produção (Railway)"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("Desenvolvimento local")
                ))
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
