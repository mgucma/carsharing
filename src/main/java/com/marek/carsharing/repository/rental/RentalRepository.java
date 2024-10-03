package com.marek.carsharing.repository.rental;

import com.marek.carsharing.model.classes.Rental;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long>,
        JpaSpecificationExecutor<Rental> {

    @Query("SELECT r FROM Rental r "
            + "WHERE r.userId = :userId AND r.isDeleted = false")
    Optional<Rental> findByUserId(Long userId);
}
