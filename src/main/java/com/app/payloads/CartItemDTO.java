package com.app.payloads;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
	
	private Long cartItemId;
	private Long parentCartId; // or omit if not needed on frontend

	private ProductDTO product;
	private Integer quantity;
	private double discount;
	private double productSpecialPrice;;

}
