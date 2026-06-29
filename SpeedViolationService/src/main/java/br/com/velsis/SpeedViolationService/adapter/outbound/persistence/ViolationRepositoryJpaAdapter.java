package br.com.velsis.SpeedViolationService.adapter.outbound.persistence;

import br.com.velsis.SpeedViolationService.domain.model.Violation;
import br.com.velsis.SpeedViolationService.domain.port.outbound.ViolationRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("h2")
public class ViolationRepositoryJpaAdapter implements ViolationRepository {

    private final ViolationJpaRepository repository;

    public ViolationRepositoryJpaAdapter(ViolationJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(Violation violation) {
        repository.save(new ViolationEntity(violation));
    }

    @Override
    public List<Violation> findByLicensePlate(String licensePlate) {
        return repository.findByLicensePlate(licensePlate).stream()
                .map(ViolationEntity::toDomain)
                .toList();
    }
}
