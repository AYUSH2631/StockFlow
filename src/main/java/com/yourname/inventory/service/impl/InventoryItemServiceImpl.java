package com.yourname.inventory.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yourname.inventory.dto.request.CreateInventoryItemRequest;
import com.yourname.inventory.dto.request.UpdateInventoryItemRequest;
import com.yourname.inventory.dto.response.InventoryItemResponse;
import com.yourname.inventory.entity.InventoryItem;
import com.yourname.inventory.exception.DuplicateInventoryItemException;
import com.yourname.inventory.exception.InventoryItemNotFoundException;
import com.yourname.inventory.repository.InventoryItemRepository;
import com.yourname.inventory.service.InventoryItemService;

@Service
@Transactional
public class InventoryItemServiceImpl implements InventoryItemService {

	private final InventoryItemRepository inventoryItemRepository;

	public InventoryItemServiceImpl(InventoryItemRepository inventoryItemRepository) {
		this.inventoryItemRepository = inventoryItemRepository;
	}

	@Override
	public InventoryItemResponse create(CreateInventoryItemRequest request) {
		String sku = normalize(request.sku());
		String locationId = normalize(request.locationId());
		if (inventoryItemRepository.existsBySkuAndLocationId(sku, locationId)) {
			throw new DuplicateInventoryItemException(sku, locationId);
		}

		InventoryItem item = inventoryItemRepository.saveAndFlush(new InventoryItem(sku, locationId));
		return InventoryItemResponse.from(item);
	}

	@Override
	@Transactional(readOnly = true)
	public InventoryItemResponse getById(Long id) {
		return InventoryItemResponse.from(findById(id));
	}

	@Override
	@Transactional(readOnly = true)
	public Page<InventoryItemResponse> list(Pageable pageable) {
		return inventoryItemRepository.findAll(pageable).map(InventoryItemResponse::from);
	}

	@Override
	public InventoryItemResponse update(Long id, UpdateInventoryItemRequest request) {
		InventoryItem item = findById(id);
		String sku = normalize(request.sku());
		String locationId = normalize(request.locationId());
		if (inventoryItemRepository.existsBySkuAndLocationIdAndIdNot(sku, locationId, id)) {
			throw new DuplicateInventoryItemException(sku, locationId);
		}

		item.updateIdentity(sku, locationId);
		return InventoryItemResponse.from(inventoryItemRepository.saveAndFlush(item));
	}

	private InventoryItem findById(Long id) {
		return inventoryItemRepository.findById(id)
				.orElseThrow(() -> new InventoryItemNotFoundException(id));
	}

	private String normalize(String value) {
		return value.trim();
	}
}
