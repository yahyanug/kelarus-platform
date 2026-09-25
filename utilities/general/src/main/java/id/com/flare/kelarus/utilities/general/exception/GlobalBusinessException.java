package id.com.flare.kelarus.utilities.general.exception;

import id.com.flare.kelarus.utilities.general.constant.ErrorCodeGlobalEnum;
import org.springframework.http.HttpStatus;

public class GlobalBusinessException extends BusinessException {

	public GlobalBusinessException(ErrorCodeGlobalEnum errorCode) {
		super(errorCode, HttpStatus.BAD_REQUEST);
	}

	public GlobalBusinessException(ErrorCodeGlobalEnum errorCode, String message) {
		super(errorCode, HttpStatus.BAD_REQUEST, message);
	}

	public GlobalBusinessException(ErrorCodeGlobalEnum errorCode, Throwable cause) {
		super(errorCode, HttpStatus.BAD_REQUEST, cause);
	}

}
