package com.ShopSphere.shop_sphere.model;

import java.math.BigDecimal;

public class Product {
	
	


	public Product(int productId, int userId, Integer categoryId, String productName, BigDecimal productPrice,
			BigDecimal productMrp, Integer productQuantity, BigDecimal productAvgRating, Integer productReviewsCount,
			String brand, String productDescription, String imageUrl) {
		super();
		this.productId = productId;
		this.userId = userId;
		this.categoryId = categoryId;
		this.productName = productName;
		this.productPrice = productPrice;
		this.productMrp = productMrp;
		this.productQuantity = productQuantity;
		this.productAvgRating = productAvgRating;
		this.productReviewsCount = productReviewsCount;
		this.brand = brand;
		this.productDescription = productDescription;
		this.imageUrl = imageUrl;
	}

	private int productId;
	private int userId;
	private Integer categoryId;
	private String productName;
	private BigDecimal productPrice;
	private BigDecimal productMrp;
	private Integer productQuantity;
	private BigDecimal productAvgRating;
	private Integer productReviewsCount;
	private String brand;
	private String productDescription;
	private String imageUrl;
	
	public Product() {}

	public int getProductId() {
		return productId;
	}

	public void setProductId(int productId) {
		this.productId = productId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public Integer getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public BigDecimal getProductPrice() {
		return productPrice;
	}

	public void setProductPrice(BigDecimal productPrice) {
		this.productPrice = productPrice;
	}

	public BigDecimal getProductMrp() {
		return productMrp;
	}

	public void setProductMrp(BigDecimal productMrp) {
		this.productMrp = productMrp;
	}

	public Integer getProductQuantity() {
		return productQuantity;
	}

	public void setProductQuantity(Integer productQuantity) {
		this.productQuantity = productQuantity;
	}

	public BigDecimal getProductAvgRating() {
		return productAvgRating;
	}

	public void setProductAvgRating(BigDecimal productAvgRating) {
		this.productAvgRating = productAvgRating;
	}

	public Integer getProductReviewsCount() {
		return productReviewsCount;
	}

	public void setProductReviewsCount(Integer productReviewsCount) {
		this.productReviewsCount = productReviewsCount;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getProductDescription() {
		return productDescription;
	}

	public void setProductDescription(String productDescription) {
		this.productDescription = productDescription;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	



	

}

