package com.yourname.inventory.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
		name = "inventory_items",
		uniqueConstraints = @UniqueConstraint(
				name = "uq_inventory_items_sku_location",
				columnNames = {"sku", "location_id"}))
public class InventoryItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "sku", nullable = false, length = 100)
	private String sku;

	@Column(name = "location_id", nullable = false, length = 100)
	private String locationId;

	@Column(name = "quantity_on_hand", nullable = false)
	private int quantityOnHand;

	@Column(name = "quantity_reserved", nullable = false)
	private int quantityReserved;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected InventoryItem() {
	}

	public InventoryItem(String sku, String locationId) {
		this.sku = sku;
		this.locationId = locationId;
		this.quantityOnHand = 0;
		this.quantityReserved = 0;
	}

	public void updateIdentity(String sku, String locationId) {
		this.sku = sku;
		this.locationId = locationId;
	}

	public Long getId() {
		return id;
	}

	public String getSku() {
		return sku;
	}

	public String getLocationId() {
		return locationId;
	}

	public int getQuantityOnHand() {
		return quantityOnHand;
	}

	public int getQuantityReserved() {
		return quantityReserved;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
