package com.app.entites;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Data
@Table(name = "products")
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @NotBlank
    @Size(min = 3)
    private String productName;

    @NotBlank
    private String description;

    private double price;

    private double discount;

    private double specialPrice;

    private String image;
	private int quantity;


    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    // Watch-specific fields
    private String brand;
    private String model;
    private String type;       // Analog, Digital, Smart
    private String color;
    private String warranty;
    


}
