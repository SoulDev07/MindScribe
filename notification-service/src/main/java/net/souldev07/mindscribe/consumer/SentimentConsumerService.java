package net.souldev07.mindscribe.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;
import net.souldev07.mindscribe.model.SentimentData;
import net.souldev07.mindscribe.service.EmailService;

@Service
@Slf4j
public class SentimentConsumerService {

    @Autowired
    private EmailService emailService;

    @KafkaListener(topics = "weekly-sentiments", groupId = "weekly-sentiment-group")
    public void consume(SentimentData sentimentData) {
        try {
            if (sentimentData == null) {
                log.warn("Received null SentimentData event, ignoring.");
                return;
            }
            if (!StringUtils.hasText(sentimentData.getEmail())) {
                log.warn("Received SentimentData with empty recipient email, ignoring.");
                return;
            }
            log.info("Received SentimentData for weekly summary aggregation email: {}", sentimentData.getEmail());
            emailService.sendEmail(
                    sentimentData.getEmail(),
                    "Your Weekly Journal Sentiment Summary",
                    sentimentData.getSentiment() != null ? sentimentData.getSentiment() : "No sentiment recorded for the period.");
        } catch (Exception e) {
            log.error("Failed to process SentimentData message: ", e);
        }
    }
}
