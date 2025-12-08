package com.ShopSphere.shop_sphere.service;
 
import java.util.List;
 
import com.ShopSphere.shop_sphere.model.Location;
 
public interface LocationService {
	Location createLocation(Location location);
	Location getLocationById(int locationId);
	List<Location> getAllLocation();
	List<Location> getLocationsByCity(String city);
	List<Location> searchLocationByKeyword(String keyword);
//	Location updateLocation(int locationId, Location location);
	boolean existsBycity(String city);
	int countLocations();
 
}