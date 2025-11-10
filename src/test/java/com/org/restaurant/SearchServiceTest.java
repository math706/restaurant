package com.org.restaurant;

import com.org.restaurant.dto.SearchCriteria;
import com.org.restaurant.model.Cuisine;
import com.org.restaurant.model.Restaurant;
import com.org.restaurant.repository.RestaurantRepository;
import com.org.restaurant.service.SearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class SearchServiceTest {

    private RestaurantRepository repository;
    private SearchService service;

    @BeforeEach
    void setup() {
        repository = Mockito.mock(RestaurantRepository.class);
        service = new SearchService(repository);
    }

    @Test
    void testFilterByRatingAndPriceAndSortTieBreakers() {
        // cuisines
        Cuisine cAmerican = Cuisine.builder().id(1L).name("American").build();
        Cuisine cChinese = Cuisine.builder().id(2L).name("Chinese").build();

        // Prepare restaurants:
        // name, rating, distance, price, cuisine
        Restaurant r1 = Restaurant.builder()
                .id(1L)
                .name("Mcdonald's")
                .customerRating(4)
                .distance(1.0)
                .price(10.0)
                .cuisine(cAmerican)
                .build();

        Restaurant r2 = Restaurant.builder()
                .id(2L)
                .name("KFC")
                .customerRating(3)
                .distance(1.0)
                .price(8.0)
                .cuisine(cAmerican)
                .build();

        Restaurant r3 = Restaurant.builder()
                .id(3L)
                .name("Far Away Restaurant")
                .customerRating(5)
                .distance(5.0)
                .price(12.0)
                .cuisine(cChinese)
                .build();

        Restaurant r4 = Restaurant.builder()
                .id(4L)
                .name("Cheap Eats")
                .customerRating(3)
                .distance(0.8)
                .price(9.0)
                .cuisine(cAmerican)
                .build();

        // repository returns all restaurants
        when(repository.findAll()).thenReturn(Arrays.asList(r1, r2, r3, r4));

        // search: min rating = 3, max price = 15 (should include all except none)
        SearchCriteria criteria = new SearchCriteria();
        criteria.setMinCustomerRating(3);
        criteria.setMaxPrice(15.0);

        List<Restaurant> results = service.search(criteria);

        // Expect top 3-4 results ordered by distance asc, rating desc, price asc.
        // Distances: r4(0.8), r1(1.0), r2(1.0), r3(5.0)
        // For r1 vs r2 (same distance), r1 rating 4 > r2 rating 3 => r1 before r2.
        assertNotNull(results);
        assertTrue(results.size() <= 5);
        assertEquals(r4.getName(), results.get(0).getName(), "Closest (Cheap Eats) should be first");
        assertEquals(r1.getName(), results.get(1).getName(), "Mcdonald's should be second");
        assertEquals(r2.getName(), results.get(2).getName(), "KFC should be third");
        assertEquals(r3.getName(), results.get(3).getName(), "Far Away should be last");
    }

    @Test
    void testFilterByNameAndCuisinePartialMatch() {
        Cuisine cThai = Cuisine.builder().id(11L).name("Thai").build();

        Restaurant a = Restaurant.builder()
                .id(1L)
                .name("DeliciousThaiKitchen")
                .customerRating(5)
                .distance(2.0)
                .price(20.0)
                .cuisine(cThai)
                .build();

        Restaurant b = Restaurant.builder()
                .id(2L)
                .name("SomeOther")
                .customerRating(4)
                .distance(1.5)
                .price(15.0)
                .cuisine(cThai)
                .build();

        when(repository.findAll()).thenReturn(Arrays.asList(a, b));

        SearchCriteria criteria = new SearchCriteria();
        criteria.setName("Delicious");    // partial match on name
        criteria.setCuisine("Thai");      // partial/exact match on cuisine name

        List<Restaurant> results = service.search(criteria);

        assertEquals(1, results.size(), "Only DeliciousThaiKitchen should match both name and cuisine");
        assertEquals("DeliciousThaiKitchen", results.get(0).getName());
    }

    @Test
    void testValidationThrowsForInvalidInputs() {
        when(repository.findAll()).thenReturn(List.of()); // empty repo

        SearchCriteria badRating = new SearchCriteria();
        badRating.setMinCustomerRating(0);
        IllegalArgumentException e1 = assertThrows(IllegalArgumentException.class, () -> service.search(badRating));
        assertTrue(e1.getMessage().contains("customerRating"));

        SearchCriteria badDistance = new SearchCriteria();
        badDistance.setMaxDistance(20.0);
        IllegalArgumentException e2 = assertThrows(IllegalArgumentException.class, () -> service.search(badDistance));
        assertTrue(e2.getMessage().contains("distance"));

        SearchCriteria badPrice = new SearchCriteria();
        badPrice.setMaxPrice(5.0);
        IllegalArgumentException e3 = assertThrows(IllegalArgumentException.class, () -> service.search(badPrice));
        assertTrue(e3.getMessage().contains("price"));
    }
}