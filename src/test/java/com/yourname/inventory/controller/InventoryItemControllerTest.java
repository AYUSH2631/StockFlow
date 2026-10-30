package com.yourname.inventory.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.yourname.inventory.dto.request.CreateInventoryItemRequest;
import com.yourname.inventory.dto.request.UpdateInventoryItemRequest;
import com.yourname.inventory.dto.response.InventoryItemResponse;
import com.yourname.inventory.exception.ApiExceptionHandler;
import com.yourname.inventory.exception.DuplicateInventoryItemException;
import com.yourname.inventory.exception.InventoryItemNotFoundException;
import com.yourname.inventory.service.InventoryItemService;

import static org.mockito.Mockito.mock;

class InventoryItemControllerTest {

	private InventoryItemService service;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		service = mock(InventoryItemService.class);
		mockMvc = MockMvcBuilders.standaloneSetup(new InventoryItemController(service))
				.setControllerAdvice(new ApiExceptionHandler())
				.setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
				.build();
	}

	@Test
	void createsItemAndReturnsCreatedLocation() throws Exception {
		when(service.create(any(CreateInventoryItemRequest.class))).thenReturn(response(5L));

		mockMvc.perform(post("/api/v1/inventory")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"sku\":\"SKU-101\",\"locationId\":\"WH-1\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(5))
				.andExpect(jsonPath("$.availableQuantity").value(0))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
						.string("Location", "http://localhost/api/v1/inventory/5"));
	}

	@Test
	void rejectsBlankSkuWithValidationError() throws Exception {
		mockMvc.perform(post("/api/v1/inventory")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"sku\":\" \",\"locationId\":\"WH-1\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.validationErrors.sku").exists());
	}

	@Test
	void returnsConflictForDuplicateInventoryPair() throws Exception {
		when(service.create(any(CreateInventoryItemRequest.class)))
				.thenThrow(new DuplicateInventoryItemException("SKU-101", "WH-1"));

		mockMvc.perform(post("/api/v1/inventory")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"sku\":\"SKU-101\",\"locationId\":\"WH-1\"}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409));
	}

	@Test
	void returnsConflictWhenDatabaseRejectsConcurrentDuplicate() throws Exception {
		when(service.create(any(CreateInventoryItemRequest.class)))
				.thenThrow(new DataIntegrityViolationException("duplicate key"));

		mockMvc.perform(post("/api/v1/inventory")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"sku\":\"SKU-101\",\"locationId\":\"WH-1\"}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value(
						"Inventory already exists for this SKU at this location"));
	}

	@Test
	void returnsNotFoundForMissingItem() throws Exception {
		when(service.getById(88L)).thenThrow(new InventoryItemNotFoundException(88L));

		mockMvc.perform(get("/api/v1/inventory/88"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	@Test
	void passesRequestedPaginationToService() throws Exception {
		when(service.list(any())).thenReturn(new PageImpl<>(
				List.of(response(5L), response(6L)), PageRequest.of(1, 5), 7));

		mockMvc.perform(get("/api/v1/inventory").param("page", "1").param("size", "5"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.number").value(1))
				.andExpect(jsonPath("$.size").value(5))
				.andExpect(jsonPath("$.totalElements").value(7))
				.andExpect(jsonPath("$.content[0].sku").value("SKU-101"));

		verify(service).list(eq(PageRequest.of(1, 5)));
	}

	@Test
	void updatesItemAndReturnsOk() throws Exception {
		when(service.update(eq(5L), any(UpdateInventoryItemRequest.class))).thenReturn(response(5L));

		mockMvc.perform(put("/api/v1/inventory/5")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"sku\":\"SKU-101\",\"locationId\":\"WH-1\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(5));
	}

	private InventoryItemResponse response(Long id) {
		Instant timestamp = Instant.parse("2026-01-01T00:00:00Z");
		return new InventoryItemResponse(id, "SKU-101", "WH-1", 0, 0, 0, timestamp, timestamp);
	}
}
