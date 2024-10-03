package com.marek.carsharing.repository.payment.provider;

import com.marek.carsharing.exception.NoProviderException;
import com.marek.carsharing.model.classes.Payment;
import com.marek.carsharing.repository.SpecificationProvider;
import com.marek.carsharing.repository.SpecificationProviderManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentSpecificationProviderManager implements SpecificationProviderManager<Payment> {
    private final List<SpecificationProvider<Payment>> specificationProviders;

    @Override
    public SpecificationProvider<Payment> getSpecificationProvider(String key) {
        return specificationProviders.stream()
                .filter(p -> p.getKey().equals(key))
                .findFirst()
                .orElseThrow(
                        () -> new NoProviderException(
                                "No payment specification provider found for key: " + key
                        )
                );
    }
}
