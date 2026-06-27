package net.souldev07.mindscribe.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import net.souldev07.mindscribe.entity.AuditEventEntity;
import net.souldev07.mindscribe.model.AuditEvent;
import net.souldev07.mindscribe.repository.AuditEventRepository;

@Service
@Slf4j
public class AuditEventConsumer {

    @Autowired
    private AuditEventRepository repository;

    @KafkaListener(topics = "audit-log", groupId = "audit-log-group")
    public void consume(AuditEvent event) {
        try {
            AuditEventEntity entity = AuditEventEntity.builder()
                    .timestamp(event.getTimestamp())
                    .service(event.getService())
                    .action(event.getAction())
                    .actor(event.getActor())
                    .resourceType(event.getResourceType())
                    .resourceId(event.getResourceId())
                    .details(event.getDetails())
                    .build();
            repository.save(entity);
            log.info("Audit event stored successfully: {} - {} by {}", event.getService(), event.getAction(),
                    event.getActor());
        } catch (Exception e) {
            log.error("Failed to store audit event: ", e);
        }
    }
}
