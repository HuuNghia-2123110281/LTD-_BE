package com.nghiashop.ecome_backend.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private Long price;
    private String image;
    private Double rating;
    private Integer sold;
    private Integer stock;
    private CategoryResponse category;
}
