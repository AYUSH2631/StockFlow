package com.yourname.inventory.exception;

public class InventoryItemNotFoundException extends RuntimeException {

	public InventoryItemNotFoundException(Long id) {
		super("Inventory item with ID %d was not found".formatted(id));
	}
}
