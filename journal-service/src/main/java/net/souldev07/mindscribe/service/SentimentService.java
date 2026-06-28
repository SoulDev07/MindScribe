package net.souldev07.mindscribe.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SentimentService {

    @Value("${sentiment.service.url:http://SENTIMENT-SERVICE}")
    private String sentimentServiceUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SentimentAnalysisResponse {
        @JsonProperty("sentiment")
        private String sentiment;

        @JsonProperty("score")
        private Double score;

        @JsonProperty("entry_id")
        private String entryId;
    }

    @CircuitBreaker(name = "aiSentimentCB", fallbackMethod = "analyzeFallback")
    public String analyzeSentiment(String entryId, String userId, String content) {
        Map<String, String> request = new HashMap<>();
        request.put("entry_id", entryId);
        request.put("user_id", userId);
        request.put("content", content);

        SentimentAnalysisResponse response = restTemplate.postForObject(
                sentimentServiceUrl + "/analyze", request, SentimentAnalysisResponse.class);

        if (response != null && response.getSentiment() != null) {
            return response.getSentiment();
        }

        return null;
    }

    public String analyzeFallback(String entryId, String userId, String content, Throwable t) {
        log.error("AI API circuit breaker triggered for entry {}: {}", entryId, t.getMessage(), t);
        return null;
    }
}
