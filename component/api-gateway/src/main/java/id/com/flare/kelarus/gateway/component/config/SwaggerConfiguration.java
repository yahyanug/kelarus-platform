package id.com.flare.kelarus.gateway.component.config;

import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.springdoc.core.properties.AbstractSwaggerUiConfigProperties.SwaggerUrl;
import static org.springdoc.core.utils.Constants.DEFAULT_API_DOCS_URL;

@Configuration
public class SwaggerConfiguration {

	@Bean
	@Primary
	@Lazy(false)
	public SwaggerUiConfigProperties swaggerUiConfig(SwaggerUiConfigProperties config, RouteDefinitionLocator locator) {

		Set<SwaggerUrl> urls = locator.getRouteDefinitions().map(route -> {
			assert route.getId() != null;
			return route.getId();
		}).filter(routeId -> routeId.endsWith("-component")).sort()
				.map(routeId -> new SwaggerUrl(routeId, "/" + routeId + DEFAULT_API_DOCS_URL, null)).collectList()
				.map(LinkedHashSet::new).blockOptional().orElseGet(LinkedHashSet::new);

		config.setUrl(null);
		config.setUrls(urls);

		return config;
	}

}
