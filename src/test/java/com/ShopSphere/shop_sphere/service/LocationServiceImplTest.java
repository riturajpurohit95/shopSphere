package com.ShopSphere.shop_sphere.service;

import com.ShopSphere.shop_sphere.exception.ResourceNotFoundException;
import com.ShopSphere.shop_sphere.model.Location;
import com.ShopSphere.shop_sphere.repository.LocationDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceImplTest {

    @Mock
    private LocationDao locationDao;

    @InjectMocks
    private LocationServiceImpl locationService;

    private Location sample;

    @BeforeEach
    void setup() {
        sample = new Location(1, "Chandigarh", 20);
    }

    // ──────────────────────────────────────────────
    // createLocation()
    // ──────────────────────────────────────────────

    @Test
    void createLocation_success() {
        when(locationDao.existsByCity("Chandigarh")).thenReturn(false);
        when(locationDao.save(sample)).thenReturn(sample);

        Location result = locationService.createLocation(sample);

        assertNotNull(result);
        assertEquals("Chandigarh", result.getCity());
        verify(locationDao).save(sample);
    }

    @Test
    void createLocation_shouldThrow_whenCityExists() {
        when(locationDao.existsByCity("Chandigarh")).thenReturn(true);

        assertThrows(
                RuntimeException.class,
                () -> locationService.createLocation(sample)
        );

        verify(locationDao, never()).save(any());
    }

    // ──────────────────────────────────────────────
    // getLocationById()
    // ──────────────────────────────────────────────

    @Test
    void getLocationById_success() {
        when(locationDao.finndById(1)).thenReturn(Optional.of(sample));

        Location result = locationService.getLocationById(1);

        assertEquals("Chandigarh", result.getCity());
        verify(locationDao).finndById(1);
    }

    @Test
    void getLocationById_notFound() {
        when(locationDao.finndById(1)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> locationService.getLocationById(1)
        );
    }

    // ──────────────────────────────────────────────
    // getAllLocation()
    // ──────────────────────────────────────────────

    @Test
    void getAllLocation_success() {
        when(locationDao.finAll()).thenReturn(Arrays.asList(sample));

        var list = locationService.getAllLocation();

        assertEquals(1, list.size());
        verify(locationDao).finAll();
    }

    // ──────────────────────────────────────────────
    // getLocationsByCity()
    // ──────────────────────────────────────────────

    @Test
    void getLocationsByCity_success() {
        when(locationDao.findByCity("Chandigarh"))
                .thenReturn(Arrays.asList(sample));

        var list = locationService.getLocationsByCity("Chandigarh");

        assertEquals(1, list.size());
        assertEquals("Chandigarh", list.get(0).getCity());
    }

    @Test
    void getLocationsByCity_notFound() {
        when(locationDao.findByCity("Unknown")).thenReturn(Collections.emptyList());

        assertThrows(
                ResourceNotFoundException.class,
                () -> locationService.getLocationsByCity("Unknown")
        );
    }

    // ──────────────────────────────────────────────
    // searchLocationByKeyword()
    // ──────────────────────────────────────────────

    @Test
    void searchLocationByKeyword_success() {
        when(locationDao.searchByCity("chan"))
                .thenReturn(Arrays.asList(sample));

        var results = locationService.searchLocationByKeyword("chan");

        assertEquals(1, results.size());
    }

    @Test
    void searchLocationByKeyword_notFound() {
        when(locationDao.searchByCity("zzz"))
                .thenReturn(Collections.emptyList());

        assertThrows(
                ResourceNotFoundException.class,
                () -> locationService.searchLocationByKeyword("zzz")
        );
    }

    // ──────────────────────────────────────────────
    // existsBycity()
    // ──────────────────────────────────────────────

    @Test
    void existsByCity_success() {
        when(locationDao.existsByCity("Pune")).thenReturn(true);

        boolean exists = locationService.existsBycity("Pune");

        assertTrue(exists);
        verify(locationDao).existsByCity("Pune");
    }

    // ──────────────────────────────────────────────
    // countLocations()
    // ──────────────────────────────────────────────

    @Test
    void countLocations_success() {
        when(locationDao.countLocations()).thenReturn(5);

        int count = locationService.countLocations();

        assertEquals(5, count);
        verify(locationDao).countLocations();
    }
}
