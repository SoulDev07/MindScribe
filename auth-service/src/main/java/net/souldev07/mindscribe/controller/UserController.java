package net.souldev07.mindscribe.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.souldev07.mindscribe.entity.User;
import net.souldev07.mindscribe.service.UserService;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PutMapping
    public ResponseEntity<?> updateUser(@RequestBody User user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User userInDB = userService.findByUsername(username);
        if (userInDB == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        userInDB.setUsername(user.getUsername());
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            userInDB.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userInDB.setEmail(user.getEmail());
        userInDB.setSentimentAnalysis(user.isSentimentAnalysis());

        userService.saveUser(userInDB);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        userService.deleteByUsername(username);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
