package com.ShopSphere.shop_sphere.exception;

public class PaymentAlreadyCompletedException extends RuntimeException{

	public PaymentAlreadyCompletedException(int paymentId){
		super("Payment with ID: "+ paymentId+ "is already completed.");
	}

}
