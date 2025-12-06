//package com.ShopSphere.shop_sphere.dto;
//
//import jakarta.validation.constraints.NotNull;
//
//public class CartItemDto {
//	
//
//	public CartItemDto() {
//		super();
//	}
//	public CartItemDto(Integer cartItemsId, @NotNull(message = "Cart ID is required") Integer cartId,
//			@NotNull(message = "Product ID is required") Integer productId,
//			@NotNull(message = "Quantity is required") Integer quantity) {
//		super();
//		this.cartItemsId = cartItemsId;
//		this.cartId = cartId;
//		this.productId = productId;
//		this.quantity = quantity;
//	}
//	
//	
//
//
//	public CartItemDto(Integer cartItemsId, @NotNull(message = "Cart ID is required")  Integer cartId, @NotNull(message = "Quantity is required") Integer quantity,@NotNull(message = "Product ID is required")  Integer productId, String productName,
//			double productPrice) {
//		
//		this.cartItemsId = cartItemsId;
//		this.cartId = cartId;
//this.productId = productId;
//		this.quantity = quantity;
//		//this.productName = productName;
//		//this.productPrice= productPrice;
//	}
//
//
//
//
//	private Integer cartItemsId;
//	@NotNull(message = "Cart ID is required")
//	private Integer cartId;
//	@NotNull(message = "Product ID is required")
//	private Integer productId;
//	@NotNull(message = "Quantity is required")
//	private Integer quantity;
//	//private String productName;
//	//private Double productPrice;
//	
//	//private Double totalItemPrice;
//	
//	public Integer getCartItemsId() {
//		return cartItemsId;
//	}
//	public Integer getCartId() {
//		return cartId;
//	}
//	public Integer getProductId() {
//		return productId;
//	}
//	public Integer getQuantity() {
//		return quantity;
//	}
//	/*public String getProductName() {
//		return productName;
//	}
//	public Double getProductPrice() {
//		return productPrice;
//	}*/
//	
//	public void setCartItemsId(Integer cartItemsId) {
//		this.cartItemsId = cartItemsId;
//	}
//	public void setCartId(Integer cartId) {
//		this.cartId = cartId;
//	}
//	public void setProductId(Integer productId) {
//		this.productId = productId;
//	}
//	public void setQuantity(Integer quantity) {
//		this.quantity = quantity;
//	}
///*	public void setProductName(String productName) {
//		this.productName = productName;
//	}
//	public void setProductPrice(Double productPrice) {
//		this.productPrice = productPrice;
//	}*/
//
//	
//	
//
//}
//
//

package com.ShopSphere.shop_sphere.dto;

import jakarta.validation.constraints.NotNull;

public class CartItemDto {

    public CartItemDto(Integer cartItemsId, @NotNull(message = "Cart ID is required") Integer cartId,
			@NotNull(message = "Product ID is required") Integer productId,
			@NotNull(message = "Quantity is required") Integer quantity, String productName, Double productPrice,
			String imageUrl) {
		super();
		this.cartItemsId = cartItemsId;
		this.cartId = cartId;
		this.productId = productId;
		this.quantity = quantity;
		this.productName = productName;
		this.productPrice = productPrice;
		this.imageUrl = imageUrl;
	}
	private Integer cartItemsId;

    @NotNull(message = "Cart ID is required")
    private Integer cartId;

    @NotNull(message = "Product ID is required")
    private Integer productId;

    @NotNull(message = "Quantity is required")
    private Integer quantity;

    // New fields for frontend
    private String productName;
    private Double productPrice;
    private String imageUrl;

    public CartItemDto() {
        super();
    }

    public CartItemDto(Integer cartItemsId, @NotNull(message = "Cart ID is required") Integer cartId,
                       @NotNull(message = "Product ID is required") Integer productId,
                       @NotNull(message = "Quantity is required") Integer quantity) {
        this.cartItemsId = cartItemsId;
        this.cartId = cartId;
        this.productId = productId;
        this.quantity = quantity;
    }



    // Getters and setters
    public Integer getCartItemsId() { return cartItemsId; }
    public void setCartItemsId(Integer cartItemsId) { this.cartItemsId = cartItemsId; }

    public Integer getCartId() { return cartId; }
    public void setCartId(Integer cartId) { this.cartId = cartId; }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Double getProductPrice() { return productPrice; }
    public void setProductPrice(Double productPrice) { this.productPrice = productPrice; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}

