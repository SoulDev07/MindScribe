package net.souldev07.mindscribe.component;

import java.time.Instant;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

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

            ListenableFuture<SendResult<String, AuditEvent>> future = kafkaTemplate.send("audit-log", serviceName, event);
            if (future != null) {
                future.addCallback(new ListenableFutureCallback<SendResult<String, AuditEvent>>() {
                    @Override
                    public void onSuccess(SendResult<String, AuditEvent> result) {
                        log.info("Successfully published audit event to topic {} for action: {} - {}",
                                result != null && result.getRecordMetadata() != null ? result.getRecordMetadata().topic() : "audit-log",
                                action, resourceId);
                    }

                    @Override
                    public void onFailure(Throwable ex) {
                        log.error("Failed to asynchronously publish audit event for action {}: ", action, ex);
                    }
                });
            }
        } catch (Exception e) {
            log.error("Failed to publish audit event: ", e);
        }
    }
}
