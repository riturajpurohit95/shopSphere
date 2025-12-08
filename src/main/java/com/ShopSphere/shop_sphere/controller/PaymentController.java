package com.ShopSphere.shop_sphere.controller;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ShopSphere.shop_sphere.dto.ConfirmUpiRequest;
import com.ShopSphere.shop_sphere.dto.PaymentDto;
import com.ShopSphere.shop_sphere.service.PaymentService;

import jakarta.validation.Valid;

@CrossOrigin(origins="[http://localhost:3000](http://localhost:3000)")
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

private final PaymentService paymentService;

public PaymentController(PaymentService paymentService) {
    this.paymentService = paymentService;
}


@PostMapping
public ResponseEntity<PaymentDto> createPayment(@Valid @RequestBody PaymentDto dto) {
    PaymentDto created = paymentService.createPayment(dto);
    return ResponseEntity.created(URI.create("/api/payments/" + created.getPayment_id()))
                         .body(created);
}

@PostMapping("/confirm-upi/{orderId}")
public ResponseEntity<PaymentDto> confirmUpiPaymentOrder(
        @PathVariable int orderId,
        @Valid @RequestBody ConfirmUpiRequest request) {

    // Mock UPI confirmation (90% success, 10% fail)
    PaymentDto updated = paymentService.confirmUpiPaymentOrder(
            orderId,
            null,  // no RazorpayPaymentId needed
            null,  // no signature needed
            request.getUpiVpa()
    );

    return ResponseEntity.ok(updated);
}

@GetMapping("/{id}")
public ResponseEntity<PaymentDto> getPaymentById(@PathVariable("id") int paymentId) {
    return ResponseEntity.ok(paymentService.getPaymentById(paymentId));
}

@GetMapping("/order/{orderId}")
public ResponseEntity<PaymentDto> getPaymentByOrderId(@PathVariable int orderId) {
    return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
}

//@PutMapping("/{id}/status")
//public ResponseEntity<PaymentDto> updatePaymentStatus(
//        @PathVariable("id") int paymentId,
//        @RequestParam("status") String status) {
//
//    return ResponseEntity.ok(paymentService.updatePaymentStatus(paymentId, status));
//}

@PutMapping("/{id}/payment-status")
public ResponseEntity<PaymentDto> updatePaymentStatus(
        @PathVariable("id") int orderId,
        @RequestParam("status") String status
) {
    PaymentDto updatedPayment = paymentService.updatePaymentStatusByOrderId(orderId, status);
    return ResponseEntity.ok(updatedPayment);
}


@PutMapping("/{id}/gateway")
public ResponseEntity<PaymentDto> updateGatewayDetails(
        @PathVariable("id") int paymentId,
        @RequestParam("gatewayRef") String gatewayRef,
        @RequestParam("upiVpa") String upiVpa,
        @RequestParam(value = "responsePayload", required = false) String responsePayload) {

    return ResponseEntity.ok(
            paymentService.updateGatewayDetails(paymentId, gatewayRef, upiVpa, responsePayload)
    );
}

@GetMapping("/user/{userId}")
public ResponseEntity<?> getPaymentsByUserId(@PathVariable int userId) {
    List<Map<String, Object>> payments = paymentService.getPaymentDetails(userId);

    if (payments.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body("No payment records found for userId: " + userId);
    }

    return ResponseEntity.ok(payments);
}

}
