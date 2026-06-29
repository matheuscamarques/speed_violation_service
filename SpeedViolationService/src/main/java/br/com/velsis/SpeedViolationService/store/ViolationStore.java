package br.com.velsis.SpeedViolationService.store;

import br.com.velsis.SpeedViolationService.dto.ViolationResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ViolationStore {

    private final ConcurrentHashMap<String, List<ViolationResponse>> violations = new ConcurrentHashMap<>();

    public void save(ViolationResponse response) {
        violations.compute(response.licensePlate(), (key, list) -> {
            var newList = list == null ? new ArrayList<ViolationResponse>() : new ArrayList<>(list);
            newList.add(response);
            return Collections.unmodifiableList(newList);
        });
    }

    public List<ViolationResponse> findByLicensePlate(String licensePlate) {
        return violations.getOrDefault(licensePlate, List.of());
    }
}
