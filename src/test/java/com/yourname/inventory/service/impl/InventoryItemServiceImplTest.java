package com.yourname.inventory.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.yourname.inventory.dto.request.CreateInventoryItemRequest;
import com.yourname.inventory.dto.request.UpdateInventoryItemRequest;
import com.yourname.inventory.dto.response.InventoryItemResponse;
import com.yourname.inventory.entity.InventoryItem;
import com.yourname.inventory.exception.DuplicateInventoryItemException;
import com.yourname.inventory.exception.InventoryItemNotFoundException;
import com.yourname.inventory.repository.InventoryItemRepository;

@ExtendWith(MockitoExtension.class)
class InventoryItemServiceImplTest {

	@Mock
	private InventoryItemRepository repository;

	@InjectMocks
	private InventoryItemServiceImpl service;

	@Test
	void createsItemWithZeroQuantitiesAndTrimmedIdentifiers() {
		when(repository.saveAndFlush(any(InventoryItem.class))).thenAnswer(invocation -> {
			return invocation.getArgument(0);
		});

		InventoryItemResponse response = service.create(new CreateInventoryItemRequest(" SKU-101 ", " WH-1 "));

		assertEquals("SKU-101", response.sku());
		assertEquals("WH-1", response.locationId());
		assertEquals(0, response.quantityOnHand());
		assertEquals(0, response.quantityReserved());
		assertEquals(0, response.availableQuantity());
	}

	@Test
	void rejectsDuplicateSkuAtSameLocation() {
		when(repository.existsBySkuAndLocationId("SKU-101", "WH-1")).thenReturn(true);

		assertThrows(
				DuplicateInventoryItemException.class,
				() -> service.create(new CreateInventoryItemRequest("SKU-101", "WH-1")));

		verify(repository, never()).saveAndFlush(any(InventoryItem.class));
	}

	@Test
	void reportsMissingItem() {
		when(repository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(InventoryItemNotFoundException.class, () -> service.getById(99L));
	}

	@Test
	void mapsPagedItemsToResponses() {
		InventoryItem firstItem = new InventoryItem("SKU-101", "WH-1");
		InventoryItem secondItem = new InventoryItem("SKU-202", "WH-2");
		PageRequest pageRequest = PageRequest.of(1, 5);
		when(repository.findAll(pageRequest))
				.thenReturn(new PageImpl<>(java.util.List.of(firstItem, secondItem), pageRequest, 7));

		var page = service.list(pageRequest);

		assertEquals(1, page.getNumber());
		assertEquals(5, page.getSize());
		assertEquals(7, page.getTotalElements());
		assertEquals("SKU-101", page.getContent().get(0).sku());
	}

	@Test
	void rejectsUpdateThatDuplicatesAnotherItemsSkuAndLocation() {
		InventoryItem item = new InventoryItem("SKU-101", "WH-1");
		when(repository.findById(1L)).thenReturn(Optional.of(item));
		when(repository.existsBySkuAndLocationIdAndIdNot("SKU-202", "WH-2", 1L)).thenReturn(true);

		assertThrows(
				DuplicateInventoryItemException.class,
				() -> service.update(1L, new UpdateInventoryItemRequest("SKU-202", "WH-2")));
		verify(repository, never()).saveAndFlush(any(InventoryItem.class));
	}

}
