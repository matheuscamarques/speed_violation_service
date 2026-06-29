package br.com.velsis.SpeedViolationService.adapter.outbound.persistence;

import br.com.velsis.SpeedViolationService.domain.model.Violation;
import br.com.velsis.SpeedViolationService.domain.port.outbound.ViolationRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryViolationRepository implements ViolationRepository {

    private final ConcurrentHashMap<String, List<Violation>> violations = new ConcurrentHashMap<>();

    @Override
    public void save(Violation violation) {
        violations.compute(violation.licensePlate(), (key, list) -> {
            List<Violation> newList;
            if (list == null) {
                newList = new ArrayList<>();
            } else {
                newList = new ArrayList<>(list);
            }
            newList.add(violation);
            return Collections.unmodifiableList(newList);
        });
    }

    @Override
    public List<Violation> findByLicensePlate(String licensePlate) {
        return violations.getOrDefault(licensePlate, List.of());
    }
}
