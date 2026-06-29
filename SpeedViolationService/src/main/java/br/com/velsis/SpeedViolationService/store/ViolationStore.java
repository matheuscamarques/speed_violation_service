package br.com.velsis.SpeedViolationService.store;

import br.com.velsis.SpeedViolationService.model.Violation;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ViolationStore {

    private final ConcurrentHashMap<String, List<Violation>> violations = new ConcurrentHashMap<>();

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

    public List<Violation> findByLicensePlate(String licensePlate) {
        return violations.getOrDefault(licensePlate, List.of());
    }
}
