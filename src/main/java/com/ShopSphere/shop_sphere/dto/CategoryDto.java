package com.ShopSphere.shop_sphere.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

public class CategoryDto {
	
	
	
	public CategoryDto(Integer categoryId, @NotBlank(message = "Category name is required") String categoryName) {
		super();
		this.categoryId = categoryId;
		this.categoryName = categoryName;
	}


	public CategoryDto() {}
	
	private Integer categoryId;
	@NotBlank(message="Category name is required")
	
	private String categoryName;
	//private String description;
	public Integer getCategoryId() {
		return categoryId;
	}


	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}


	public String getCategoryName() {
		return categoryName;
	}


	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
	
	


}