package com.ecommerce.platform.service;

import com.ecommerce.platform.client.NotificationClient;
import com.ecommerce.platform.domain.Shipment;
import com.ecommerce.platform.dto.NotificationChannel;
import com.ecommerce.platform.model.ShipmentStatus;
import com.ecommerce.platform.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShipmentService {

    private static final Logger log = LoggerFactory.getLogger(ShipmentService.class);
    
    private final ShipmentRepository shipmentRepository;
    private final NotificationClient notificationClient;

    @Transactional
    public Shipment createShipment(Long userId, Long orderId, String shippingAddress) {
        String trackingNumber = "TRK-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
        
        Shipment shipment = new Shipment(userId, orderId, trackingNumber, shippingAddress);
        shipment = shipmentRepository.save(shipment);
        
        log.info("Shipment created for order {}: tracking={}", orderId, trackingNumber);
        
        // Send shipment notification
        String message = String.format("Your package is being prepared! Tracking number: %s. Address: %s", 
                trackingNumber, shippingAddress);
        notificationClient.sendNotification(userId, NotificationChannel.EMAIL, message);
        
        return shipment;
    }

    @Transactional
    public Shipment shipPackage(Long shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new RuntimeException("Shipment not found: " + shipmentId));
        
        shipment.setStatus(ShipmentStatus.SHIPPED);
        shipment.setShippedAt(LocalDateTime.now());
        shipment = shipmentRepository.save(shipment);
        
        log.info("Package shipped: tracking={}", shipment.getTrackingNumber());
        
        // Send shipped notification
        String message = String.format("Your package has been shipped! Tracking: %s. Expected delivery in 2-3 business days.", 
                shipment.getTrackingNumber());
        notificationClient.sendNotification(shipment.getUserId(), NotificationChannel.SMS, message);
        
        return shipment;
    }

    @Transactional
    public Shipment deliverPackage(Long shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new RuntimeException("Shipment not found: " + shipmentId));
        
        shipment.setStatus(ShipmentStatus.DELIVERED);
        shipment.setDeliveredAt(LocalDateTime.now());
        shipment = shipmentRepository.save(shipment);
        
        log.info("Package delivered: tracking={}", shipment.getTrackingNumber());
        
        // Send delivery notification
        String message = String.format("Your package has been delivered! Tracking: %s. Enjoy your purchase!", 
                shipment.getTrackingNumber());
        notificationClient.sendNotification(shipment.getUserId(), NotificationChannel.EMAIL, message);
        
        return shipment;
    }

    public Shipment getShipment(Long shipmentId) {
        return shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new RuntimeException("Shipment not found: " + shipmentId));
    }
}
