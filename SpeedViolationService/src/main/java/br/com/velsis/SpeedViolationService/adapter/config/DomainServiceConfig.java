package br.com.velsis.SpeedViolationService.adapter.config;

import br.com.velsis.SpeedViolationService.domain.port.inbound.ViolationEvaluationUseCase;
import br.com.velsis.SpeedViolationService.domain.port.outbound.ViolationConfigPort;
import br.com.velsis.SpeedViolationService.domain.port.outbound.ViolationRepository;
import br.com.velsis.SpeedViolationService.domain.service.ViolationEvaluationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainServiceConfig {

    @Bean
    public ViolationEvaluationUseCase violationEvaluationUseCase(
            ViolationRepository violationRepository,
            ViolationConfigPort violationConfigPort) {
        return new ViolationEvaluationService(violationRepository, violationConfigPort);
    }
}
