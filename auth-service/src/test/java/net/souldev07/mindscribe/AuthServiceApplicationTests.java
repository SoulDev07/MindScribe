package net.souldev07.mindscribe;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import net.souldev07.mindscribe.model.AuditEvent;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
})
class AuthServiceApplicationTests {

    @MockBean
    private KafkaTemplate<String, AuditEvent> kafkaTemplate;

    @Test
    void contextLoads() {
    }

}
