package com.org.restaurant;

import com.org.restaurant.model.Cuisine;
import com.org.restaurant.model.Restaurant;
import com.org.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @BeforeEach
    void beforeEach() {
        restaurantRepository.deleteAll();

        Cuisine c = Cuisine.builder().id(1L).name("American").build();

        Restaurant r1 = Restaurant.builder()
                .name("Mcdonald's")
                .customerRating(4)
                .distance(1.0)
                .price(10.0)
                .cuisine(c)
                .build();

        Restaurant r2 = Restaurant.builder()
                .name("KFC")
                .customerRating(3)
                .distance(1.0)
                .price(8.0)
                .cuisine(c)
                .build();

        restaurantRepository.save(r1);
        restaurantRepository.save(r2);
    }

    @Test
    void searchEndpointRequiresAuthAndReturnsResults() throws Exception {
        mockMvc.perform(get("/api/restaurants/search")
                        .param("customerRating", "3")
                        .param("price", "15")
                        .with(SecurityMockMvcRequestPostProcessors.httpBasic("admin", "admin"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].name", not(emptyString())));
    }

    @Test
    void searchEndpointUnauthorizedWhenNoCredentials() throws Exception {
        mockMvc.perform(get("/api/restaurants/search")
                        .param("customerRating", "3")
                        .param("price", "15")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}