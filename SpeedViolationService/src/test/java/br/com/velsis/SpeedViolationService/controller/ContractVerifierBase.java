package br.com.velsis.SpeedViolationService.controller;

import br.com.velsis.SpeedViolationService.adapter.inbound.web.ViolationEvaluateController;
import br.com.velsis.SpeedViolationService.domain.port.outbound.ViolationConfigPort;
import br.com.velsis.SpeedViolationService.domain.port.outbound.ViolationRepository;
import br.com.velsis.SpeedViolationService.domain.service.ViolationEvaluationService;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ContractVerifierBase {

    @BeforeEach
    void setUp() {
        ViolationRepository repository = mock(ViolationRepository.class);
        ViolationConfigPort config = mock(ViolationConfigPort.class);

        when(config.getToleranceFixed()).thenReturn(7.0);
        when(config.getTolerancePercentage()).thenReturn(7.0);
        when(config.getThreshold()).thenReturn(100.0);
        when(config.getExcessLimitMedium()).thenReturn(20.0);
        when(config.getExcessLimitSerious()).thenReturn(50.0);

        ViolationEvaluationService service = new ViolationEvaluationService(repository, config);
        ViolationEvaluateController controller = new ViolationEvaluateController(service);

        RestAssuredMockMvc.mockMvc(MockMvcBuilders.standaloneSetup(controller).build());
    }
}
