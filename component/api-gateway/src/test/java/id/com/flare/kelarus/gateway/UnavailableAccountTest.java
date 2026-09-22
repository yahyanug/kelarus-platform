package id.com.flare.kelarus.gateway;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "eureka.client.enabled=false")
class UnavailableAccountTest {
    @LocalServerPort int port;

    @Test
    void missingAccountInstanceReturnsServiceUnavailable() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/v1/public/auth/login"))
                .timeout(Duration.ofSeconds(10)).POST(HttpRequest.BodyPublishers.ofString("{}")).build();
        assertThat(HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.discarding()).statusCode())
                .isEqualTo(503);
    }
}
