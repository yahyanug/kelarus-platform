package id.com.flare.kelarus.utilities.general.dto;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(String requestId, String errorCode, String title, String message, int status,
		Instant timestamp, List<ValidationSubError> subErrors) {
}
