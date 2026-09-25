package id.com.flare.kelarus.utilities.general.exception;

public interface ExceptionEnum {

	String getCode();

	String getDescription();

	default String getTitle() {
		return "Request failed";
	}

}
