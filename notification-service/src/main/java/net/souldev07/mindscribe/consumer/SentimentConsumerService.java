package net.souldev07.mindscribe.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

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
        log.info("Received SentimentData for weekly summary aggregation email: {}", sentimentData.getEmail());
        emailService.sendEmail(
                sentimentData.getEmail(),
                "Your Weekly Journal Sentiment Summary",
                sentimentData.getSentiment());
    }
}
