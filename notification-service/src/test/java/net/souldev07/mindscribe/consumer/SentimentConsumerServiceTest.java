package net.souldev07.mindscribe.consumer;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;

import net.souldev07.mindscribe.model.SentimentData;
import net.souldev07.mindscribe.service.EmailService;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
})
public class SentimentConsumerServiceTest {

    @Autowired
    private SentimentConsumerService consumerService;

    @MockBean
    private EmailService emailService;

    @MockBean
    private JavaMailSender mailSender;

    @Test
    public void testConsumeWeeklySummary() {
        SentimentData data = SentimentData.builder()
                .email("test@email.com")
                .sentiment("Sentiment: JOYFUL")
                .build();

        consumerService.consume(data);

        verify(emailService, times(1)).sendEmail(
                "test@email.com",
                "Your Weekly Journal Sentiment Summary",
                "Sentiment: JOYFUL");
    }
}
