package com.org.restaurant.dto;

import lombok.Data;

@Data
public class SearchCriteria {
    private String name;
    private Integer minCustomerRating;
    private Double maxDistance;
    private Double maxPrice;
    private String cuisine;
}
