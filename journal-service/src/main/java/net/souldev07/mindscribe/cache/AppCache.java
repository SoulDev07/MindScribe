package net.souldev07.mindscribe.cache;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.souldev07.mindscribe.repository.ConfigJournalAppRepository;

@Component
public class AppCache {

    public enum keys {
        WEATHER_API
    }

    @Autowired
    private ConfigJournalAppRepository configJournalAppRepository;

    public Map<String, String> appCache;

    @PostConstruct
    public void init() {
        appCache = new HashMap<>();
        configJournalAppRepository.findAll().forEach(config -> {
            appCache.put(config.getKey(), config.getValue());
        });
    }
}
