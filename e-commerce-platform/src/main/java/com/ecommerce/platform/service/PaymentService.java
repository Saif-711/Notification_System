package com.ecommerce.platform.service;

import com.ecommerce.platform.client.NotificationClient;
import com.ecommerce.platform.domain.Payment;
import com.ecommerce.platform.dto.NotificationChannel;
import com.ecommerce.platform.model.PaymentStatus;
import com.ecommerce.platform.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    
    private final PaymentRepository paymentRepository;
    private final NotificationClient notificationClient;

    @Transactional
    public Payment createPayment(Long userId, Long orderId, BigDecimal amount, String paymentMethod) {
        Payment payment = new Payment(userId, orderId, amount, paymentMethod);
        payment = paymentRepository.save(payment);
        
        log.info("Payment created for order {}: amount={}, method={}", orderId, amount, paymentMethod);
        
        // Simulate payment processing
        processPayment(payment.getId());
        
        return payment;
    }

    @Transactional
    public Payment processPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
        
        // Simulate payment success (in real app, this would call payment gateway)
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setCompletedAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);
        
        log.info("Payment completed: id={}, amount={}", paymentId, payment.getAmount());
        
        // Send payment success notification
        String message = String.format("Payment of $%.2f received successfully for order #%d. Thank you!", 
                payment.getAmount(), payment.getOrderId());
        notificationClient.sendNotification(payment.getUserId(), NotificationChannel.SMS, message);
        
        return payment;
    }

    public Payment getPayment(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
    }
}
