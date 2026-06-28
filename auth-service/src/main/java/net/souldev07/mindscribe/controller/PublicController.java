package net.souldev07.mindscribe.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import net.souldev07.mindscribe.entity.User;
import net.souldev07.mindscribe.service.UserDetailsServiceImpl;
import net.souldev07.mindscribe.service.UserService;
import net.souldev07.mindscribe.utils.JwtUtil;

@RestController
@RequestMapping("/public")
@Slf4j
public class PublicController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user) {
        boolean saved = userService.saveNewUser(user);
        if (saved) {
            return new ResponseEntity<>(HttpStatus.CREATED);
        }

        return new ResponseEntity<>("User already exists or invalid data", HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));

            String jwt = jwtUtil.generateToken(user.getUsername());
            return new ResponseEntity<>(jwt, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Authentication failed for user: {}", user.getUsername(), e);
            return new ResponseEntity<>("Incorrect username or password", HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/health-check")
    public String healthCheck() {
        return "Ok";
    }
}
