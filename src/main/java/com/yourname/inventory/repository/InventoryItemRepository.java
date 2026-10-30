package com.yourname.inventory.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yourname.inventory.entity.InventoryItem;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

	Optional<InventoryItem> findBySkuAndLocationId(String sku, String locationId);

	boolean existsBySkuAndLocationId(String sku, String locationId);

	boolean existsBySkuAndLocationIdAndIdNot(String sku, String locationId, Long id);
}
