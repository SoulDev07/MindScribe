package net.souldev07.mindscribe.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.souldev07.mindscribe.entity.User;
import net.souldev07.mindscribe.service.UserService;

@RestController
@RequestMapping("/internal")
public class InternalController {

    @Autowired
    private UserService userService;

    @GetMapping("/users/sentiment-analysis")
    public List<User> getUsersForSentimentAnalysis() {
        return userService.getUsersForSentimentAnalysis();
    }

    @GetMapping("/user/{username}")
    public User getUserByName(@PathVariable String username) {
        return userService.findByUsername(username);
    }
}
