package com.ShopSphere.shop_sphere.service;

import org.springframework.stereotype.Service;

import com.ShopSphere.shop_sphere.repository.LocationDao;
import com.ShopSphere.shop_sphere.repository.ProductDao;
import com.ShopSphere.shop_sphere.repository.UserDao;

@Service
public class DeliveryService {

	
	private final LocationDao locationDao;
	private final UserService userService;
	private final ProductDao productDao;
	
	public DeliveryService(LocationDao locationDao, 
			UserService userService, ProductDao productDao)
	{
		this.locationDao = locationDao;
		this.productDao = productDao;
		this.userService = userService;
	}
	
	public int calculateDeliveryDays(int buyerId, int productId) {
		int buyerLocationId = userService.getLocationIdOfUser(buyerId);
		int buyerHub = locationDao.getHubValue(buyerLocationId);
		int sellerId = productDao.getSellerIdByProductId(productId);
		int sellerLocationId = userService.getLocationIdOfUser(sellerId);
		int sellerHub = locationDao.getHubValue(sellerLocationId);
		int diff = Math.abs(buyerHub - sellerHub );
		return diff / 20;
	}
}
