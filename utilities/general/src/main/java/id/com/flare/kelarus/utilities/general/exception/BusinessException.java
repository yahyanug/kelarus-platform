package id.com.flare.kelarus.utilities.general.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {

	private final ExceptionEnum errorCode;

	private final HttpStatus status;

	public BusinessException(ExceptionEnum errorCode, HttpStatus status) {
		super(errorCode.getDescription());
		this.errorCode = errorCode;
		this.status = status;
	}

	public BusinessException(ExceptionEnum errorCode, HttpStatus status, String message) {
		super(message);
		this.errorCode = errorCode;
		this.status = status;
	}

	public BusinessException(ExceptionEnum errorCode, HttpStatus status, Throwable cause) {
		super(errorCode.getDescription(), cause);
		this.errorCode = errorCode;
		this.status = status;
	}

}
