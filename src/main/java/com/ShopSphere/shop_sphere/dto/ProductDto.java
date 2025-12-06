package com.ShopSphere.shop_sphere.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ProductDto {


public ProductDto(Integer product_id, @NotNull(message = "User Id is required") Integer userId,
			@NotNull(message = "Category Id is required") Integer categoryId,
			@NotBlank(message = "Product name is required") String productName,
			@NotNull(message = "Product Price is required") @DecimalMin(value = "0.0", message = "Product Price must be >=0.0") BigDecimal productPrice,
			@NotNull(message = "Product MRP is required") @DecimalMin(value = "0.0", message = "Product MRP must be >=0.0") BigDecimal productMrp,
			@NotNull(message = "Product quantity is required") @Min(value = 1, message = "Product qsuantity must be >=1") int productQuantity,
			@NotNull(message = "Product average rating is required") @DecimalMin(value = "0.0", message = "Product average rating must be >=0.0") BigDecimal productAvgRating,
			@NotBlank(message = "Product Description is required") String productDescription,
			@NotNull(message = "Product Reviews Count is required") @Min(value = 0, message = "Product Reviews Count must be >=0") Integer productReviewsCount,
			String brand, String imageUrl) {
		super();
		this.product_id = product_id;
		this.userId = userId;
		this.categoryId = categoryId;
		this.productName = productName;
		this.productPrice = productPrice;
		this.productMrp = productMrp;
		this.productQuantity = productQuantity;
		this.productAvgRating = productAvgRating;
		this.productDescription = productDescription;
		this.productReviewsCount = productReviewsCount;
		this.brand = brand;
		this.imageUrl = imageUrl;
	}





	//private String productDescription;

	private Integer product_id;
		
		@NotNull(message  = "User Id is required")
		private Integer userId;
		
		@NotNull(message  = "Category Id is required")
		private Integer categoryId;
		
		@NotBlank(message  = "Product name is required")
		private String productName;
		
		@NotNull(message  = "Product Price is required")
		@DecimalMin(value = "0.0",message = "Product Price must be >=0.0")
        private BigDecimal productPrice;
		
		@NotNull(message  = "Product MRP is required")
		@DecimalMin(value = "0.0",message = "Product MRP must be >=0.0")
        private BigDecimal productMrp;
		
		@NotNull(message  = "Product quantity is required")
		@Min(value = 1,message = "Product qsuantity must be >=1")
		private int productQuantity;
		
		@NotNull(message  = "Product average rating is required")
		@DecimalMin(value = "0.0",message = "Product average rating must be >=0.0")
        private BigDecimal productAvgRating;
		
		@NotBlank(message  = "Product Description is required")
		private String productDescription;
		
		@NotNull(message  = "Product Reviews Count is required")
		@Min(value = 0,message = "Product Reviews Count must be >=0")
		private Integer productReviewsCount;
		

		private String brand;
		private String imageUrl;
		
		
		
		
		
		public ProductDto() {}





		public Integer getProduct_id() {
			return product_id;
		}





		public void setProduct_id(Integer product_id) {
			this.product_id = product_id;
		}





		public Integer getUserId() {
			return userId;
		}





		public void setUserId(Integer userId) {
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





		public int getProductQuantity() {
			return productQuantity;
		}





		public void setProductQuantity(int productQuantity) {
			this.productQuantity = productQuantity;
		}





		public BigDecimal getProductAvgRating() {
			return productAvgRating;
		}





		public void setProductAvgRating(BigDecimal productAvgRating) {
			this.productAvgRating = productAvgRating;
		}





		public String getProductDescription() {
			return productDescription;
		}





		public void setProductDescription(String productDescription) {
			this.productDescription = productDescription;
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





		public String getImageUrl() {
			return imageUrl;
		}





		public void setImageUrl(String imageUrl) {
			this.imageUrl = imageUrl;
		}





		




		






		
}

