package net.souldev07.journalApp.service;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.client.RestTemplate;

import net.souldev07.journalApp.component.AuditPublisher;
import net.souldev07.journalApp.model.AuditEvent;
import net.souldev07.journalApp.model.JournalEntryEvent;
import net.souldev07.journalApp.model.SentimentData;
import net.souldev07.journalApp.repository.ConfigJournalAppRepository;
import net.souldev07.journalApp.repository.JournalEntryRepository;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
})
public class SentimentServiceTest {

    @Autowired
    private SentimentService sentimentService;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private JournalEntryRepository journalEntryRepository;

    @MockBean
    private ConfigJournalAppRepository configJournalAppRepository;

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
    public void testSentimentServiceFallback() {
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenThrow(new RuntimeException("API error"));

        String response = sentimentService.analyzeSentiment("entry123", "user123", "content");
        assertNull(response);

        verify(restTemplate, times(1)).postForObject(anyString(), any(), eq(Map.class));
    }
}
