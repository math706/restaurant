package com.org.restaurant.controller;

import com.org.restaurant.dto.SearchCriteria;
import com.org.restaurant.model.Restaurant;
import com.org.restaurant.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@Tag(name = "Restaurants", description = "Search restaurants")
public class SearchController {

    private final SearchService svc;

    public SearchController(SearchService svc) {
        this.svc = svc;
    }

    @GetMapping("/search")
    @Operation(summary = "Search restaurants by criteria",
            description = "Provide up to five parameters: name, customerRating, distance, price, cuisine. Returns up to 5 best matches.")
    public ResponseEntity<?> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false, name = "customerRating") Integer customerRating,
            @RequestParam(required = false, name = "distance") Double distance,
            @RequestParam(required = false, name = "price") Double price,
            @RequestParam(required = false) String cuisine
    ) {
        try {
            SearchCriteria c = new SearchCriteria();
            c.setName(name);
            c.setMinCustomerRating(customerRating);
            c.setMaxDistance(distance);
            c.setMaxPrice(price);
            c.setCuisine(cuisine);

            List<Restaurant> result = svc.search(c);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("internal error: " + ex.getMessage());
        }
    }
}