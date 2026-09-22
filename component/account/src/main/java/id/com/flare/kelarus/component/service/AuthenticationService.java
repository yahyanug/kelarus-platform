package id.com.flare.kelarus.component.service;

import id.com.flare.kelarus.component.dto.request.*;
import id.com.flare.kelarus.component.dto.response.*;
import java.util.UUID;
import jakarta.validation.Valid;

public interface AuthenticationService {
    RegisterResponse register(@Valid RegisterRequest request);
    AuthTokenResponse login(@Valid LoginRequest request);
    AuthTokenResponse refresh(@Valid RefreshTokenRequest request);
    void logout(UUID authenticatedUserId, @Valid LogoutRequest request);
}
