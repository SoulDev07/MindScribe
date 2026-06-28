package net.souldev07.mindscribe.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import net.souldev07.mindscribe.model.JournalEntryEvent;
import net.souldev07.mindscribe.service.JournalEntryService;
import net.souldev07.mindscribe.service.SentimentService;

@Service
@Slf4j
public class JournalEntryConsumer {

    @Autowired
    private JournalEntryService journalEntryService;

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
                journalEntryService.updateSentiment(event.getEntryId(), sentiment);
                log.info("Successfully updated sentiment to {} for entry {}", sentiment, event.getEntryId());
            }
        } catch (Exception e) {
            log.error("Failed to analyze or write back sentiment: ", e);
        }
    }
}
