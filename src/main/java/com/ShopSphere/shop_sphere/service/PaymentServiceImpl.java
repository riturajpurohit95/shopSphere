//package com.ShopSphere.shop_sphere.service;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//import org.springframework.stereotype.Service;
//
//import com.ShopSphere.shop_sphere.dto.PaymentDto;
//import com.ShopSphere.shop_sphere.exception.PaymentAlreadyCompletedException;
//import com.ShopSphere.shop_sphere.exception.ResourceNotFoundException;
//import com.ShopSphere.shop_sphere.model.Order;
//import com.ShopSphere.shop_sphere.model.Payment;
//import com.ShopSphere.shop_sphere.repository.PaymentDao;
//
//@Service
//public class PaymentServiceImpl implements PaymentService {
//
//    private final PaymentDao paymentDao;
//    private final RazorpayGatewayService razorpayGatewayService;
//
//    public PaymentServiceImpl(PaymentDao paymentDao, RazorpayGatewayService razorpayGatewayService) {
//        this.paymentDao = paymentDao;
//        this.razorpayGatewayService = razorpayGatewayService;
//    }
//
//    private Payment dtoToModel(PaymentDto dto) {
//        Payment payment = new Payment();
//        //payment.setPaymentId(dto.getPayment_id());
//        payment.setOrderId(dto.getOrderId());
//        payment.setUserId(dto.getUserId());
//        payment.setAmount(dto.getAmount());
//        payment.setCurrency(dto.getCurrency());
//        payment.setPaymentMethod(dto.getPaymentMethod());
//        payment.setCreatedAt(dto.getCreatedAt());
//        payment.setStatus(dto.getStatus());
//        payment.setGatewayRef(dto.getGatewayRef());
//        payment.setUpiVpa(dto.getUpiVpa());
//        payment.setResponsePayload(dto.getResponsePayLoad());
//        return payment;
//    }
//
//    private PaymentDto modelToDto(Payment payment) {
//        PaymentDto dto = new PaymentDto();
//        dto.setPayment_id(payment.getPaymentId());
//        dto.setOrderId(payment.getOrderId());
//        dto.setUserId(payment.getUserId());
//        dto.setAmount(payment.getAmount());
//        dto.setCurrency(payment.getCurrency());
//        dto.setPaymentMethod(payment.getPaymentMethod());
//        dto.setStatus(payment.getStatus());
//        dto.setGatewayRef(payment.getGatewayRef());
//        dto.setUpiVpa(payment.getUpiVpa());
//        dto.setResponsePayLoad(payment.getResponsePayload());
//        return dto;
//    }
//
//    @Override
//    public PaymentDto createPayment(PaymentDto dto) {
//        if (dto == null) {
//            throw new IllegalArgumentException("PaymentDto must not be null");
//        }
//
//        Payment payment = dtoToModel(dto);
//        payment.setCreatedAt(LocalDateTime.now());
//
//        if (payment.getStatus() == null || payment.getStatus().isEmpty()) {
//            payment.setStatus("PENDING");
//        }
//
//        int rows = paymentDao.save(payment);
//        if (rows <= 0) {
//            throw new RuntimeException("Create failed for payment of orderId: " + payment.getOrderId());
//        }
//
//        // create Razorpay order only for UPI
//        if ("UPI".equalsIgnoreCase(payment.getPaymentMethod())) {
//            BigDecimal amountInPaise = payment.getAmount().multiply(BigDecimal.valueOf(100));
//            String receipt = "Order_" + payment.getOrderId();
//
//            com.razorpay.Order razorpayOrder;
//            try {
//                razorpayOrder = razorpayGatewayService.createUpiOrder(receipt, amountInPaise, payment.getCurrency());
//            } catch (Exception e) {
//                throw new RuntimeException("Failed to create Razorpay UPI order", e);
//            }
//
//            String razorpayOrderId = razorpayOrder.get("id").toString();
//            payment.setGatewayRef(razorpayOrderId);
//            payment.setResponsePayload(razorpayOrder.toString());
//
//            int gwRows = paymentDao.updateGatewayDetails(
//                    payment.getPaymentId(),
//                    payment.getGatewayRef(),
//                    payment.getUpiVpa(),
//                    payment.getResponsePayload());
//
//            if (gwRows <= 0) {
//                throw new RuntimeException("Failed to update gateway details for paymentId: " + payment.getPaymentId());
//            }
//        }
//
//        Optional<Payment> latest = paymentDao.findById(payment.getPaymentId());
//        Payment finalPayment = latest.orElse(payment);
//
//        return modelToDto(finalPayment);
//    }
//
//    @Override
//    public PaymentDto getPaymentById(int paymentId) {
//        Payment payment = paymentDao.findById(paymentId)
//                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
//        return modelToDto(payment);
//    }
//
//    @Override
//    public PaymentDto getPaymentByOrderId(int orderId) {
//        Optional<Payment> payment = paymentDao.findByOrderId(orderId);
//        	if(payment.isEmpty()) {
//        		return null;
//        	}
//
//        return modelToDto(payment.get());
//    }
//    @Override
//	public List<Payment> getAllPayments(){
//		return paymentDao.findAll();
//	}
//    @Override
//    public PaymentDto updatePaymentStatus(int paymentId, String status) {
//
//        PaymentDto existing = getPaymentById(paymentId);
//
//        String current = existing.getStatus();
//        if (current != null && current.equalsIgnoreCase(status)) {
//            // nothing to do (idempotent)
//            return existing;
//        }
//
//        // do not allow changing a final payment again
//        if ("REFUNDED".equalsIgnoreCase(current) || "FAILED".equalsIgnoreCase(current)) {
//            throw new PaymentAlreadyCompletedException(paymentId);
//        }
//
//        int rows = paymentDao.updateStatus(paymentId, status);
//        if (rows <= 0) {
//            throw new RuntimeException("Update status failed for Payment Id: " + paymentId);
//        }
//
//        return getPaymentById(paymentId);
//    }
//
//    @Override
//    public PaymentDto confirmUpiPaymentOrder(int orderId,
//                                             String razorpayPaymentId,
//                                             String razorpaySignature,
//                                             String upiVpa) {
//
//        Payment payment = paymentDao.findByOrderId(orderId)
//                .orElseThrow(() -> new ResourceNotFoundException("No payment stored with order id: " + orderId));
//
//        String razorpayOrderId = payment.getGatewayRef();
//        if (razorpayOrderId == null) {
//            throw new RuntimeException("No Razorpay order id stored for payment id: " + payment.getPaymentId());
//        }
//
//        boolean valid = razorpayGatewayService.verifySignature(
//                razorpayOrderId,
//                razorpayPaymentId,
//                razorpaySignature);
//
//        if (!valid) {
//            throw new RuntimeException("Invalid Razorpay signature for payment id: " + payment.getPaymentId());
//        }
//
//        int gwRows = paymentDao.updateGatewayDetails(
//                payment.getPaymentId(),
//                payment.getGatewayRef(),
//                upiVpa,
//                payment.getResponsePayload());
//
//        if (gwRows <= 0) {
//            throw new RuntimeException("Failed to update gateway details for id: " + payment.getPaymentId());
//        }
//
//        int statusRows = paymentDao.updateStatus(payment.getPaymentId(), "PAID");
//        if (statusRows <= 0) {
//            throw new RuntimeException("Failed to update payment status for id: " + payment.getPaymentId());
//        }
//
//        return getPaymentById(payment.getPaymentId());
//    }
//
//    @Override
//    public PaymentDto updateGatewayDetails(int paymentId, String gatewayRef, String upiVpa, String razorPayload) {
//
//        int rows = paymentDao.updateGatewayDetails(paymentId, gatewayRef, upiVpa, razorPayload);
//        if (rows <= 0) {
//            throw new RuntimeException("Failed to update gateway details for id: " + paymentId);
//        }
//
//        return getPaymentById(paymentId);
//    }
//
//    @Override
//    public List<Map<String, Object>> getPaymentDetails(int userId) {
//        return paymentDao.getPaymentDetails(userId);
//    }
//}
//

