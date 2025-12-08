package com.ShopSphere.shop_sphere.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.ShopSphere.shop_sphere.dto.PaymentDto;
import com.ShopSphere.shop_sphere.model.Payment;

public interface PaymentService {
	PaymentDto createPayment(PaymentDto dto);
	PaymentDto getPaymentById(int paymentId);
	PaymentDto getPaymentByOrderId(int orderId);
	PaymentDto updatePaymentStatus(int paymentId, String status);

	PaymentDto confirmUpiPaymentOrder(int orderId, String razorpayPaymentId, String razorpaySignature, String upiVpa);
	PaymentDto updateGatewayDetails(int paymentId, String gatewayRef, String upiVpa, String responsePayload);
	List<Map<String, Object>> getPaymentDetails(int userId);
	List<Payment> getAllPayments();
	PaymentDto updatePaymentStatusByOrderId(int orderId, String status);
	
	
	
	
}
