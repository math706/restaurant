package com.org.restaurant.config;

import com.org.restaurant.model.Cuisine;
import com.org.restaurant.model.Restaurant;
import com.org.restaurant.repository.CuisineRepository;
import com.org.restaurant.repository.RestaurantRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

@Component
public class DataLoader implements ApplicationRunner {

    private final CuisineRepository cuisineRepository;
    private final RestaurantRepository restaurantRepository;

    @Value("${app.csv.cuisines:data/cuisines.csv}")
    private String cuisinesCsv;

    @Value("${app.csv.restaurants:data/restaurants.csv}")
    private String restaurantsCsv;

    public DataLoader(CuisineRepository cuisineRepository, RestaurantRepository restaurantRepository) {
        this.cuisineRepository = cuisineRepository;
        this.restaurantRepository = restaurantRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (restaurantRepository.count() > 0 || cuisineRepository.count() > 0) {
            System.out.println("Database already contains data; skipping CSV load.");
            return;
        }

        ClassPathResource cuisineResource = new ClassPathResource(cuisinesCsv);
        if (cuisineResource.exists()) {
            try (Reader r = new InputStreamReader(cuisineResource.getInputStream())) {
                Iterable<CSVRecord> records = CSVFormat.DEFAULT
                        .withFirstRecordAsHeader()
                        .withTrim()
                        .parse(r);

                int cuisinesLoaded = 0;
                for (CSVRecord rec : records) {
                    if (!rec.isMapped("id") || !rec.isMapped("name")) {
                        System.err.println("Cuisines CSV missing expected headers 'id,name'. Skipping cuisines load.");
                        break;
                    }
                    String idS = rec.get("id").trim();
                    String name = rec.get("name").trim();
                    try {
                        Long id = Long.parseLong(idS);
                        Cuisine c = Cuisine.builder()
                                .id(id)
                                .name(name)
                                .build();
                        cuisineRepository.save(c);
                        cuisinesLoaded++;
                    } catch (NumberFormatException nfe) {
                        System.err.println("Skipping cuisine row due id parse error: " + idS);
                    }
                }
                System.out.println("Loaded cuisines: " + cuisineRepository.count() + " (attempted " + cuisinesLoaded + ")");
            } catch (Exception ex) {
                System.err.println("Failed to load cuisines CSV " + cuisinesCsv + " : " + ex.getMessage());
            }
        } else {
            System.err.println("Cuisines CSV not found at '" + cuisinesCsv + "'. Proceeding without cuisines.");
        }

        ClassPathResource restResource = new ClassPathResource(restaurantsCsv);
        if (!restResource.exists()) {
            System.err.println("Restaurants CSV not found at '" + restaurantsCsv + "'. Skipping restaurant load.");
            return;
        }

        int loaded = 0;
        Set<String> missingCuisineIds = new HashSet<>();
        try (Reader r = new InputStreamReader(restResource.getInputStream())) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .withTrim()
                    .parse(r);

            for (CSVRecord rec : records) {
                try {
                    if (!rec.isMapped("name") || !rec.isMapped("customer_rating") ||
                            !rec.isMapped("distance") || !rec.isMapped("price") || !rec.isMapped("cuisine_id")) {
                        System.err.println("Restaurants CSV missing required headers. Skipping row: " + rec.toString());
                        continue;
                    }

                    String name = rec.get("name").trim();
                    String ratingS = rec.get("customer_rating").trim();
                    String distanceS = rec.get("distance").trim();
                    String priceS = rec.get("price").trim();
                    String cuisineIdS = rec.get("cuisine_id").trim();

                    int rating = Integer.parseInt(ratingS);
                    double distance = Double.parseDouble(distanceS);
                    double price = Double.parseDouble(priceS);

                    Cuisine cuisine = null;
                    try {
                        Long cuisineId = Long.parseLong(cuisineIdS);
                        cuisine = cuisineRepository.findById(cuisineId).orElse(null);
                        if (cuisine == null) {
                            missingCuisineIds.add(cuisineIdS);
                        }
                    } catch (NumberFormatException nfe) {
                        missingCuisineIds.add(cuisineIdS);
                    }

                    if (cuisine == null) {
                        cuisine = cuisineRepository.findAll().stream()
                                .filter(c -> "Unknown".equalsIgnoreCase(c.getName()))
                                .findFirst()
                                .orElseGet(() -> {
                                    Cuisine unknown = Cuisine.builder().id(null).name("Unknown").build();
                                    long negId = -1L;
                                    while (cuisineRepository.existsById(negId)) negId--;
                                    unknown.setId(negId);
                                    return cuisineRepository.save(unknown);
                                });
                    }

                    Restaurant rest = Restaurant.builder()
                            .name(name)
                            .customerRating(rating)
                            .distance(distance)
                            .price(price)
                            .cuisine(cuisine)
                            .build();

                    restaurantRepository.save(rest);
                    loaded++;
                } catch (NumberFormatException nfe) {
                    System.err.println("Skipping restaurant row due parse number error: " + nfe.getMessage() + " Row: " + rec.toString());
                } catch (Exception e) {
                    System.err.println("Skipping restaurant row due unexpected error: " + e.getMessage() + " Row: " + rec.toString());
                }
            }
            if (!missingCuisineIds.isEmpty()) {
                System.err.println("Warning: some cuisine_id values were not found in cuisines CSV (examples): " + missingCuisineIds.stream().limit(5).toList());
            }
            System.out.println("Loaded restaurants from " + restaurantsCsv + ". Count = " + loaded);
        } catch (Exception ex) {
            System.err.println("Failed to load CSV " + restaurantsCsv + " : " + ex.getMessage());
        }
    }
}