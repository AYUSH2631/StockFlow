package com.yourname.inventory.exception;

public class DuplicateInventoryItemException extends RuntimeException {

	public DuplicateInventoryItemException(String sku, String locationId) {
		super("Inventory already exists for SKU '%s' at location '%s'".formatted(sku, locationId));
	}
}
