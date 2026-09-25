package id.com.flare.kelarus.utilities.general.exception;

import id.com.flare.kelarus.utilities.general.constant.ErrorCodeGlobalEnum;
import id.com.flare.kelarus.utilities.general.dto.ErrorResponse;
import id.com.flare.kelarus.utilities.general.dto.ValidationSubError;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex) {
		return response(HttpStatus.BAD_REQUEST, ErrorCodeGlobalEnum.INVALID_REQUEST, null,
				ex.getBindingResult().getFieldErrors().stream()
						.map(error -> new ValidationSubError(error.getField(), error.getDefaultMessage()))
						.collect(Collectors.toList()),
				ex);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	ResponseEntity<ErrorResponse> constraintViolation(ConstraintViolationException ex) {
		return response(HttpStatus.BAD_REQUEST, ErrorCodeGlobalEnum.INVALID_REQUEST, null,
				ex.getConstraintViolations().stream()
						.map(error -> new ValidationSubError(error.getPropertyPath().toString(), error.getMessage()))
						.collect(Collectors.toList()),
				ex);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	ResponseEntity<ErrorResponse> malformed(HttpMessageNotReadableException ex) {
		return response(HttpStatus.BAD_REQUEST, ErrorCodeGlobalEnum.MALFORMED_REQUEST, null, List.of(), ex);
	}

	@ExceptionHandler(BusinessException.class)
	ResponseEntity<ErrorResponse> business(BusinessException ex) {
		return response(ex.getStatus(), ex.getErrorCode(), ex.getMessage(), List.of(), ex);
	}

	@ExceptionHandler(EntityNotFoundException.class)
	ResponseEntity<ErrorResponse> notFound(EntityNotFoundException ex) {
		return response(HttpStatus.NOT_FOUND, ErrorCodeGlobalEnum.RESOURCE_NOT_FOUND, null, List.of(), ex);
	}

	@ExceptionHandler(AccessDeniedException.class)
	ResponseEntity<ErrorResponse> denied(AccessDeniedException ex) {
		return response(HttpStatus.FORBIDDEN, ErrorCodeGlobalEnum.ACCESS_DENIED, null, List.of(), ex);
	}

	@ExceptionHandler(AuthenticationException.class)
	ResponseEntity<ErrorResponse> unauthenticated(AuthenticationException ex) {
		return response(HttpStatus.UNAUTHORIZED, ErrorCodeGlobalEnum.UNAUTHENTICATED, null, List.of(), ex);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	ResponseEntity<ErrorResponse> conflict(DataIntegrityViolationException ex) {
		return response(HttpStatus.CONFLICT, ErrorCodeGlobalEnum.CONFLICT, null, List.of(), ex);
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<ErrorResponse> unexpected(Exception ex) {
		return response(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCodeGlobalEnum.INTERNAL_ERROR, null, List.of(), ex);
	}

	private ResponseEntity<ErrorResponse> response(HttpStatus status, ExceptionEnum errorCode, String message,
			List<ValidationSubError> subErrors, Exception ex) {
		String requestId = UUID.randomUUID().toString();
		if (status.is5xxServerError())
			LOG.error("requestId={} errorCode={} status={}", requestId, errorCode.getCode(), status.value(), ex);
		else
			LOG.warn("requestId={} errorCode={} status={}", requestId, errorCode.getCode(), status.value());
		return ResponseEntity.status(status)
				.body(new ErrorResponse(requestId, errorCode.getCode(), errorCode.getTitle(),
						message == null ? errorCode.getDescription() : message, status.value(), Instant.now(),
						subErrors));
	}

}
