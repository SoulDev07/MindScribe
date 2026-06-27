package net.souldev07.mindscribe.model;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent {
    private String timestamp;
    private String service;
    private String action;
    private String actor;
    private String resourceType;
    private String resourceId;
    private Map<String, Object> details;
}
