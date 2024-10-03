package com.marek.carsharing.repository.payment;

import com.marek.carsharing.dto.payment.PaymentSearchParameters;
import org.springframework.data.jpa.domain.Specification;

public interface SpecificationBuilder<T> {
    Specification<T> build(PaymentSearchParameters searchParameters);
}
