package id.com.flare.kelarus.utilities.general.constant;

import lombok.Getter;
import id.com.flare.kelarus.utilities.general.exception.ExceptionEnum;

@Getter
public enum ErrorCodeGlobalEnum implements ExceptionEnum {

	INVALID_REQUEST("INVALID_REQUEST", "Invalid request."),
	MALFORMED_REQUEST("MALFORMED_REQUEST", "Request body is invalid."),
	RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "Requested resource was not found."),
	ACCESS_DENIED("ACCESS_DENIED", "Access is denied."),
	UNAUTHENTICATED("UNAUTHENTICATED", "Authentication is required."),
	CONFLICT("CONFLICT", "Request conflicts with existing data."),
	INVALID_PASSCODE_VERIFICATION_REQUEST_TYPE("ACC0001", "Incorrect passcode."),
	INTERNAL_ERROR("INTERNAL_ERROR", "Unable to complete the request.");

	private final String code;

	private final String description;

	ErrorCodeGlobalEnum(String code, String description) {
		this.code = code;
		this.description = description;
	}

}
