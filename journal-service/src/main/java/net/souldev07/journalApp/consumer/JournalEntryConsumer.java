package net.souldev07.journalApp.consumer;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;
import net.souldev07.journalApp.model.JournalEntryEvent;
import net.souldev07.journalApp.service.SentimentService;

@Service
@Slf4j
public class JournalEntryConsumer {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private SentimentService sentimentService;

    @KafkaListener(topics = "journal-entries", groupId = "sentiment-analysis-group")
    public void consume(JournalEntryEvent event) {
        log.info("Received journal entry event for analysis: {}", event.getEntryId());
        try {
            // Call Python Sentiment Service
            String sentiment = sentimentService.analyzeSentiment(
                    event.getEntryId(), event.getUserId(), event.getContent());

            if (sentiment != null) {
                Map<String, String> body = new HashMap<>();
                body.put("sentiment", sentiment);

                restTemplate.put(
                        "http://JOURNAL-SERVICE/internal/" + event.getEntryId() + "/sentiment",
                        body);
                log.info("Successfully updated sentiment to {} for entry {}", sentiment, event.getEntryId());
            }
        } catch (Exception e) {
            log.error("Failed to analyze or write back sentiment: ", e);
        }
    }
}
