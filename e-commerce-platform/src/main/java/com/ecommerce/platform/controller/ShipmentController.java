package com.ecommerce.platform.controller;

import com.ecommerce.platform.domain.Shipment;
import com.ecommerce.platform.dto.ShipmentRequest;
import com.ecommerce.platform.service.ShipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping
    public ResponseEntity<Shipment> createShipment(@Valid @RequestBody ShipmentRequest request) {
        Shipment shipment = shipmentService.createShipment(
                request.getUserId(),
                request.getOrderId(),
                request.getShippingAddress()
        );
        return ResponseEntity.ok(shipment);
    }

    @PostMapping("/{shipmentId}/ship")
    public ResponseEntity<Shipment> shipPackage(@PathVariable Long shipmentId) {
        Shipment shipment = shipmentService.shipPackage(shipmentId);
        return ResponseEntity.ok(shipment);
    }

    @PostMapping("/{shipmentId}/deliver")
    public ResponseEntity<Shipment> deliverPackage(@PathVariable Long shipmentId) {
        Shipment shipment = shipmentService.deliverPackage(shipmentId);
        return ResponseEntity.ok(shipment);
    }

    @GetMapping("/{shipmentId}")
    public ResponseEntity<Shipment> getShipment(@PathVariable Long shipmentId) {
        Shipment shipment = shipmentService.getShipment(shipmentId);
        return ResponseEntity.ok(shipment);
    }
}
