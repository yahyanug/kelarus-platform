package id.com.flare.kelarus.component.validation;

import java.util.Locale;

public final class AuthInput {

	private AuthInput() {
	}

	public static String normalizeEmail(String email) {
		return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
	}

}
