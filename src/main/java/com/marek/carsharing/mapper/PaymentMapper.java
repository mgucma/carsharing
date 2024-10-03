package com.marek.carsharing.mapper;

import com.marek.carsharing.config.MapperConfig;
import com.marek.carsharing.dto.payment.CreatePaymentRequestDto;
import com.marek.carsharing.dto.payment.PaymentDto;
import com.marek.carsharing.model.classes.Payment;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface PaymentMapper {

    PaymentDto toDto(Payment payment);

    Payment toEntity(CreatePaymentRequestDto paymentDto);
}
