package com.marek.carsharing.repository.rental;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.marek.carsharing.model.classes.Rental;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RentalRepositoryTest {
    @Autowired
    private RentalRepository rentalRepository;

    @Test
    @DisplayName("""
            """)
    @Sql(scripts = {"classpath:db/repo/clean-rental-repo.sql",
            "classpath:db/repo/add-to-rental-repo.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:db/repo/clean-rental-repo.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findByUserId_getOptionalOfRentalFromUserId() {
        Long userId = 1L;
        Rental testRental = getRental();
        Rental rental = rentalRepository.findByUserId(userId).orElse(null);

        assertAll(
                () -> assertNotNull(rental),
                () -> assertEquals(rental.getUserId(), userId),
                () -> assertEquals(testRental.getRentalDate(), rental.getRentalDate()),
                () -> assertEquals(testRental.getUserId(), rental.getUserId()),
                () -> assertEquals(testRental.getCarId(), rental.getCarId()),
                () -> assertEquals(
                        testRental.getActualReturnDate(), rental.getActualReturnDate()),
                () -> assertEquals(testRental.getReturnDate(), rental.getReturnDate())
        );
    }

    private Rental getRental() {
        Rental rental = new Rental();
        rental.setId(1L);
        rental.setRentalDate(LocalDate.of(2024, 8, 1));
        rental.setReturnDate(LocalDate.of(2024, 8, 5));
        rental.setActualReturnDate(LocalDate.of(2024, 8, 5));
        rental.setCarId(1L);
        rental.setUserId(1L);
        rental.setDeleted(false);
        return rental;
    }
}

