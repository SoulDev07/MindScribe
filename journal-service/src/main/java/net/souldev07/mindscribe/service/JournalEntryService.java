package net.souldev07.mindscribe.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import net.souldev07.mindscribe.component.AuditPublisher;
import net.souldev07.mindscribe.entity.JournalEntry;
import net.souldev07.mindscribe.model.JournalEntryEvent;
import net.souldev07.mindscribe.repository.JournalEntryRepository;

@Service
@Slf4j
public class JournalEntryService {

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @Autowired
    private KafkaTemplate<String, JournalEntryEvent> entryKafkaTemplate;

    @Autowired
    private AuditPublisher auditPublisher;

    @Transactional
    public void saveEntry(JournalEntry journalEntry, String username) {
        try {
            journalEntry.setUserName(username);
            journalEntry.setDate(LocalDateTime.now());
            JournalEntry saved = journalEntryRepository.save(journalEntry);

            // Publish event for async AI sentiment analysis
            JournalEntryEvent event = JournalEntryEvent.builder()
                    .entryId(saved.getId().toString())
                    .userId(username)
                    .title(saved.getTitle())
                    .content(saved.getContent())
                    .timestamp(saved.getDate().toString())
                    .build();

            entryKafkaTemplate.send("journal-entries", username, event);
            log.info("Published JournalEntryEvent to Kafka for entry: {}", saved.getId());

            Map<String, Object> details = new HashMap<>();
            details.put("title", saved.getTitle());
            auditPublisher.publish(
                    "ENTRY_CREATED",
                    username,
                    "JournalEntry",
                    saved.getId().toString(),
                    details);
        } catch (Exception e) {
            log.error("Error saving entry: ", e);
            throw new RuntimeException("An error occurred while saving the entry.", e);
        }
    }

    public void saveEntry(JournalEntry journalEntry) {
        journalEntryRepository.save(journalEntry);
    }

    public List<JournalEntry> getAll() {
        return journalEntryRepository.findAll();
    }

    public Optional<JournalEntry> findById(ObjectId id) {
        return journalEntryRepository.findById(id);
    }

    @Transactional
    public boolean deleteById(ObjectId id, String username) {
        try {
            Optional<JournalEntry> entryOpt = journalEntryRepository.findById(id);
            if (entryOpt.isPresent() && entryOpt.get().getUserName().equals(username)) {
                journalEntryRepository.deleteById(id);

                Map<String, Object> details = new HashMap<>();
                details.put("title", entryOpt.get().getTitle());
                auditPublisher.publish(
                        "ENTRY_DELETED",
                        username,
                        "JournalEntry",
                        id.toString(),
                        details);
                return true;
            }
        } catch (Exception e) {
            log.error("Error deleting entry: ", e);
            throw new RuntimeException("An error occurred while deleting the entry.", e);
        }
        return false;
    }
}
