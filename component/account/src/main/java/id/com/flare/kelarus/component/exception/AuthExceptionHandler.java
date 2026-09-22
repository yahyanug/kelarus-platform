package id.com.flare.kelarus.component.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@RestControllerAdvice
public class AuthExceptionHandler {

	private static final Logger LOG = LoggerFactory.getLogger(AuthExceptionHandler.class);

	@ExceptionHandler(AuthException.class)
	ResponseEntity<ApiError> authentication(AuthException ex) {
		return ResponseEntity.status(ex.getError().status())
				.body(new ApiError(ex.getError().name(), ex.getError().message()));
	}

	@ExceptionHandler({ MethodArgumentNotValidException.class, HttpMessageNotReadableException.class,
			ConstraintViolationException.class, HandlerMethodValidationException.class })
	ResponseEntity<ApiError> validation(Exception ex) {
		// Never echo rejected values: they may contain passwords or tokens.
		return ResponseEntity.badRequest().body(new ApiError("INVALID_REQUEST", "Invalid request."));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	ResponseEntity<ApiError> integrity(DataIntegrityViolationException ex) {
		for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
			if (cause instanceof org.hibernate.exception.ConstraintViolationException constraint
					&& "uk_users_email".equalsIgnoreCase(constraint.getConstraintName())) {
				return authentication(AuthError.EMAIL_ALREADY_REGISTERED.exception());
			}
		}
		LOG.error("Account persistence constraint violation; database details omitted to protect identity data.");
		return ResponseEntity.internalServerError()
				.body(new ApiError("INTERNAL_ERROR", "Unable to complete the request."));
	}

}
