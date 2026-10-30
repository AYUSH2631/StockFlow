package com.yourname.inventory.dto.response;

import java.time.Instant;

import com.yourname.inventory.entity.InventoryItem;

public record InventoryItemResponse(
		Long id,
		String sku,
		String locationId,
		int quantityOnHand,
		int quantityReserved,
		int availableQuantity,
		Instant createdAt,
		Instant updatedAt) {

	public static InventoryItemResponse from(InventoryItem item) {
		return new InventoryItemResponse(
				item.getId(),
				item.getSku(),
				item.getLocationId(),
				item.getQuantityOnHand(),
				item.getQuantityReserved(),
				item.getQuantityOnHand() - item.getQuantityReserved(),
				item.getCreatedAt(),
				item.getUpdatedAt());
	}
}
