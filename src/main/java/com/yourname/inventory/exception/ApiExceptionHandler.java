package com.yourname.inventory.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import com.yourname.inventory.dto.response.ApiErrorResponse;

@RestControllerAdvice
public class ApiExceptionHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

	@ExceptionHandler(InventoryItemNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleNotFound(
			InventoryItemNotFoundException exception,
			WebRequest request) {
		return error(HttpStatus.NOT_FOUND, exception.getMessage(), request, Map.of());
	}

	@ExceptionHandler(DuplicateInventoryItemException.class)
	public ResponseEntity<ApiErrorResponse> handleConflict(
			DuplicateInventoryItemException exception,
			WebRequest request) {
		return error(HttpStatus.CONFLICT, exception.getMessage(), request, Map.of());
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiErrorResponse> handleDatabaseConflict(
			DataIntegrityViolationException exception,
			WebRequest request) {
		return error(
				HttpStatus.CONFLICT,
				"Inventory already exists for this SKU at this location",
				request,
				Map.of());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidBody(
			MethodArgumentNotValidException exception,
			WebRequest request) {
		Map<String, String> validationErrors = new LinkedHashMap<>();
		exception.getBindingResult().getFieldErrors()
				.forEach(fieldError -> validationErrors.putIfAbsent(
						fieldError.getField(), fieldError.getDefaultMessage()));
		return error(HttpStatus.BAD_REQUEST, "Request validation failed", request, validationErrors);
	}

	@ExceptionHandler({HandlerMethodValidationException.class, HttpMessageNotReadableException.class})
	public ResponseEntity<ApiErrorResponse> handleInvalidRequest(Exception exception, WebRequest request) {
		return error(HttpStatus.BAD_REQUEST, "Request is invalid", request, Map.of());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception, WebRequest request) {
		LOGGER.error("Unexpected error while handling request", exception);
		return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request, Map.of());
	}

	private ResponseEntity<ApiErrorResponse> error(
			HttpStatus status,
			String message,
			WebRequest request,
			Map<String, String> validationErrors) {
		ApiErrorResponse body = new ApiErrorResponse(
				Instant.now(),
				status.value(),
				status.getReasonPhrase(),
				message,
				request.getDescription(false).replace("uri=", ""),
				validationErrors);
		return ResponseEntity.status(status).body(body);
	}
}
