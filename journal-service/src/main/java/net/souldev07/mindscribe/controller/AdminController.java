package net.souldev07.mindscribe.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.souldev07.mindscribe.cache.AppCache;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AppCache appCache;

    @PostMapping("/clear-app-cache")
    public ResponseEntity<?> clearAppCache() {
        appCache.init();
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
