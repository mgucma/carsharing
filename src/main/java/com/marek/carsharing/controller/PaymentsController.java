package com.marek.carsharing.controller;

import com.marek.carsharing.dto.payment.CreatePaymentRequestDto;
import com.marek.carsharing.dto.payment.PaymentDto;
import com.marek.carsharing.dto.payment.PaymentSearchParameters;
import com.marek.carsharing.model.classes.User;
import com.marek.carsharing.service.payment.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentsController {
    private final PaymentService paymentService;

    @Operation(summary = "Get payments by user ID - MANAGER only ")
    @GetMapping
    @PreAuthorize("hasAnyAuthority('MANAGER')")
    @ResponseStatus(HttpStatus.OK)
    public List<PaymentDto> getPayments(PaymentSearchParameters searchParameters) {
        return paymentService.getPayments(searchParameters);
    }

    @Operation(summary = "Create payment session")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('MANAGER', 'CUSTOMER')")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentDto createPaymentSession(@RequestBody @Valid CreatePaymentRequestDto requestDto,
                                           Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return paymentService.createPayment(user, requestDto);
    }

    @Operation(summary = "Check successful Stripe payments")
    @GetMapping("/success/{sessionId}")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'CUSTOMER')")
    @ResponseStatus(HttpStatus.OK)
    public String checkPaymentSuccess(@PathVariable String sessionId) {
        return paymentService.checkPaymentSuccess(sessionId);
    }

    @Operation(summary = "Return payment paused message")
    @GetMapping("/cancel/{sessionId}")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'CUSTOMER')")
    @ResponseStatus(HttpStatus.OK)
    public String paymentPaused(@PathVariable String sessionId) {
        return paymentService.pausePayment(sessionId);
    }
}

