package net.souldev07.mindscribe.scheduler;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;
import net.souldev07.mindscribe.cache.AppCache;
import net.souldev07.mindscribe.entity.JournalEntry;
import net.souldev07.mindscribe.entity.User;
import net.souldev07.mindscribe.enums.Sentiment;
import net.souldev07.mindscribe.model.SentimentData;
import net.souldev07.mindscribe.repository.JournalEntryRepository;

@Component
@Slf4j
public class UserScheduler {

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @Autowired
    private AppCache appCache;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private KafkaTemplate<String, SentimentData> kafkaTemplate;

    @Value("${auth.service.url:http://AUTH-SERVICE}")
    private String authServiceUrl;

    @Scheduled(cron = "0 0 9 * * SUN")
    public void fetchUsersAndSendSaMail() {
        try {
            User[] usersArray = restTemplate.getForObject(authServiceUrl + "/internal/users/sentiment-analysis", User[].class);
            if (usersArray == null) {
                return;
            }
            List<User> users = Arrays.asList(usersArray);

            for (User user : users) {
                List<JournalEntry> entries = journalEntryRepository.findByUserName(user.getUsername());
                
                List<Sentiment> sentiments = entries.stream()
                        .filter(x -> x.getDate().isAfter(LocalDateTime.now().minus(7, ChronoUnit.DAYS)))
                        .map(JournalEntry::getSentiment)
                        .collect(Collectors.toList());

                Map<Sentiment, Integer> counts = new HashMap<>();
                for (Sentiment s : sentiments) {
                    if (s != null) {
                        counts.put(s, counts.getOrDefault(s, 0) + 1);
                    }
                }

                Sentiment top = null;
                int max = 0;
                for (Map.Entry<Sentiment, Integer> e : counts.entrySet()) {
                    if (e.getValue() > max) {
                        max = e.getValue();
                        top = e.getKey();
                    }
                }

                if (top != null) {
                    SentimentData data = SentimentData.builder()
                            .email(user.getEmail())
                            .sentiment("Sentiment for last 7 days: " + top)
                            .build();
                    try {
                        kafkaTemplate.send("weekly-sentiments", data.getEmail(), data);
                    } catch (Exception kafkaEx) {
                        log.error("Error sending sentiment data to Kafka for user {}: ", user.getEmail(), kafkaEx);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Error executing weekly sentiment scheduler: ", ex);
        }
    }

    @Scheduled(cron = "0 0/10 * ? * *")
    public void clearAppCache() {
        appCache.init();
    }
}
