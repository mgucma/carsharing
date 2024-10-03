package com.marek.carsharing.service.payment;

import com.marek.carsharing.dto.payment.CreatePaymentRequestDto;
import com.marek.carsharing.dto.payment.PaymentDto;
import com.marek.carsharing.dto.payment.PaymentSearchParameters;
import com.marek.carsharing.model.classes.User;
import java.util.List;

public interface PaymentService {
    PaymentDto createPayment(User user, CreatePaymentRequestDto requestDto);

    String checkPaymentSuccess(String sessionId);

    String pausePayment(String sessionId);

    List<PaymentDto> getPayments(PaymentSearchParameters searchParameters);
}

