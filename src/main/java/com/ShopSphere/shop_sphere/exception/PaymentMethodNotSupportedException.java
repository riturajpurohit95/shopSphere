package com.ShopSphere.shop_sphere.exception;

public class PaymentMethodNotSupportedException extends RuntimeException {

		
		public PaymentMethodNotSupportedException(String method) {
			super("Payment method not supported: "+ method);
		}

	}


