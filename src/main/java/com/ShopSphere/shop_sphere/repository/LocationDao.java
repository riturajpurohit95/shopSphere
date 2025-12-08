package com.ShopSphere.shop_sphere.repository;
 
import java.util.List;
import java.util.Optional;
 
import com.ShopSphere.shop_sphere.model.Location;
 
public interface LocationDao {
	Location save(Location location);
	Optional<Location> finndById(int location);
	List<Location> finAll();
	List<Location> findByCity(String city);
	List<Location> searchByCity(String keyword);
	boolean existsByCity(String city);
	int countLocations();
	int getHubValue(int locationId);

 
}