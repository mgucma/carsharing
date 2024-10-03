package com.marek.carsharing.repository.payment.provider.specification;

import com.marek.carsharing.model.classes.Payment;
import com.marek.carsharing.model.classes.Rental;
import com.marek.carsharing.repository.SpecificationProvider;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class UserIdSpecificationProvider implements SpecificationProvider<Rental> {

    public static final String USERS_ID = "usersId";

    @Override
    public String getKey() {
        return USERS_ID;
    }

    @Override
    public Specification<Rental> getSpecification(String[] params) {
        return null;
    }

    public Specification<Rental> getSpecification(Long[] params) {
        return (root, query, criteriaBuilder) -> {
            Join<Payment, Rental> rentalJoin = root.join("rental", JoinType.INNER);
            return rentalJoin.get(USERS_ID).in((Object[]) params);
        };
    }

    @Override
    public Specification<Rental> getSpecification(Boolean params) {
        return null;
    }
}
