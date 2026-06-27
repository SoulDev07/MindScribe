package net.souldev07.mindscribe.entity;

import java.util.Map;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "audit_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEventEntity {
    @Id
    private ObjectId id;
    private String timestamp;
    private String service;
    private String action;
    private String actor;
    private String resourceType;
    private String resourceId;
    private Map<String, Object> details;
}
