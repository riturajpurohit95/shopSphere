package com.ShopSphere.shop_sphere.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ShopSphere.shop_sphere.dto.LocationDto;
import com.ShopSphere.shop_sphere.model.Location;
import com.ShopSphere.shop_sphere.security.AllowedRoles;
import com.ShopSphere.shop_sphere.security.SecurityUtil;
import com.ShopSphere.shop_sphere.service.LocationService;

import jakarta.servlet.http.HttpServletRequest;


@CrossOrigin(origins="http://localhost:3000")
@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    // ---------------- Helper Methods ----------------

    private Location dtoToEntity(LocationDto dto) {
        Location loc = new Location();
        loc.setCity(dto.getCity().trim().toLowerCase());
        loc.setHubValue(dto.getHubValue());
        return loc;
    }

    private LocationDto entityToDto(Location loc) {
        LocationDto dto = new LocationDto();
        dto.setLocationId(loc.getLocationId());
        dto.setCity(loc.getCity());
        dto.setHubValue(loc.getHubValue());
        return dto;
    }

    // ---------------- Security Helper ----------------
    private void validateAdmin(HttpServletRequest request) {
        if (!SecurityUtil.isAdmin(request)) {
            throw new SecurityException("Unauthorized: Admin access required");
        }
    }

    // ---------------- API Endpoints ----------------

    @AllowedRoles({"ADMIN"})
    @PostMapping
    public ResponseEntity<LocationDto> createLocation(@RequestBody LocationDto dto, HttpServletRequest request) {
        validateAdmin(request);

        if (dto.getCity() == null || dto.getCity().trim().isBlank()) {
            throw new IllegalArgumentException("City cannot be empty");
        }
        if (dto.getHubValue() == null || dto.getHubValue() < 0) {
            throw new IllegalArgumentException("Hub value must be a positive integer");
        }

        Location saved = locationService.createLocation(dtoToEntity(dto));
        return ResponseEntity.ok(entityToDto(saved));
    }

    @AllowedRoles({"BUYER", "ADMIN","SELLER"})
    @GetMapping("/{locationId}")
    public ResponseEntity<LocationDto> getLocationById(@PathVariable int locationId) {
        if (locationId <= 0) {
            throw new IllegalArgumentException("Invalid location ID");
        }

        Location loc = locationService.getLocationById(locationId);
        return ResponseEntity.ok(entityToDto(loc));
    }

    @AllowedRoles({"BUYER", "ADMIN","SELLER"})
    @GetMapping
    public ResponseEntity<List<LocationDto>> getAllLocations() {
        List<LocationDto> list = locationService.getAllLocation().stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @AllowedRoles({"BUYER", "ADMIN","SELLER"})
    @GetMapping("/city/{city}")
    public ResponseEntity<List<LocationDto>> getLocationsByCity(@PathVariable String city) {
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("City cannot be empty");
        }
        List<LocationDto> list = locationService.getLocationsByCity(city).stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @AllowedRoles({"BUYER", "ADMIN","SELLER"})
    @GetMapping("/search/{keyword}")
    public ResponseEntity<List<LocationDto>> searchLocations(@PathVariable String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("Search keyword cannot be empty");
        }
        List<LocationDto> list = locationService.searchLocationByKeyword(keyword).stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @AllowedRoles({"BUYER", "ADMIN","SELLER"})
    @GetMapping("/exists/{city}")
    public ResponseEntity<Boolean> existsByCity(@PathVariable String city) {
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("City cannot be empty");
        }
        return ResponseEntity.ok(locationService.existsBycity(city));
    }

    @AllowedRoles({"BUYER", "ADMIN","SELLER"})
    @GetMapping("/count")
    public ResponseEntity<Integer> countLocations() {
        return ResponseEntity.ok(locationService.countLocations());
    }
}
