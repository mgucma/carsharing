package com.marek.carsharing.repository.user;

import com.marek.carsharing.model.classes.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long>,
        JpaSpecificationExecutor<User> {

    @Query("SELECT u FROM User u "
            + "WHERE u.email = :email AND u.isDeleted = false")
    Optional<User> findUserByEmail(String email);
}