package com.ShopSphere.shop_sphere.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ShopSphere.shop_sphere.dto.PaymentDto;
import com.ShopSphere.shop_sphere.exception.PaymentAlreadyCompletedException;
import com.ShopSphere.shop_sphere.exception.ResourceNotFoundException;
import com.ShopSphere.shop_sphere.model.Payment;
import com.ShopSphere.shop_sphere.repository.PaymentDao;

@Service
public class PaymentServiceImpl implements PaymentService {


private final PaymentDao paymentDao;

public PaymentServiceImpl(PaymentDao paymentDao) {
    this.paymentDao = paymentDao;
}

private Payment dtoToModel(PaymentDto dto) {
    Payment payment = new Payment();
    payment.setOrderId(dto.getOrderId());
    payment.setUserId(dto.getUserId());
    payment.setAmount(dto.getAmount());
    payment.setCurrency(dto.getCurrency());
    payment.setPaymentMethod(dto.getPaymentMethod());
    payment.setCreatedAt(dto.getCreatedAt());
    payment.setStatus(dto.getStatus());
    payment.setGatewayRef(dto.getGatewayRef());
    payment.setUpiVpa(dto.getUpiVpa());
    payment.setResponsePayload(dto.getResponsePayLoad());
    return payment;
}

private PaymentDto modelToDto(Payment payment) {
    PaymentDto dto = new PaymentDto();
    dto.setPayment_id(payment.getPaymentId());
    dto.setOrderId(payment.getOrderId());
    dto.setUserId(payment.getUserId());
    dto.setAmount(payment.getAmount());
    dto.setCurrency(payment.getCurrency());
    dto.setPaymentMethod(payment.getPaymentMethod());
    dto.setStatus(payment.getStatus());
    dto.setGatewayRef(payment.getGatewayRef());
    dto.setUpiVpa(payment.getUpiVpa());
    dto.setResponsePayLoad(payment.getResponsePayload());
    return dto;
}

@Override
public PaymentDto createPayment(PaymentDto dto) {
    if (dto == null || dto.getOrderId() == null) {
        throw new IllegalArgumentException("PaymentDto or orderId must not be null");
    }

    Payment payment = dtoToModel(dto);
    payment.setCreatedAt(LocalDateTime.now());

    // 1️⃣ COD → PENDING
    if ("COD".equalsIgnoreCase(payment.getPaymentMethod())) {
        payment.setStatus("PENDING");
    }
    // 2️⃣ UPI → Mock PAID/FAILED
    else if ("UPI".equalsIgnoreCase(payment.getPaymentMethod())) {
        payment.setGatewayRef("UPI_" + payment.getOrderId());
        payment.setUpiVpa("user@upi");

        double rand = Math.random();
        if (rand <= 0.9) {
            payment.setStatus("PAID");
        } else {
            payment.setStatus("FAILED");
        }
    }
    // 3️⃣ Cancelled → REFUNDED
    else if ("CANCELLED".equalsIgnoreCase(payment.getStatus())) {
        payment.setStatus("REFUNDED");
    } 
    // Default
    else if (payment.getStatus() == null || payment.getStatus().isEmpty()) {
        payment.setStatus("PENDING");
    }

    int rows = paymentDao.save(payment);
    if (rows <= 0) {
        throw new RuntimeException("Failed to create payment for orderId: " + payment.getOrderId());
    }

    Optional<Payment> latest = paymentDao.findById(payment.getPaymentId());
    Payment finalPayment = latest.orElse(payment);

    return modelToDto(finalPayment);
}

@Override
public PaymentDto getPaymentById(int paymentId) {
    Payment payment = paymentDao.findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
    return modelToDto(payment);
}

@Override
public PaymentDto getPaymentByOrderId(int orderId) {
    Optional<Payment> payment = paymentDao.findByOrderId(orderId);
    return payment.map(this::modelToDto).orElse(null);
}

@Override
public List<Payment> getAllPayments() {
    return paymentDao.findAll();
}

@Override
public PaymentDto updatePaymentStatus(int paymentId, String status) {
    PaymentDto existing = getPaymentById(paymentId);

    String current = existing.getStatus();
    if (current != null && current.equalsIgnoreCase(status)) {
        return existing;
    }

    if ("REFUNDED".equalsIgnoreCase(current) || "FAILED".equalsIgnoreCase(current)) {
        throw new PaymentAlreadyCompletedException(paymentId);
    }

    int rows = paymentDao.updateStatus(paymentId, status);
    if (rows <= 0) {
        throw new RuntimeException("Update status failed for Payment Id: " + paymentId);
    }

    return getPaymentById(paymentId);
}

@Override
public PaymentDto confirmUpiPaymentOrder(int orderId, String ignored1, String ignored2, String upiVpa) {
    Payment payment = paymentDao.findByOrderId(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("No payment found for order id: " + orderId));

    if (!"PAID".equalsIgnoreCase(payment.getStatus()) && !"FAILED".equalsIgnoreCase(payment.getStatus())) {
        double rand = Math.random();
        if (rand <= 0.9) {
            payment.setStatus("PAID");
        } else {
            payment.setStatus("FAILED");
        }

        payment.setUpiVpa(upiVpa);
        payment.setGatewayRef("UPI_" + payment.getPaymentId());

        paymentDao.updateGatewayDetails(payment.getPaymentId(),
                payment.getGatewayRef(),
                payment.getUpiVpa(),
                "MOCK_CONFIRM");

        paymentDao.updateStatus(payment.getPaymentId(), payment.getStatus());
    }

    return modelToDto(payment);
}

@Override
public PaymentDto updateGatewayDetails(int paymentId, String gatewayRef, String upiVpa, String razorPayload) {
    int rows = paymentDao.updateGatewayDetails(paymentId, gatewayRef, upiVpa, razorPayload);
    if (rows <= 0) {
        throw new RuntimeException("Failed to update gateway details for id: " + paymentId);
    }
    return getPaymentById(paymentId);
}

@Override
public List<Map<String, Object>> getPaymentDetails(int userId) {
    return paymentDao.getPaymentDetails(userId);
}

@Override
@Transactional
public PaymentDto updatePaymentStatusByOrderId(int orderId, String status) {
    PaymentDto payment = getPaymentByOrderId(orderId); // fetch correct row
    if (payment == null) {
        throw new RuntimeException("Payment not found for orderId: " + orderId);
    }
    return updatePaymentStatus(payment.getPayment_id(), status);
}


}
