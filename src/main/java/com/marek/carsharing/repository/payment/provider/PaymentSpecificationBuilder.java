package com.marek.carsharing.repository.payment.provider;

import com.marek.carsharing.dto.payment.PaymentSearchParameters;
import com.marek.carsharing.model.classes.Payment;
import com.marek.carsharing.repository.SpecificationProviderManager;
import com.marek.carsharing.repository.payment.SpecificationBuilder;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentSpecificationBuilder implements SpecificationBuilder<Payment> {
    private final SpecificationProviderManager<Payment> specificationProviderManager;

    @Override
    public Specification<Payment> build(PaymentSearchParameters searchParameters) {
        Specification<Payment> spec = Specification.where(null);
        if (searchParameters.usersId() != null
                && searchParameters.usersId().length > 0) {
            Long[] usersIds = Arrays.stream(searchParameters.usersId())
                    .map(s -> s.replaceAll("[\\[\\]\"]", ""))
                    .map(Long::valueOf)
                    .toArray(Long[]::new);
            spec = spec.and(
                    specificationProviderManager.getSpecificationProvider("usersId")
                            .getSpecification(usersIds));
        }
        return spec;
    }
}

