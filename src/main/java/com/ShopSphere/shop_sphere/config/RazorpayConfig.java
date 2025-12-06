package com.ShopSphere.shop_sphere.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.razorpay.RazorpayClient;

@Configuration
public class RazorpayConfig {
	
	private final String key;
	private final String secret;
	private final RazorpayClient client;
	
	
	public RazorpayConfig(Environment env) throws Exception{
		this.key = env.getProperty("razorpay.key");
		this.secret = env.getProperty("razorpay.secret");
		
		if (key ==null || key.isEmpty( ) || secret == null || secret.isEmpty()) {
			throw new IllegalStateException("Razorpay key/secret not configured");
		}
		this.client = new RazorpayClient(key, secret);
	}
	
	public String getKey() {
		return key;
	}
	public String getSecret() {
		return secret;
	}
	public RazorpayClient getClient() {
		return client;
	}

}
