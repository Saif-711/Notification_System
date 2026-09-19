package com.ecommerce.platform.domain;

import com.ecommerce.platform.model.ShipmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "shipments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private String trackingNumber;

    @Column(nullable = false)
    private String shippingAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime shippedAt;

    @Column
    private LocalDateTime deliveredAt;

    public Shipment(Long userId, Long orderId, String trackingNumber, String shippingAddress) {
        this.userId = userId;
        this.orderId = orderId;
        this.trackingNumber = trackingNumber;
        this.shippingAddress = shippingAddress;
        this.status = ShipmentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }
}
