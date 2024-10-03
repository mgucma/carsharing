package com.marek.carsharing.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marek.carsharing.dto.payment.CreatePaymentRequestDto;
import com.marek.carsharing.dto.payment.PaymentDto;
import com.marek.carsharing.dto.payment.PaymentSearchParameters;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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
class PaymentsControllerTest {
    public static final String[] USERS_ID = {"1"};
    public static final long ID = 1L;
    public static final String SESSION_URL = "asfasnfkasf";
    public static final String SESSION_ID = "123124124";
    public static final BigDecimal AMOUNT_TO_PAY = BigDecimal.valueOf(100);
    private static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(@Autowired WebApplicationContext applicationContext) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @AfterAll
    static void afterAll(@Autowired DataSource dataSource) {
        teardown(dataSource);
    }

    @SneakyThrows
    private static void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("db/controller/delete-from-payments.sql")
            );
        }
    }

    @Test
    @WithMockUser(username = "user", authorities = {"MANAGER"})
    @DisplayName("Test for successful retrieval of payments")
    @Sql(scripts = "classpath:db/controller/add-to-payments.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/controller/delete-from-payments.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @SneakyThrows
    void getPayments_Success() {
        // Given
        PaymentSearchParameters paymentSearchParameters = getPaymentSearchParameters();
        PaymentDto paymentDto = getPaymentDto();

        String json = objectMapper.writeValueAsString(paymentSearchParameters);
        // When
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/payments")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        // Then
        List<PaymentDto> list = Arrays.asList(
                objectMapper.readValue(result.getResponse().getContentAsString(),
                        PaymentDto[].class)
        );
        EqualsBuilder.reflectionEquals(paymentDto, list.get(0), "id");
    }

    @Test
    @WithUserDetails(value = "admin@simpleart.eu")
    @DisplayName("Test for successful creation of payment session")
    @Sql(scripts = {"classpath:db/controller/add-to-rentals.sql",
            "classpath:db/controller/add-to-cars.sql",
            "classpath:db/controller/delete-from-users.sql",
            "classpath:db/controller/add-to-users.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:db/controller/delete-from-rentals.sql",
            "classpath:db/controller/delete-from-cars.sql",
            "classpath:db/controller/delete-from-users.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @SneakyThrows
    void createPaymentSession_Success() {
        // Given
        CreatePaymentRequestDto createPaymentRequestDto = getCreatePaymentRequestDto();
        PaymentDto paymentDto = getPaymentDto();

        String json = objectMapper.writeValueAsString(createPaymentRequestDto);
        // When
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.post("/payments")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andReturn();
        // Then
        PaymentDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                PaymentDto.class);
        EqualsBuilder.reflectionEquals(paymentDto, actual, "id");
    }

    @Test
    @WithMockUser(username = "user", authorities = {"MANAGER"})
    @DisplayName("Test for invalid search parameters")
    @SneakyThrows
    void getPayments_InvalidSearchParameters() {
        PaymentSearchParameters invalidSearchParameters =
                new PaymentSearchParameters(null);

        String json = objectMapper.writeValueAsString(invalidSearchParameters);
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/payments")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user", authorities = {"USER"})
    @DisplayName("Test for unauthorized access to create payment session")
    @SneakyThrows
    void createPaymentSession_Unauthorized() {
        CreatePaymentRequestDto createPaymentRequestDto = getCreatePaymentRequestDto();
        String json = objectMapper.writeValueAsString(createPaymentRequestDto);

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/payments")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user", authorities = {"MANAGER"})
    @DisplayName("Test for retrieving non-existent payments")
    @SneakyThrows
    void getPayments_NonExistentResource() {
        PaymentSearchParameters paymentSearchParameters =
                new PaymentSearchParameters(new String[]{"9999"}); // Non-existent user ID

        String json = objectMapper.writeValueAsString(paymentSearchParameters);

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/payments")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());
    }

    private PaymentDto getPaymentDto() {
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setId(ID);
        paymentDto.setRentalId(ID);
        paymentDto.setSessionUrl(SESSION_URL);
        paymentDto.setSessionId(SESSION_ID);
        paymentDto.setAmountToPay(AMOUNT_TO_PAY);
        return paymentDto;
    }

    private PaymentSearchParameters getPaymentSearchParameters() {
        PaymentSearchParameters paymentSearchParameters = new PaymentSearchParameters(
                USERS_ID
        );
        return paymentSearchParameters;
    }

    private CreatePaymentRequestDto getCreatePaymentRequestDto() {
        CreatePaymentRequestDto createPaymentRequestDto =
                new CreatePaymentRequestDto(
                        ID
                );
        return createPaymentRequestDto;
    }
}
