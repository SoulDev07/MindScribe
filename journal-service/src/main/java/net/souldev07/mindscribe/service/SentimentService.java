package net.souldev07.mindscribe.service;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SentimentService {

    @Value("${sentiment.service.url:http://SENTIMENT-SERVICE}")
    private String sentimentServiceUrl;

    @Autowired
    private RestTemplate restTemplate;

    @CircuitBreaker(name = "aiSentimentCB", fallbackMethod = "analyzeFallback")
    public String analyzeSentiment(String entryId, String userId, String content) {
        Map<String, String> request = new HashMap<>();
        request.put("entry_id", entryId);
        request.put("user_id", userId);
        request.put("content", content);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = (Map<String, Object>) restTemplate.postForObject(
                sentimentServiceUrl + "/analyze", request, Map.class);

        if (response != null)
            return (String) response.get("sentiment");

        return null;
    }

    public String analyzeFallback(String entryId, String userId, String content, Throwable t) {
        log.warn("AI API circuit breaker triggered for entry {}: {}", entryId, t.getMessage());
        return null;
    }
}
