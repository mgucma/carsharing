package com.marek.carsharing.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marek.carsharing.dto.user.UpdateUserRequestDto;
import com.marek.carsharing.dto.user.UserDto;
import com.marek.carsharing.model.classes.User;
import com.marek.carsharing.model.enums.Role;
import java.sql.Connection;
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
class UsersControllerTest {
    public static final long ID = 1L;
    public static final String EMAIL = "admin@simpleart.eu";
    public static final String FIRST_NAME = "Admin";
    public static final String LAST_NAME = "User";
    public static final String PASSWORD = "password";
    public static final Role ROLE = Role.MANAGER;
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
                    new ClassPathResource("db/controller/delete-from-users.sql")
            );
        }
    }

    @Test
    @WithMockUser(username = "user", authorities = {"MANAGER"})
    @DisplayName("Test updating user role successfully - MANAGER only")
    @Sql(scripts = {"classpath:db/controller/delete-from-users.sql",
            "classpath:db/controller/add-to-users.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:db/controller/delete-from-users.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateUserRole_Success() throws Exception {
        //given
        UserDto userDto = getUserDto();
        Long id = userDto.getId();

        //when
        MvcResult result = mockMvc.perform(
                        put("/users/" + id + "/role")
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andReturn();

        //then
        UserDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                UserDto.class);

        EqualsBuilder.reflectionEquals(userDto, actual, "id");
    }

    @Test
    @WithUserDetails(value = "admin@simpleart.eu")
    @Sql(scripts = {"classpath:db/controller/delete-from-users.sql",
            "classpath:db/controller/add-to-users.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:db/controller/delete-from-users.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Get the profile of the currently authenticated user successfully")
    void getMyProfile_Success() throws Exception {
        UserDto userDto = getUserDto();

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/users/me")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        UserDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                UserDto.class);

        EqualsBuilder.reflectionEquals(userDto, actual, "id");
    }

    @Test
    @WithUserDetails(value = "admin@simpleart.eu")
    @Sql(scripts = {"classpath:db/controller/add-to-users.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:db/controller/delete-from-users.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Update own profile successfully")
    void updateMyProfile_Success() throws Exception {
        UpdateUserRequestDto updateUserRequestDto = getUpdateUserRequestDto();
        UserDto userDto = getUserDto();

        String json = objectMapper.writeValueAsString(updateUserRequestDto);

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.put("/users/me")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        UserDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                UserDto.class);

        EqualsBuilder.reflectionEquals(userDto, actual, "id");

    }

    @Test
    @WithMockUser(username = "user", authorities = {"USER"})
    @DisplayName("Test updating user role without MANAGER authority")
    @Sql(scripts = {"classpath:db/controller/delete-from-users.sql",
            "classpath:db/controller/add-to-users.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:db/controller/delete-from-users.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateUserRole_Fail_Unauthorized() throws Exception {
        Long id = ID;

        mockMvc.perform(
                put("/users/" + id + "/role")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = "admin@simpleart.eu")
    @DisplayName("Test updating own profile with invalid data")
    @Sql(scripts = {"classpath:db/controller/delete-from-users.sql",
            "classpath:db/controller/add-to-users.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:db/controller/delete-from-users.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateMyProfile_Fail_InvalidData() throws Exception {
        UpdateUserRequestDto invalidRequestDto = new UpdateUserRequestDto("", "");

        String json = objectMapper.writeValueAsString(invalidRequestDto);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/users/me")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
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

    private UserDto getUserDto() {
        UserDto userDto = new UserDto();
        userDto.setId(ID);
        userDto.setEmail(EMAIL);
        userDto.setFirstName(FIRST_NAME);
        userDto.setLastName(LAST_NAME);
        userDto.setRole(String.valueOf(ROLE));
        return userDto;
    }

    private UpdateUserRequestDto getUpdateUserRequestDto() {
        return new UpdateUserRequestDto(
                FIRST_NAME, LAST_NAME
        );
    }
}
