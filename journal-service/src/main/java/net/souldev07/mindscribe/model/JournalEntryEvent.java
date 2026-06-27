package net.souldev07.mindscribe.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalEntryEvent {
    private String entryId;
    private String userId;
    private String title;
    private String content;
    private String timestamp;
}
