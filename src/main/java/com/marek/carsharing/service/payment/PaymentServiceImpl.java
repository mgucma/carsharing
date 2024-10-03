package com.marek.carsharing.service.payment;

import com.marek.carsharing.dto.payment.CreatePaymentRequestDto;
import com.marek.carsharing.dto.payment.PaymentDto;
import com.marek.carsharing.dto.payment.PaymentSearchParameters;
import com.marek.carsharing.exception.PaymentException;
import com.marek.carsharing.mapper.PaymentMapper;
import com.marek.carsharing.model.classes.Car;
import com.marek.carsharing.model.classes.Payment;
import com.marek.carsharing.model.classes.Rental;
import com.marek.carsharing.model.classes.User;
import com.marek.carsharing.model.enums.PaymentType;
import com.marek.carsharing.model.enums.Status;
import com.marek.carsharing.repository.car.CarRepository;
import com.marek.carsharing.repository.payment.PaymentRepository;
import com.marek.carsharing.repository.payment.provider.PaymentSpecificationBuilder;
import com.marek.carsharing.repository.rental.RentalRepository;
import com.marek.carsharing.service.notification.NotificationService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private static final String paymentDomain = "/payments/";
    private static final String COMPLETE = "complete";
    private static final String OPEN = "open";
    private static final String USD = "usd";

    @Value("${domain}")
    private String domain;

    private final RentalRepository rentalRepository;
    private final CarRepository carRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final NotificationService notificationService;
    private final PaymentSpecificationBuilder paymentSpecificationBuilder;

    @Override
    @Transactional
    public PaymentDto createPayment(User user, CreatePaymentRequestDto requestDto) {
        Rental rental = rentalRepository.findById(requestDto.rentalId()).orElseThrow(
                () -> new EntityNotFoundException(
                        "Rental with id " + requestDto.rentalId() + " not found")
        );
        BigDecimal total = getAmountToPay(rental);

        try {
            Session session = createStripeSession(total, rental);
            Payment payment = getPayment(Status.PENDING,
                    PaymentType.PAYMENT,
                    rental,
                    total,
                    session);

            return paymentMapper.toDto(
                    paymentRepository.save(payment));

        } catch (StripeException e) {
            throw new PaymentException("cannot create payment", e);
        }
    }

    private Session createStripeSession(BigDecimal total, Rental rental) throws StripeException {
        SessionCreateParams rentInUsd = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(domain + paymentDomain + "success/{CHECKOUT_SESSION_ID}")
                .setCancelUrl(domain + paymentDomain + "cancel/{CHECKOUT_SESSION_ID}")
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(USD)
                                .setUnitAmount(total.multiply(BigDecimal.valueOf(100)).longValue())
                                .setProductData(
                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                .setName("Payment for: " + rental.getId())
                                                .build())
                                .build())
                        .setQuantity(1L)
                        .build())
                .setClientReferenceId(rental.getId().toString())
                .build();
        return Session.create(rentInUsd);
    }

    private BigDecimal getAmountToPay(Rental rental) {
        Car car = carRepository.findById(rental.getCarId()).orElseThrow(
                () -> new EntityNotFoundException("Car with id " + rental.getCarId() + " not found")
        );
        BigDecimal dailyFee = car.getDailyFee();
        long start = rental.getRentalDate().toEpochDay();
        long end = rental.getReturnDate().toEpochDay();
        long daysAmount = end - start;
        return dailyFee.multiply(BigDecimal.valueOf(daysAmount));
    }

    private Payment getPayment(Status pending,
                               PaymentType payment,
                               Rental rental,
                               BigDecimal total,
                               Session session) {
        Payment paymentEntity = new Payment();
        paymentEntity.setStatus(pending);
        paymentEntity.setType(payment);
        paymentEntity.setRentalId(rental.getId());
        paymentEntity.setSessionUrl(session.getUrl());
        paymentEntity.setSessionId(session.getId());
        paymentEntity.setAmountToPay(total);
        paymentEntity.setDeleted(false);
        return paymentEntity;
    }

    @Override
    public String checkPaymentSuccess(String sessionId) {
        try {
            Session session = Session.retrieve(sessionId);
            if (COMPLETE.equals(session.getStatus())) {
                Payment payment = paymentRepository.findBySessionIdAndIsDeletedFalse(sessionId)
                        .orElseThrow(() ->
                                new PaymentException("Cannot find payment with session id "
                                + sessionId));
                payment.setStatus(Status.PAID);
                paymentRepository.save(payment);
                String message = "Payment successful! Thank you for your payment.";
                notificationService.notifySuccessfulPayments(message);
                return message;
            } else {
                return "Payment is not completed yet. Please try again later. In Success";
            }
        } catch (StripeException e) {
            throw new PaymentException("cannot retrieve payment success", e);
        }
    }

    @Override
    public String pausePayment(String sessionId) {
        try {
            Session session = Session.retrieve(sessionId);
            if (OPEN.equals(session.getStatus())) {
                Payment payment = paymentRepository.findBySessionIdAndIsDeletedFalse(sessionId)
                        .orElseThrow(() ->
                                new PaymentException("Cannot find payment with session id "
                                + sessionId));
                payment.setStatus(Status.PAUSED);
                paymentRepository.save(payment);
                return "Payment paused. You can resume your payment later.";
            } else if (COMPLETE.equals(session.getStatus())) {
                return "Payment has been paid. Thank you for your payment.";
            } else {
                return "Payment is not completed yet. Please try again later. In Pause";
            }
        } catch (StripeException e) {
            throw new PaymentException("cannot retrieve payment success", e);
        }
    }

    @Override
    public List<PaymentDto> getPayments(PaymentSearchParameters searchParameters) {
        Specification<Payment> build = paymentSpecificationBuilder.build(searchParameters);
        List<Payment> all = paymentRepository.findAll(build);

        if (all.isEmpty()) {
            throw new EntityNotFoundException("No payments found for the given search parameters");
        }

        return all.stream()
                .map(paymentMapper::toDto)
                .toList();
    }
}
