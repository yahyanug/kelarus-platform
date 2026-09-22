package id.com.flare.kelarus.component.service;

import id.com.flare.kelarus.component.domain.User;
import id.com.flare.kelarus.component.dto.request.VerifyEmailRequest;
import jakarta.validation.Valid;

public interface EmailVerificationService {

	void create(User user);

	void verify(@Valid VerifyEmailRequest request);

}
