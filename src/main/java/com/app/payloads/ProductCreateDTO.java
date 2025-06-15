package com.app.payloads;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductCreateDTO {
    private String productName;
    private String description;
    private double price;
    private double discount;
    private double specialPrice;
    private String image;
    private String brand;
    private String model;
    private String type;
    private String color;
    private String warranty;
    private int quantity;
}