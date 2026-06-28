package net.souldev07.mindscribe.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.souldev07.mindscribe.entity.ConfigJournalAppEntity;
import net.souldev07.mindscribe.repository.ConfigJournalAppRepository;

@Component
@Slf4j
public class AppCache {

    public enum Keys {
        WEATHER_API
    }

    @Autowired
    private ConfigJournalAppRepository configJournalAppRepository;

    public volatile Map<String, String> appCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        try {
            Map<String, String> tempCache = new ConcurrentHashMap<>();
            for (ConfigJournalAppEntity config : configJournalAppRepository.findAll()) {
                if (config != null && config.getKey() != null && config.getValue() != null) {
                    tempCache.put(config.getKey(), config.getValue());
                }
            }
            this.appCache = tempCache;
        } catch (Exception e) {
            log.error("Failed to initialize AppCache from MongoDB: ", e);
        }
    }
}
