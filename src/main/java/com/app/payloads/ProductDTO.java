package com.app.payloads;




import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {
    private Long productId;
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
    @Schema(hidden = true)
private CategoryDTO category;
   
	private int quantity;
    // ...existing code...
// ...existing code...


	
}
