package id.com.flare.kelarus.component.dto.response;

public record AuthTokenResponse(String accessToken, String refreshToken, String tokenType, long expiresIn) {
	@Override
	public String toString() {
		return "AuthTokenResponse[REDACTED]";
	}
}
