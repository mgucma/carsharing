package com.marek.carsharing.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marek.carsharing.dto.car.CarDetailsDto;
import com.marek.carsharing.dto.car.CarDto;
import com.marek.carsharing.dto.car.CarRequestDto;
import com.marek.carsharing.model.classes.Car;
import com.marek.carsharing.model.enums.Type;
import java.math.BigDecimal;
import java.sql.Connection;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CarsControllerTest {
    public static final String MODEL = "model";
    public static final String BRAND = "brand";
    public static final String UNIVERSAL = "UNIVERSAL";
    public static final int INVENTORY = 10;
    public static final BigDecimal DAILY_FEE = BigDecimal.valueOf(10);
    public static final long ID = 1L;
    public static final Type UNIVERSAL_TYPE = Type.UNIVERSAL;
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
                    new ClassPathResource("db/controller/delete-from-cars.sql")
            );
        }
    }

    @Test
    @WithMockUser(username = "MANAGER", authorities = {"MANAGER"})
    @DisplayName("Test adding a new car successfully - MANAGER only")
    void addCar_Success() throws Exception {
        //given
        CarRequestDto carRequestDto = getCarRequestDto();
        CarDto carDto = getCarDto();

        String jsonRequest = objectMapper.writeValueAsString(
                carRequestDto
        );
        //when
        MvcResult result = mockMvc.perform(
                        post("/cars")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isCreated())
                .andReturn();
        //then

        CarDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                CarDto.class);

        EqualsBuilder.reflectionEquals(carDto, actual, "id");
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"MANAGER"})
    @DisplayName("Test retrieving a list of cars successfully - MANAGER & CUSTOMER")
    @Sql(scripts = "/db/controller/add-to-cars.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/db/controller/delete-from-cars.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getCars_Success() throws Exception {
        //given
        Pageable pageable = PageRequest.of(0, 10);
        CarDto carDto = getCarDto();

        String jsonRequest = objectMapper.writeValueAsString(
                pageable
        );
        //when
        MvcResult result = mockMvc.perform(
                        get("/cars")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andReturn();
        //then
        List<CarDto> actual = Arrays.asList(
                objectMapper.readValue(result.getResponse().getContentAsString(),
                        CarDto[].class));

        EqualsBuilder.reflectionEquals(carDto, actual.get(0), "id");
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"MANAGER"})
    @DisplayName("Test retrieving detailed information of a car successfully - MANAGER & CUSTOMER")
    @Sql(scripts = "/db/controller/add-to-cars.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/db/controller/delete-from-cars.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getCarDetails_Success() throws Exception {
        //given
        Long id = ID;
        CarDetailsDto carDetailsDto = getCarDetailsDto();

        String jsonRequest = objectMapper.writeValueAsString(
                id
        );
        //when
        MvcResult result = mockMvc.perform(
                        get("/cars/" + id)
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andReturn();
        //then
        CarDetailsDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                CarDetailsDto.class);

        EqualsBuilder.reflectionEquals(carDetailsDto, actual, "id");
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"MANAGER"})
    @DisplayName("Test updating a car successfully - MANAGER only")
    @Sql(scripts = "classpath:db/controller/add-to-cars.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/controller/delete-from-cars.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateCar_Success() throws Exception {
        //given
        Long id = ID;
        CarDto carDto = getCarDto();
        CarRequestDto carRequestDto = getCarRequestDto();

        String jsonRequest = objectMapper.writeValueAsString(
                carRequestDto
        );
        //when
        MvcResult result = mockMvc.perform(
                        put("/cars/" + id)
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andReturn();
        //then

        CarDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                CarDto.class);

        EqualsBuilder.reflectionEquals(carDto, actual, "id");
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"MANAGER"})
    @DisplayName("Test deleting a car successfully - MANAGER only")
    void deleteCar_Success() throws Exception {
        Car car = getCar();
        Long id = car.getId();

        mockMvc.perform(
                        delete("/cars/" + id)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "MANAGER", authorities = {"MANAGER"})
    @DisplayName("Test adding a car with invalid data - MANAGER only")
    void addCar_InvalidData() throws Exception {
        CarRequestDto carRequestDto =
                new CarRequestDto(null, null, null, -1, BigDecimal.ZERO); // Invalid data

        String jsonRequest = objectMapper.writeValueAsString(carRequestDto);

        mockMvc.perform(
                        post("/cars")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"MANAGER"})
    @DisplayName("Test retrieving details of a non-existent car")
    void getCarDetails_NonExistentCar() throws Exception {
        Long nonExistentId = 999L;

        mockMvc.perform(
                        get("/cars/" + nonExistentId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"MANAGER"})
    @DisplayName("Test updating a car with invalid data - MANAGER only")
    void updateCar_InvalidData() throws Exception {
        Long id = ID;
        CarRequestDto carRequestDto =
                new CarRequestDto(null, null, null, -1, BigDecimal.ZERO); // Invalid data

        String jsonRequest = objectMapper.writeValueAsString(carRequestDto);

        mockMvc.perform(
                        put("/cars/" + id)
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    private CarRequestDto getCarRequestDto() {
        return new CarRequestDto(
                MODEL, BRAND, UNIVERSAL, INVENTORY, DAILY_FEE
        );
    }

    private Car getCar() {
        Car car = new Car();
        car.setId(ID);
        car.setModel(MODEL);
        car.setBrand(BRAND);
        car.setType(UNIVERSAL_TYPE);
        car.setInventory(INVENTORY);
        car.setDailyFee(DAILY_FEE);
        car.setDeleted(false);
        return car;
    }

    private CarDto getCarDto() {
        CarDto carDto = new CarDto();
        carDto.setId(ID);
        carDto.setModel(MODEL);
        carDto.setBrand(BRAND);
        carDto.setInventory(INVENTORY);
        carDto.setDailyFee(DAILY_FEE);
        return carDto;
    }

    private CarDetailsDto getCarDetailsDto() {
        CarDetailsDto carDetailsDto = new CarDetailsDto();
        carDetailsDto.setId(ID);
        carDetailsDto.setModel(MODEL);
        carDetailsDto.setBrand(BRAND);
        carDetailsDto.setType(UNIVERSAL);
        carDetailsDto.setInventory(INVENTORY);
        carDetailsDto.setDailyFee(DAILY_FEE);
        return carDetailsDto;
    }
}

