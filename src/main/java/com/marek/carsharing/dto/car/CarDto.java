package com.marek.carsharing.dto.car;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class CarDto {
    private Long id;
    private String model;
    private String brand;
    private int inventory;
    private BigDecimal dailyFee;
}

