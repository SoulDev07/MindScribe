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
    private String service; // "auth-service", "journal-service"
    private String action; // "USER_CREATED", "ENTRY_DELETED"
    private String actor; // userId or "SYSTEM"
    private String resourceType; // "User", "JournalEntry"
    private String resourceId; // the entity ID
    private Map<String, Object> details; // extra context
}
