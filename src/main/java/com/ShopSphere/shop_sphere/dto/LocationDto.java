package com.ShopSphere.shop_sphere.dto;

import jakarta.validation.constraints.NotBlank;

public class LocationDto {
	
	public LocationDto(Integer locationId, @NotBlank(message = "City is required") String city, 
			@NotBlank(message = "Hub value is required") Integer hubValue) {
		super();
		this.locationId = locationId;
		this.city = city;
		this.hubValue = hubValue;
	}
	
	public LocationDto(){}
	private Integer locationId;
	@NotBlank(message = "City is required")
	private String city;
	@NotBlank(message = "Hub value is required")
	private Integer hubValue;
	
	public Integer getLocationId() {
		return locationId;
	}
	public String getCity() {
		return city;
	}
	public void setLocationId(Integer locationId) {
		this.locationId = locationId;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public Integer getHubValue() {
		return hubValue;
	}
	public void setHubValue(Integer hubValue) {
		this.hubValue = hubValue;
	}
	
	

}
