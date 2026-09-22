package id.com.flare.kelarus.component;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = { "spring.datasource.url=jdbc:h2:mem:account-discovery;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
				"spring.datasource.username=sa", "spring.datasource.password=",
				"eureka.client.initial-instance-info-replication-interval-seconds=1" })
@DirtiesContext
@TestExecutionListeners(value = AccountDiscoveryTest.RegistryCleanup.class,
		mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class AccountDiscoveryTest {

	private static final CountDownLatch REGISTERED = new CountDownLatch(1);

	private static final HttpServer REGISTRY = registry();

	@DynamicPropertySource
	static void configuration(DynamicPropertyRegistry properties) {
		byte[] key = new byte[32];
		new SecureRandom().nextBytes(key);
		properties.add("kelarus.auth.jwt-secret", () -> Base64.getEncoder().encodeToString(key));
		properties.add("KELARUS_EUREKA_URL", () -> "http://localhost:" + REGISTRY.getAddress().getPort() + "/eureka/");
	}

	@Test
	void runningAccountRegistersWithConfiguredEurekaEndpoint() throws Exception {
		assertThat(REGISTERED.await(15, TimeUnit.SECONDS)).isTrue();
	}

	public static class RegistryCleanup extends AbstractTestExecutionListener {

		@Override
		public int getOrder() {
			return 0;
		}

		@Override
		public void afterTestClass(TestContext context) {
			// afterTestClass listeners run in reverse order: close the fixture after
			// Spring.
			REGISTRY.stop(0);
		}

	}

	private static HttpServer registry() {
		try {
			HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
			server.createContext("/eureka/", exchange -> {
				if (exchange.getRequestMethod().equals("POST")
						&& exchange.getRequestURI().getPath().equals("/eureka/apps/ACCOUNT")) {
					exchange.getRequestBody().readAllBytes();
					exchange.sendResponseHeaders(204, -1);
					REGISTERED.countDown();
				}
				else if (exchange.getRequestMethod().equals("GET")) {
					byte[] body = "{\"applications\":{\"versions__delta\":\"1\",\"apps__hashcode\":\"\",\"application\":[]}}"
							.getBytes(StandardCharsets.UTF_8);
					exchange.getResponseHeaders().set("Content-Type", "application/json");
					exchange.sendResponseHeaders(200, body.length);
					exchange.getResponseBody().write(body);
				}
				else {
					exchange.sendResponseHeaders(200, -1);
				}
				exchange.close();
			});
			server.start();
			return server;
		}
		catch (java.io.IOException ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}

}
