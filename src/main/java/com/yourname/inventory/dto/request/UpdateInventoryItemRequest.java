package com.yourname.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateInventoryItemRequest(
		@NotBlank @Size(max = 100) String sku,
		@NotBlank @Size(max = 100) String locationId) {
}
