package com.marek.carsharing.config;

import org.testcontainers.containers.PostgreSQLContainer;

public class CustomPostgresContainer extends PostgreSQLContainer<CustomPostgresContainer> {
    public static final String DB_IMAGE = "postgres:15.3";

    private static CustomPostgresContainer postgresContainer;

    public CustomPostgresContainer() {
        super(DB_IMAGE);
    }

    public static synchronized CustomPostgresContainer getInstance() {
        if (postgresContainer == null) {
            postgresContainer = new CustomPostgresContainer();
        }
        return postgresContainer;
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("TEST_DB_URL", postgresContainer.getJdbcUrl());
        System.setProperty("TEST_DB_USERNAME", postgresContainer.getUsername());
        System.setProperty("TEST_DB_PASSWORD", postgresContainer.getPassword());
    }

    @Override
    public void stop() {
    }
}
