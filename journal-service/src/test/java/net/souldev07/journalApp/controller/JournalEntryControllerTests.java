package net.souldev07.journalApp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.souldev07.journalApp.component.AuditPublisher;
import net.souldev07.journalApp.entity.JournalEntry;
import net.souldev07.journalApp.model.AuditEvent;
import net.souldev07.journalApp.model.JournalEntryEvent;
import net.souldev07.journalApp.model.SentimentData;
import net.souldev07.journalApp.repository.JournalEntryRepository;
import net.souldev07.journalApp.service.JournalEntryService;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
})
@AutoConfigureMockMvc
public class JournalEntryControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JournalEntryService journalEntryService;

    @MockBean
    private JournalEntryRepository journalEntryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KafkaTemplate<String, JournalEntryEvent> entryKafkaTemplate;

    @MockBean
    private KafkaTemplate<String, SentimentData> sentimentKafkaTemplate;

    @MockBean
    private KafkaTemplate<String, AuditEvent> auditKafkaTemplate;

    @MockBean
    private AuditPublisher auditPublisher;

    @MockBean
    private JavaMailSender mailSender;

    @Test
    public void testGetAllJournalEntriesSuccess() throws Exception {
        JournalEntry entry = new JournalEntry();
        entry.setTitle("Test Title");
        entry.setUserName("test_user");

        when(journalEntryRepository.findByUserName("test_user")).thenReturn(Arrays.asList(entry));

        mockMvc.perform(get("/journal")
                .header("X-User-Id", "test_user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Title"));
    }

    @Test
    public void testCreateEntrySuccess() throws Exception {
        JournalEntry entry = new JournalEntry();
        entry.setTitle("New Entry");
        entry.setContent("Content");

        doNothing().when(journalEntryService).saveEntry(any(JournalEntry.class), eq("test_user"));

        mockMvc.perform(post("/journal")
                .header("X-User-Id", "test_user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(entry)))
                .andExpect(status().isCreated());
    }
}
