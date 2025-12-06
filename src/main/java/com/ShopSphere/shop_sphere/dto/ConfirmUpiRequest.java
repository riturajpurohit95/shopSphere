package com.ShopSphere.shop_sphere.dto;

public class ConfirmUpiRequest {

	private String razorpayPaymentId;
	private String razorpaySignature;
	private String upiVpa;
	
	public ConfirmUpiRequest() {
		
}

	public String getRazorpayPaymentId() {
		return razorpayPaymentId;
	}

	public void setRazorpayPaymentId(String razorpayPaymentId) {
		this.razorpayPaymentId = razorpayPaymentId;
	}

	public String getRazorpaySignature() {
		return razorpaySignature;
	}

	public void setRazorpaySignature(String razorpaySignature) {
		this.razorpaySignature = razorpaySignature;
	}

	public String getUpiVpa() {
		return upiVpa;
	}

	public void setUpiVpa(String upiVpa) {
		this.upiVpa = upiVpa;
	}
}