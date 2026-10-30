package com.yourname.inventory.controller;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.yourname.inventory.dto.request.CreateInventoryItemRequest;
import com.yourname.inventory.dto.request.UpdateInventoryItemRequest;
import com.yourname.inventory.dto.response.InventoryItemResponse;
import com.yourname.inventory.service.InventoryItemService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@Validated
@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryItemController {

	private final InventoryItemService inventoryItemService;

	public InventoryItemController(InventoryItemService inventoryItemService) {
		this.inventoryItemService = inventoryItemService;
	}

	@PostMapping
	public ResponseEntity<InventoryItemResponse> create(@Valid @RequestBody CreateInventoryItemRequest request) {
		InventoryItemResponse response = inventoryItemService.create(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
				.path("/{id}")
				.buildAndExpand(response.id())
				.toUri();
		return ResponseEntity.created(location).body(response);
	}

	@GetMapping("/{id}")
	public InventoryItemResponse getById(@PathVariable @Positive Long id) {
		return inventoryItemService.getById(id);
	}

	@GetMapping
	public Page<InventoryItemResponse> list(@PageableDefault(size = 20) Pageable pageable) {
		return inventoryItemService.list(pageable);
	}

	@PutMapping("/{id}")
	public InventoryItemResponse update(
			@PathVariable @Positive Long id,
			@Valid @RequestBody UpdateInventoryItemRequest request) {
		return inventoryItemService.update(id, request);
	}
}
