package com.ShopSphere.shop_sphere.service;
 
import java.util.List;
 
import org.springframework.stereotype.Service;
 
import com.ShopSphere.shop_sphere.exception.ResourceNotFoundException;
import com.ShopSphere.shop_sphere.model.Location;
import com.ShopSphere.shop_sphere.repository.LocationDao;
 
@Service
public class LocationServiceImpl implements LocationService{
	private final LocationDao locationDao;
	public LocationServiceImpl(LocationDao locationDao) {
		this.locationDao = locationDao;
	}
	@Override
	public Location createLocation(Location location) {
		if(locationDao.existsByCity(location.getCity())) {
			throw new RuntimeException("Location already exists with city: "+location.getCity());
		}
		return locationDao.save(location);
	}
 
	@Override
	public Location getLocationById(int locationId) {
		return locationDao.finndById(locationId)
							.orElseThrow(()-> new ResourceNotFoundException("Location not found with id: "+locationId));
	}
 
	@Override
	public List<Location> getAllLocation() {
		return locationDao.finAll();
	}
 
	@Override
	public List<Location> getLocationsByCity(String city) {
		List<Location> list = locationDao.findByCity(city);
		if(list.isEmpty()) {
			throw new ResourceNotFoundException("No locations found for city: "+city);
		}
		return list;
	}
 
	@Override
	public List<Location> searchLocationByKeyword(String keyword) {
		List<Location> list = locationDao.searchByCity(keyword);
		if(list.isEmpty()) {
			throw new ResourceNotFoundException("No results found for keyword: "+keyword);
		}
		return list;
	}
 
//	@Override
//	public Location updateLocation(int locationId, Location location) {
//		Location existing = getLocationById(locationId);
//		existing.setCity(location.getCity());
//		existing.setHubValue(location.getHubValue());
//		int rows = locationDao.update(existing);
//		if(rows<=0) {
//			throw new RuntimeException("Update failed for location id:"+locationId);
//		}
//		return existing;
//	}
 
	@Override
	public boolean existsBycity(String city) {
		return locationDao.existsByCity(city);
	}
 
	@Override
	public int countLocations() {
		return locationDao.countLocations();
	}

}