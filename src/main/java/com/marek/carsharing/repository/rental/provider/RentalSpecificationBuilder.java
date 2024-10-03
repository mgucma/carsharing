package com.marek.carsharing.repository.rental.provider;

import com.marek.carsharing.dto.rental.RentalSearchParameters;
import com.marek.carsharing.model.classes.Rental;
import com.marek.carsharing.repository.SpecificationProviderManager;
import com.marek.carsharing.repository.rental.SpecificationBuilder;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RentalSpecificationBuilder implements SpecificationBuilder<Rental> {

    private final SpecificationProviderManager<Rental> specificationProviderManager;

    @Override
    public Specification<Rental> build(RentalSearchParameters searchParameters) {
        Specification<Rental> spec = Specification.where(null);
        if (searchParameters.userId() != null
                && searchParameters.userId().length > 0) {
            Long[] userIds = Arrays.stream(searchParameters.userId())
                    .map(s -> s.replaceAll("[\\[\\]\"]", ""))
                    .map(Long::valueOf)
                    .toArray(Long[]::new);
            spec = spec.and(
                    specificationProviderManager.getSpecificationProvider("userId")
                            .getSpecification(userIds));
        }
        if (searchParameters.isActive() != null) {
            spec = spec.and(
                    specificationProviderManager.getSpecificationProvider("isActive")
                            .getSpecification(searchParameters.isActive()));
        }
        return spec;
    }
}


