package id.com.flare.kelarus.gateway;

import com.sun.net.httpserver.HttpServer;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "eureka.client.enabled=false")
class AccountRoutingTest {
    private static final HttpServer ACCOUNT = startAccount();
    @LocalServerPort int port;
    @Autowired RouteLocator routes;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    @DynamicPropertySource
    static void discovery(DynamicPropertyRegistry properties) {
        properties.add("spring.cloud.discovery.client.simple.instances.account[0].uri",
                () -> "http://localhost:" + ACCOUNT.getAddress().getPort());
    }

    @AfterAll
    static void stopAccount() { ACCOUNT.stop(0); }

    @Test
    void usesServiceDiscoveryAndPreservesPublicPathAndBody() throws Exception {
        assertThat(routes.getRoutes().collectList().block(Duration.ofSeconds(5)))
                .anySatisfy(route -> {
                    assertThat(route.getId()).isEqualTo("account-auth");
                    assertThat(route.getUri().toString()).isEqualTo("lb://account");
                });
        HttpResponse<String> response = send("/v1/public/auth/login", "request-body", null);
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("/v1/public/auth/login:request-body");
    }

    @Test
    void forwardsAuthorizationAndPreservesDownstreamAuthenticationFailure() throws Exception {
        assertThat(send("/v1/auth/logout", "body", null).statusCode()).isEqualTo(401);
        assertThat(send("/v1/auth/logout", "body", "Bearer test-fixture").statusCode()).isEqualTo(200);
    }

    @Test
    void doesNotExposeArbitraryServicesOrPaths() throws Exception {
        assertThat(send("/account/v1/public/auth/login", "", null).statusCode()).isEqualTo(404);
        assertThat(send("/unrelated", "", null).statusCode()).isEqualTo(404);
    }

    private HttpResponse<String> send(String path, String body, String authorization) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(10)).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body));
        if (authorization != null) request.header("Authorization", authorization);
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static HttpServer startAccount() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            server.createContext("/", exchange -> {
                String path = exchange.getRequestURI().getPath();
                boolean denied = path.startsWith("/v1/auth/")
                        && !"Bearer test-fixture".equals(exchange.getRequestHeaders().getFirst("Authorization"));
                byte[] body = (path + ":" + new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8))
                        .getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(denied ? 401 : 200, body.length);
                try (var output = exchange.getResponseBody()) { output.write(body); }
            });
            server.start();
            return server;
        } catch (java.io.IOException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }
}
