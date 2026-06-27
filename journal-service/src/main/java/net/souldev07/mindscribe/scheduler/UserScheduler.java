package net.souldev07.mindscribe.scheduler;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import net.souldev07.mindscribe.cache.AppCache;
import net.souldev07.mindscribe.entity.JournalEntry;
import net.souldev07.mindscribe.entity.User;
import net.souldev07.mindscribe.enums.Sentiment;
import net.souldev07.mindscribe.model.SentimentData;
import net.souldev07.mindscribe.repository.JournalEntryRepository;
import net.souldev07.mindscribe.service.EmailService;

@Component
public class UserScheduler {

    @Autowired
    private EmailService emailService;

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @Autowired
    private AppCache appCache;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private KafkaTemplate<String, SentimentData> kafkaTemplate;

    @Scheduled(cron = "0 0 9 * * SUN")
    public void fetchUsersAndSendSaMail() {
        try {
            User[] usersArray = restTemplate.getForObject("http://AUTH-SERVICE/internal/users/sentiment-analysis", User[].class);
            if (usersArray == null) return;
            List<User> users = Arrays.asList(usersArray);

            for (User user : users) {
                List<JournalEntry> entries = journalEntryRepository.findByUserName(user.getUsername());
                
                List<Sentiment> sentiments = entries.stream()
                        .filter(x -> x.getDate().isAfter(LocalDateTime.now().minus(7, ChronoUnit.DAYS)))
                        .map(JournalEntry::getSentiment)
                        .collect(Collectors.toList());

                Map<Sentiment, Integer> counts = new HashMap<>();
                for (Sentiment s : sentiments) {
                    if (s != null) counts.put(s, counts.getOrDefault(s, 0) + 1);
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
                        emailService.sendEmail(data.getEmail(), "Sentiment", data.getSentiment());
                    }
                }
            }
        } catch (Exception ex) {
            // handle rest template connection errors gracefully during boot
        }
    }

    @Scheduled(cron = "0 0/10 * ? * *")
    public void clearAppCache() {
        appCache.init();
    }
}
