package id.com.flare.kelarus.gateway.component.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.springdoc.core.properties.AbstractSwaggerUiConfigProperties.SwaggerUrl;
import static org.springdoc.core.utils.Constants.DEFAULT_API_DOCS_URL;

@Configuration
public class SwaggerConfiguration {

	private static final Logger log = LoggerFactory.getLogger(SwaggerConfiguration.class);

	@Bean
	@Lazy(false)
	public ApplicationRunner swaggerApis(SwaggerUiConfigProperties swaggerUiConfigProperties,
			RouteDefinitionLocator locator) {

		return args -> {
			Set<SwaggerUrl> urls = locator.getRouteDefinitions().map(routeDefinition -> routeDefinition.getId())
					.filter(routeId -> routeId != null && routeId.endsWith("-component")).sort()
					.map(routeId -> new SwaggerUrl(routeId, "/" + routeId + DEFAULT_API_DOCS_URL, routeId))
					.collectList().map(LinkedHashSet::new).blockOptional().orElseGet(LinkedHashSet::new);

			swaggerUiConfigProperties.setUrl(null);
			swaggerUiConfigProperties.setUrls(urls);

			log.info("Swagger API definitions registered: {}", urls);
		};
	}

}
