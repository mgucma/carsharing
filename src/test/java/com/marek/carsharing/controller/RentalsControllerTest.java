package com.marek.carsharing.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marek.carsharing.dto.rental.CreateRentalRequestDto;
import com.marek.carsharing.dto.rental.RentalDto;
import com.marek.carsharing.dto.rental.RentalSearchParameters;
import com.marek.carsharing.model.classes.Rental;
import com.marek.carsharing.model.classes.User;
import com.marek.carsharing.model.enums.Role;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RentalsControllerTest {
    public static final long ID = 1L;
    public static final String EMAIL = "admin@simpleart.eu";
    public static final String FIRST_NAME = "Admin";
    public static final String LAST_NAME = "User";
    public static final String PASSWORD = "password";
    public static final Role ROLE = Role.MANAGER;
    public static final LocalDate RENTAL_DATE = LocalDate.of(2024, 8, 1);
    public static final LocalDate RETURN_DATE = LocalDate.of(2024, 9, 29);
    public static final LocalDate ACTUAL_RETURN_DATE = LocalDate.of(2024, 8, 5);
    public static final String[] USER_ID = {"1"};
    public static final boolean IS_ACTIVE = true;
    private static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void beforeAll() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @AfterEach
    void afterAll() {
        teardown(dataSource);
    }

    @SneakyThrows
    private static void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("db/controller/delete-from-rentals.sql")
            );
        }
    }

    @Test
    @WithUserDetails(value = "admin@simpleart.eu")
    @Sql(scripts = {"classpath:db/controller/add-to-cars.sql",
            "classpath:db/controller/add-to-users.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:db/controller/delete-from-cars.sql",
            "classpath:db/controller/delete-from-users.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Test adding a rental successfully")
    void addRental_Success() throws Exception {
        CreateRentalRequestDto createRentalRequestDto = getCreateRentalRequestDto();
        RentalDto rentalDto = getRentalDto();

        String json = objectMapper
                .writeValueAsString(createRentalRequestDto);

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.post("/rentals")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andReturn();

        RentalDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                RentalDto.class);

        EqualsBuilder.reflectionEquals(rentalDto, actual, "id");
    }

    @Test
    @WithMockUser(username = "user", authorities = {"MANAGER"})
    @Sql(scripts = {"classpath:db/controller/add-to-cars.sql",
            "classpath:db/controller/add-to-rentals.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:db/controller/delete-from-cars.sql",
            "classpath:db/controller/delete-from-rentals.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Test getting all rentals successfully with search parameters")
    void getRentals_Success() throws Exception {
        RentalSearchParameters parameters = getRentalSearchParameters();

        RentalDto rentalDto = getRentalDto();

        String json = objectMapper.writeValueAsString(parameters);

        MvcResult result = mockMvc.perform(
                        get("/rentals")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        List<RentalDto> actual = Arrays.asList(
                objectMapper.readValue(result.getResponse().getContentAsString(),
                        RentalDto[].class));

        EqualsBuilder.reflectionEquals(rentalDto, actual.get(0), "id");
    }

    @Test
    @WithMockUser(username = "user", authorities = {"MANAGER"})
    @DisplayName("Test getting specific rental successfully - MANAGER only")
    @Sql(scripts = {"classpath:db/controller/add-to-rentals.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:db/controller/delete-from-rentals.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getRental_Success() throws Exception {
        //given
        RentalDto rentalDto = getRentalDto();
        Long id = rentalDto.getId();

        //when
        MvcResult result = mockMvc.perform(
                        get("/rentals/" + id)
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andReturn();
        //then
        RentalDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                RentalDto.class);

        EqualsBuilder.reflectionEquals(rentalDto, actual, "id");
    }

    @Test
    @WithUserDetails(value = "admin@simpleart.eu")
    @Sql(scripts = {"classpath:db/controller/add-to-rentals.sql",
            "classpath:db/controller/add-to-cars.sql",
            "classpath:db/controller/add-to-users.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:db/controller/delete-from-rentals.sql",
            "classpath:db/controller/delete-from-cars.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Test returning a rental successfully")
    void returnRental_Success() throws Exception {
        long id = ID;

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/rentals/" + id + "/return")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

    }

    @Test
    @WithMockUser(username = "user", authorities = {"MANAGER"})
    @DisplayName("Test getting specific rental with invalid ID")
    void getRental_InvalidId() throws Exception {
        Long invalidId = 999L;

        mockMvc.perform(
                        get("/rentals/" + invalidId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @WithUserDetails(value = "admin@simpleart.eu")
    @DisplayName("Test returning a rental with invalid ID")
    void returnRental_InvalidId() throws Exception {
        long invalidId = 999L;

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/rentals/" + invalidId + "/return")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());
    }

    private User getUser() {
        User user = new User();
        user.setId(ID);
        user.setEmail(EMAIL);
        user.setFirstName(FIRST_NAME);
        user.setLastName(LAST_NAME);
        user.setPassword(PASSWORD);
        user.setRole(ROLE);
        user.setDeleted(false);
        return user;
    }

    private CreateRentalRequestDto getCreateRentalRequestDto() {
        return new CreateRentalRequestDto(
                RENTAL_DATE, RETURN_DATE, ID
        );
    }

    private Rental getRental() {
        Rental rental = new Rental();
        rental.setId(ID);
        rental.setRentalDate(RENTAL_DATE);
        rental.setReturnDate(RETURN_DATE);
        rental.setActualReturnDate(ACTUAL_RETURN_DATE);
        rental.setCarId(ID);
        rental.setUserId(ID);
        rental.setDeleted(false);
        return rental;
    }

    private RentalDto getRentalDto() {
        RentalDto rentalDto = new RentalDto();
        rentalDto.setId(ID);
        rentalDto.setRentalDate(RENTAL_DATE);
        rentalDto.setActualReturnDate(ACTUAL_RETURN_DATE);
        rentalDto.setReturnDate(RETURN_DATE);
        rentalDto.setCarId(ID);
        return rentalDto;
    }

    private RentalSearchParameters getRentalSearchParameters() {
        RentalSearchParameters parameters = new RentalSearchParameters(USER_ID, IS_ACTIVE);
        return parameters;
    }
}

