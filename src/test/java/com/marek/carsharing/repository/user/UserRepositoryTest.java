package com.marek.carsharing.repository.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.marek.carsharing.model.classes.User;
import com.marek.carsharing.model.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("""
            """)
    @Sql(scripts = {"classpath:db/repo/clean-user-repo.sql",
            "classpath:db/repo/add-to-user-repo.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:db/repo/clean-user-repo.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findUserByEmail_getOptionalOfUserFromEmail() {
        String email = "admin@simpleart.eu";
        User testUser = getUser();

        User user = userRepository.findUserByEmail(email).orElse(null);

        assertAll(
                () -> assertNotNull(user),
                () -> assertEquals(email, user.getEmail()),
                () -> assertEquals(testUser.getId(), user.getId()),
                () -> assertEquals(testUser.getEmail(), user.getEmail()),
                () -> assertEquals(testUser.getRole(), user.getRole()),
                () -> assertEquals(testUser.getFirstName(), user.getFirstName()),
                () -> assertEquals(testUser.getLastName(), user.getLastName())
        );
    }

    private User getUser() {
        User user = new User();
        user.setId(1L);
        user.setFirstName("B");
        user.setLastName("C");
        user.setRole(Role.MANAGER);
        user.setPassword("password");
        user.setEmail("admin@simpleart.eu");
        user.setDeleted(false);
        return user;
    }
}
