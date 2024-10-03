package com.marek.carsharing.repository.rental.provider.specification;

import com.marek.carsharing.model.classes.Rental;
import com.marek.carsharing.repository.SpecificationProvider;
import java.util.Arrays;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class UserIdRentalSpecificationProvider implements SpecificationProvider<Rental> {

    public static final String USER_ID = "userId";

    @Override
    public String getKey() {
        return USER_ID;
    }

    @Override
    public Specification<Rental> getSpecification(String[] params) {
        return null;
    }

    public Specification<Rental> getSpecification(Long[] params) {
        return (root, query, criteriaBuilder) -> root
                .get(USER_ID)
                .in(Arrays.stream(params).toArray());
    }

    @Override
    public Specification<Rental> getSpecification(Boolean params) {
        return null;
    }
}
