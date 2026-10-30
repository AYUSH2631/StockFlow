package com.yourname.inventory.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.yourname.inventory.dto.request.CreateInventoryItemRequest;
import com.yourname.inventory.dto.request.UpdateInventoryItemRequest;
import com.yourname.inventory.dto.response.InventoryItemResponse;

public interface InventoryItemService {

	InventoryItemResponse create(CreateInventoryItemRequest request);

	InventoryItemResponse getById(Long id);

	Page<InventoryItemResponse> list(Pageable pageable);

	InventoryItemResponse update(Long id, UpdateInventoryItemRequest request);
}
