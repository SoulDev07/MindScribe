package net.souldev07.mindscribe.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.souldev07.mindscribe.api.response.WeatherResponse;
import net.souldev07.mindscribe.service.WeatherService;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private WeatherService weatherService;

    @GetMapping
    public ResponseEntity<String> greeting(@RequestHeader("X-User-Id") String username) {
        String greetingMessage = "Hi " + username + "!";

        WeatherResponse weatherData = weatherService.getWeather("Mumbai");
        if (weatherData != null) {
            greetingMessage += " Weather feels like " + weatherData.getMain().getFeelsLike() + "\u00b0C";
        }

        return new ResponseEntity<>(greetingMessage, HttpStatus.OK);
    }
}
