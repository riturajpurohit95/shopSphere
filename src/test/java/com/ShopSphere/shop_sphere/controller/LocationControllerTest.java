package com.ShopSphere.shop_sphere.controller;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.MockedStatic;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ShopSphere.shop_sphere.dto.LocationDto;
import com.ShopSphere.shop_sphere.model.Location;
import com.ShopSphere.shop_sphere.security.SecurityUtil;
import com.ShopSphere.shop_sphere.service.LocationService;

public class LocationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LocationService locationService;

    @InjectMocks
    private LocationController locationController;

    private ObjectMapper objectMapper = new ObjectMapper();

    private Location sampleLocation;
    private LocationDto sampleDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(locationController).build();

        sampleLocation = new Location();
        sampleLocation.setLocationId(7);
        sampleLocation.setCity("mumbai");
        sampleLocation.setHubValue(10);

        sampleDto = new LocationDto();
        sampleDto.setCity("Mumbai");
        sampleDto.setHubValue(10);
    }

    // ---------- createLocation (ADMIN) ----------
    @Test
    void testCreateLocation_AsAdmin_Succeeds() throws Exception {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(true);

            when(locationService.createLocation(any(Location.class))).thenReturn(sampleLocation);

            mockMvc.perform(post("/api/locations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(sampleDto)))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$.locationId").value(7))
                   .andExpect(jsonPath("$.city").value("mumbai"))
                   .andExpect(jsonPath("$.hubValue").value(10));

            verify(locationService, times(1)).createLocation(any(Location.class));
        }
    }

    @Test
    void testCreateLocation_NotAdmin_ThrowsSecurity() throws Exception {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(false);

            mockMvc.perform(post("/api/locations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(sampleDto)))
                   .andExpect(status().isInternalServerError()); // SecurityException -> 500 without advice

            verify(locationService, never()).createLocation(any());
        }
    }

    @Test
    void testCreateLocation_InvalidCity_Throws() throws Exception {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(true);

            LocationDto bad = new LocationDto();
            bad.setCity(" ");
            bad.setHubValue(5);

            mockMvc.perform(post("/api/locations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bad)))
                   .andExpect(status().isInternalServerError()); // IllegalArgumentException -> 500

            verify(locationService, never()).createLocation(any());
        }
    }

    @Test
    void testCreateLocation_InvalidHubValue_Throws() throws Exception {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(true);

            LocationDto bad = new LocationDto();
            bad.setCity("Pune");
            bad.setHubValue(-1);

            mockMvc.perform(post("/api/locations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bad)))
                   .andExpect(status().isInternalServerError()); // IllegalArgumentException -> 500

            verify(locationService, never()).createLocation(any());
        }
    }

    // ---------- getLocationById ----------
    @Test
    void testGetLocationById_Succeeds() throws Exception {
        when(locationService.getLocationById(7)).thenReturn(sampleLocation);

        mockMvc.perform(get("/api/locations/7"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.locationId").value(7))
               .andExpect(jsonPath("$.city").value("mumbai"));

        verify(locationService, times(1)).getLocationById(7);
    }

    @Test
    void testGetLocationById_InvalidId_Throws() throws Exception {
        mockMvc.perform(get("/api/locations/0"))
               .andExpect(status().isInternalServerError()); // IllegalArgumentException -> 500
        verify(locationService, never()).getLocationById(anyInt());
    }

    // ---------- getAllLocations ----------
    @Test
    void testGetAllLocations_Succeeds() throws Exception {
        when(locationService.getAllLocation()).thenReturn(Arrays.asList(sampleLocation));

        mockMvc.perform(get("/api/locations"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.size()").value(1))
               .andExpect(jsonPath("$[0].city").value("mumbai"));

        verify(locationService, times(1)).getAllLocation();
    }

    // ---------- getLocationsByCity ----------
    @Test
    void testGetLocationsByCity_Succeeds() throws Exception {
        when(locationService.getLocationsByCity("mumbai")).thenReturn(Arrays.asList(sampleLocation));

        mockMvc.perform(get("/api/locations/city/mumbai"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.size()").value(1))
               .andExpect(jsonPath("$[0].city").value("mumbai"));

        verify(locationService, times(1)).getLocationsByCity("mumbai");
    }

    @Test
    void testGetLocationsByCity_InvalidCity_Throws() throws Exception {
        mockMvc.perform(get("/api/locations/city/ "))
               .andExpect(status().isInternalServerError());
        verify(locationService, never()).getLocationsByCity(any());
    }

    // ---------- searchLocations ----------
    @Test
    void testSearchLocations_Succeeds() throws Exception {
        when(locationService.searchLocationByKeyword("mum")).thenReturn(Arrays.asList(sampleLocation));

        mockMvc.perform(get("/api/locations/search/mum"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.size()").value(1));
        verify(locationService, times(1)).searchLocationByKeyword("mum");
    }

    @Test
    void testSearchLocations_Invalid_Throws() throws Exception {
        mockMvc.perform(get("/api/locations/search/ "))
               .andExpect(status().isInternalServerError());
        verify(locationService, never()).searchLocationByKeyword(any());
    }

    // ---------- existsByCity ----------
    @Test
    void testExistsByCity_ReturnsTrue() throws Exception {
        when(locationService.existsBycity("mumbai")).thenReturn(true);

        mockMvc.perform(get("/api/locations/exists/mumbai"))
               .andExpect(status().isOk())
               .andExpect(content().string("true"));

        verify(locationService, times(1)).existsBycity("mumbai");
    }

    @Test
    void testExistsByCity_InvalidCity_Throws() throws Exception {
        mockMvc.perform(get("/api/locations/exists/ "))
               .andExpect(status().isInternalServerError());
        verify(locationService, never()).existsBycity(any());
    }

    // ---------- countLocations ----------
    @Test
    void testCountLocations_ReturnsInteger() throws Exception {
        when(locationService.countLocations()).thenReturn(42);

        mockMvc.perform(get("/api/locations/count"))
               .andExpect(status().isOk())
               .andExpect(content().string("42"));

        verify(locationService, times(1)).countLocations();
    }

    // Parameterized sanity check
    @ParameterizedTest
    @ValueSource(strings = {"mumbai", "pune", "delhi"})
    void parameterizedCities(String city) throws Exception {
        when(locationService.getLocationsByCity(city)).thenReturn(List.of(sampleLocation));
        mockMvc.perform(get("/api/locations/city/" + city)).andExpect(status().isOk());
    }

    @Disabled("Example disabled controller test")
    @Test
    void disabledTestExample() {
        fail("This test is disabled");
    }
}