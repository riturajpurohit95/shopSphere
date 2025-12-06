package com.ShopSphere.shop_sphere.service;

import java.math.BigDecimal;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.ShopSphere.shop_sphere.config.RazorpayConfig;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;

@Service
public class RazorpayGatewayService {

	public RazorpayGatewayService(RazorpayConfig razorpayConfig) {
		super();
		this.razorpayConfig = razorpayConfig;
	}

	private final RazorpayConfig razorpayConfig;
	

public Order createUpiOrder(String receipt, BigDecimal amountInPaise, String currency) throws Exception{
	RazorpayClient client = razorpayConfig.getClient();
	
	JSONObject request = new JSONObject();
	request.put("amount", amountInPaise);
	request.put("currency", currency);
	request.put("receipt", receipt);
	request.put("payment_capture",1);
	
	return client.Orders.create(request);
	
}
public boolean verifySignature(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
	try {
		JSONObject attributes = new JSONObject();
		attributes.put("razorpay_order_id",razorpayOrderId);
		attributes.put("razorpay_payment_id",razorpayPaymentId);
		attributes.put("razorpay_signature",razorpaySignature);
		
		Utils.verifyPaymentSignature(attributes, razorpayConfig.getSecret());
		return true;
		
	}catch(Exception e) {
		return false;
	}
}
	public String getPublicKey() {
		return razorpayConfig.getKey();
	}
}