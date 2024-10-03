package com.marek.carsharing.repository.rental.provider;

import com.marek.carsharing.exception.NoProviderException;
import com.marek.carsharing.model.classes.Rental;
import com.marek.carsharing.repository.SpecificationProvider;
import com.marek.carsharing.repository.SpecificationProviderManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RentalSpecificationProviderManager implements SpecificationProviderManager<Rental> {
    private final List<SpecificationProvider<Rental>> specificationProviders;

    @Override
    public SpecificationProvider<Rental> getSpecificationProvider(String key) {
        return specificationProviders.stream()
                .filter(p -> p.getKey().equals(key))
                .findFirst()
                .orElseThrow(
                        () -> new NoProviderException(
                                "No specification provider found for key: " + key)
                );
    }
}

