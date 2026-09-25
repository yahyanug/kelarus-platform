package id.com.flare.kelarus.component.security.annotation;

import id.com.flare.kelarus.component.security.constant.AuthenticationHeaders;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

import java.lang.annotation.*;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@SecurityRequirements
@Parameters({
		@Parameter(in = ParameterIn.HEADER, name = AuthenticationHeaders.IDENTIFIER,
				description = "Unique request identifier", required = true),
		@Parameter(in = ParameterIn.HEADER, name = AuthenticationHeaders.HASH, description = "Request signature hash",
				required = true),
		@Parameter(in = ParameterIn.HEADER, name = AuthenticationHeaders.TIMESTAMP,
				description = "Request timestamp in ISO-8601 format", example = "2026-09-23T13:45:00+07:00",
				required = true) })
public @interface PublicAuth {

}
