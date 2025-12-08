package com.ShopSphere.shop_sphere.model;

public class Location {
	
	public Location(int locationId, String city, int hubValue) {
		this.locationId = locationId;
		this.city = city;
		this.hubValue = hubValue;
	}

	private int locationId;
	private String city;
	private int hubValue;
	
	public Location() {}

	public int getLocationId() {
		return locationId;
	}

	public String getCity() {
		return city;
	}

	public int getHubValue() {
		return hubValue;
	}

	public void setLocationId(int locationId) {
		this.locationId = locationId;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public void setHubValue(int hubValue) {
		this.hubValue = hubValue;
	}

	@Override
	public String toString() {
		return "Location [locationId=" + locationId + ", city=" + city + ", hubValue=" + hubValue + "]";
	}
	
	

}
