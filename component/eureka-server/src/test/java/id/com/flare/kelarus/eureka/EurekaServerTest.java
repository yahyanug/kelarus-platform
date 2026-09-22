package id.com.flare.kelarus.eureka;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import com.netflix.discovery.EurekaClientConfig;
import com.netflix.eureka.cluster.PeerEurekaNodes;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EurekaServerTest {
    @LocalServerPort int port;
    @Autowired EurekaClientConfig configuration;
    @Autowired PeerEurekaNodes peers;

    @Test
    void standaloneRegistryAcceptsAndRemovesAccountRegistration() throws Exception {
        assertThat(configuration.shouldRegisterWithEureka()).isFalse();
        assertThat(configuration.shouldFetchRegistry()).isFalse();
        assertThat(peers.getPeerEurekaNodes()).isEmpty();
        HttpClient client = HttpClient.newHttpClient();
        String registration = """
                {"instance":{"instanceId":"account-test","app":"ACCOUNT","hostName":"localhost",
                "ipAddr":"127.0.0.1","status":"UP","port":{"$":50003,"@enabled":"true"},
                "vipAddress":"account","dataCenterInfo":{"@class":"com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo","name":"MyOwn"}}}
                """;
        HttpResponse<String> registered = client.send(request("/eureka/apps/ACCOUNT")
                .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(registration)).build(),
                HttpResponse.BodyHandlers.ofString());
        try {
            assertThat(registered.statusCode()).isEqualTo(204);
            HttpResponse<String> found = client.send(request("/eureka/apps/ACCOUNT/account-test")
                    .header("Accept", "application/json").GET().build(), HttpResponse.BodyHandlers.ofString());
            assertThat(found.statusCode()).isEqualTo(200);
            assertThat(found.body()).contains("account-test");
        } finally {
            client.send(request("/eureka/apps/ACCOUNT/account-test").DELETE().build(), HttpResponse.BodyHandlers.discarding());
        }
    }

    private HttpRequest.Builder request(String path) {
        return HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).timeout(Duration.ofSeconds(10));
    }
}
