package net.souldev07.mindscribe.component;

import java.time.Instant;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.souldev07.mindscribe.model.AuditEvent;

@Component
@Slf4j
public class AuditPublisher {

    @Autowired
    private KafkaTemplate<String, AuditEvent> kafkaTemplate;

    @Value("${spring.application.name}")
    private String serviceName;

    public void publish(String action, String actor, String resourceType,
            String resourceId, Map<String, Object> details) {
        try {
            AuditEvent event = AuditEvent.builder()
                    .timestamp(Instant.now().toString())
                    .service(serviceName)
                    .action(action)
                    .actor(actor)
                    .resourceType(resourceType)
                    .resourceId(resourceId)
                    .details(details)
                    .build();

            kafkaTemplate.send("audit-log", serviceName, event).completable().whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish audit event for action: {}, resourceId: {}", action, resourceId, ex);
                } else {
                    log.info("Published audit event: {} - {}", action, resourceId);
                }
            });
        } catch (Exception e) {
            log.error("Failed to initiate audit event publishing: ", e);
        }
    }
}
