package net.souldev07.mindscribe.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import net.souldev07.mindscribe.api.response.WeatherResponse;
import net.souldev07.mindscribe.cache.AppCache;
import net.souldev07.mindscribe.component.AuditPublisher;
import net.souldev07.mindscribe.model.AuditEvent;
import net.souldev07.mindscribe.model.JournalEntryEvent;
import net.souldev07.mindscribe.model.SentimentData;
import net.souldev07.mindscribe.repository.ConfigJournalAppRepository;
import net.souldev07.mindscribe.repository.JournalEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(properties = {
        "weather.api.key=dummy_key",
        "eureka.client.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
})
public class WeatherServiceTest {

    @Autowired
    private WeatherService weatherService;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private RedisService redisService;

    @MockBean
    private AppCache appCache;

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

    @BeforeEach
    public void setUp() {
        Map<String, String> cacheMap = new HashMap<>();
        cacheMap.put("WEATHER_API", "http://dummy-weather-api.com/city={city}&key={api_key}");
        appCache.appCache = cacheMap;
    }

    @Test
    public void testWeatherApiFallbackAndRetry() {
        when(redisService.get(anyString(), eq(WeatherResponse.class))).thenReturn(null);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(WeatherResponse.class)))
                .thenThrow(new RuntimeException("API error"));

        WeatherResponse response = weatherService.getWeather("Mumbai");
        assertNull(response);

        verify(restTemplate, times(3)).exchange(anyString(), eq(HttpMethod.GET), any(), eq(WeatherResponse.class));
    }

    @Test
    public void testWeatherApiCaching() {
        WeatherResponse mockResponse = new WeatherResponse();
        WeatherResponse.Main main = new WeatherResponse.Main();
        main.setFeelsLike(28.0);
        mockResponse.setMain(main);

        when(redisService.get(eq("weather_of_Mumbai"), eq(WeatherResponse.class)))
                .thenReturn(null)
                .thenReturn(mockResponse);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(WeatherResponse.class)))
                .thenReturn(ResponseEntity.ok(mockResponse));

        WeatherResponse response1 = weatherService.getWeather("Mumbai");
        assertNotNull(response1);

        WeatherResponse response2 = weatherService.getWeather("Mumbai");
        assertNotNull(response2);

        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(), eq(WeatherResponse.class));
    }
}
