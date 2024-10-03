package com.marek.carsharing.repository.rental.provider.specification;

import com.marek.carsharing.model.classes.Rental;
import com.marek.carsharing.repository.SpecificationProvider;
import java.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class IsRentedSpecificationProvider implements SpecificationProvider<Rental> {

    public static final String ACTUAL_RETURN_DATE = "actualReturnDate";
    public static final String RETURN_DATE = "returnDate";

    @Override
    public String getKey() {
        return "isActive";
    }

    @Override
    public Specification<Rental> getSpecification(String[] params) {
        return null;
    }

    @Override
    public Specification<Rental> getSpecification(Long[] params) {
        return null;
    }

    @Override
    public Specification<Rental> getSpecification(Boolean params) {
        return (root, query, criteriaBuilder) -> {
            if (params == null) {
                return null;
            }
            if (params) {
                return criteriaBuilder.and(
                        criteriaBuilder.isNull(
                                root.get(ACTUAL_RETURN_DATE)
                        ),
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.get(RETURN_DATE),
                                LocalDate.now()
                        ));
            } else {
                return criteriaBuilder.or(
                        criteriaBuilder.isNotNull(
                                root.get(ACTUAL_RETURN_DATE)
                        ),
                        criteriaBuilder.lessThan(
                                root.get(RETURN_DATE),
                                LocalDate.now()
                        ));
            }
        };
    }
}

