package id.com.flare.kelarus.gateway.component.config;

import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

import static org.springdoc.core.properties.AbstractSwaggerUiConfigProperties.SwaggerUrl;
import static org.springdoc.core.utils.Constants.DEFAULT_API_DOCS_URL;

@Configuration
public class SwaggerConfiguration {

	@Bean
	@Lazy(false)
	public Set<SwaggerUrl> apis(SwaggerUiConfigParameters swaggerUiConfigParameters, RouteDefinitionLocator locator) {

		Set<SwaggerUrl> urls = locator.getRouteDefinitions()
				.flatMap(route -> Mono.justOrEmpty(route.getId()))
				.filter(routeId -> routeId.endsWith("-component"))
				.map(serviceName -> new SwaggerUrl(serviceName, "/" + serviceName + DEFAULT_API_DOCS_URL,
						null))
				.collect(Collectors.toSet())
				.blockOptional()
				.orElseGet(Set::of);

		swaggerUiConfigParameters.setUrls(urls);

		return urls;
	}

}
