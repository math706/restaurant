package com.org.restaurant.service;

import com.org.restaurant.dto.SearchCriteria;
import com.org.restaurant.model.Restaurant;
import com.org.restaurant.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class SearchService {
    private final RestaurantRepository repository;

    public SearchService(RestaurantRepository repository) {
        this.repository = repository;
    }

    public List<Restaurant> search(SearchCriteria c) {
        validate(c);

        List<Restaurant> filtered = repository.findAll().stream()
                .filter(r -> matchesName(r, c.getName()))
                .filter(r -> matchesRating(r, c.getMinCustomerRating()))
                .filter(r -> matchesDistance(r, c.getMaxDistance()))
                .filter(r -> matchesPrice(r, c.getMaxPrice()))
                .filter(r -> matchesCuisine(r, c.getCuisine())).toList();

        return filtered.stream()
                .sorted(Comparator.comparingDouble(Restaurant::getDistance)
                        .thenComparing(Comparator.comparingInt(Restaurant::getCustomerRating).reversed())
                        .thenComparingDouble(Restaurant::getPrice))
                .limit(5)
                .collect(Collectors.toList());
    }

    private void validate(SearchCriteria c) {
        if (c == null) return;
        if (c.getMinCustomerRating() != null) {
            int r = c.getMinCustomerRating();
            if (r < 1 || r > 5) throw new IllegalArgumentException("customerRating must be between 1 and 5");
        }
        if (c.getMaxDistance() != null) {
            double d = c.getMaxDistance();
            if (d < 1 || d > 10) throw new IllegalArgumentException("distance must be between 1 and 10 miles");
        }
        if (c.getMaxPrice() != null) {
            double p = c.getMaxPrice();
            if (p < 10 || p > 50) throw new IllegalArgumentException("price must be between $10 and $50");
        }
    }

    private boolean matchesName(Restaurant r, String name) {
        if (!StringUtils.hasText(name)) return true;
        return containsIgnoreCase(r.getName(), name);
    }

    private boolean matchesRating(Restaurant r, Integer minRating) {
        if (minRating == null) return true;
        return r.getCustomerRating() >= minRating;
    }

    private boolean matchesDistance(Restaurant r, Double maxDistance) {
        if (maxDistance == null) return true;
        return r.getDistance() <= maxDistance;
    }

    private boolean matchesPrice(Restaurant r, Double maxPrice) {
        if (maxPrice == null) return true;
        return r.getPrice() <= maxPrice;
    }

    private boolean matchesCuisine(Restaurant r, String cuisine) {
        if (!StringUtils.hasText(cuisine)) return true;
        if (r.getCuisine() == null || r.getCuisine().getName() == null) return false;
        return containsIgnoreCase(r.getCuisine().getName(), cuisine);
    }

    private boolean containsIgnoreCase(String text, String fragment) {
        if (text == null) return false;
        return text.toLowerCase(Locale.ROOT).contains(fragment.toLowerCase(Locale.ROOT));
    }
}
