package net.souldev07.journalApp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.javamail.JavaMailSender;

import net.souldev07.journalApp.component.AuditPublisher;
import net.souldev07.journalApp.entity.JournalEntry;
import net.souldev07.journalApp.model.AuditEvent;
import net.souldev07.journalApp.model.JournalEntryEvent;
import net.souldev07.journalApp.model.SentimentData;
import net.souldev07.journalApp.repository.JournalEntryRepository;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
})
public class JournalEntryServiceTest {

    @Autowired
    private JournalEntryService journalEntryService;

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @MockBean
    private AuditPublisher auditPublisher;

    @MockBean
    private KafkaTemplate<String, JournalEntryEvent> entryKafkaTemplate;

    @MockBean
    private KafkaTemplate<String, SentimentData> sentimentKafkaTemplate;

    @MockBean
    private KafkaTemplate<String, AuditEvent> auditKafkaTemplate;

    @MockBean
    private JavaMailSender mailSender;

    private static final String TEST_USER = "integration_test_user";

    @BeforeEach
    public void setUp() {
        journalEntryRepository.deleteAll();
    }

    @AfterEach
    public void tearDown() {
        journalEntryRepository.deleteAll();
    }

    @Test
    public void testSaveAndRetrieveEntry() {
        JournalEntry entry = new JournalEntry();
        entry.setTitle("Integration Test Entry");
        entry.setContent("Verifying writes to local MongoDB and Kafka publishing trigger.");

        journalEntryService.saveEntry(entry, TEST_USER);

        List<JournalEntry> entries = journalEntryRepository.findByUserName(TEST_USER);
        assertEquals(1, entries.size());
        assertEquals("Integration Test Entry", entries.get(0).getTitle());
    }
}
