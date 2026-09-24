package id.com.flare.kelarus;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:common;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS common",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "eureka.client.enabled=false"
})
class CommonApplicationTest {

    @Test
    void contextLoads() {
    }

}
