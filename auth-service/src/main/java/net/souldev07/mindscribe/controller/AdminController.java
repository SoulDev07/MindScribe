package net.souldev07.mindscribe.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.souldev07.mindscribe.dto.UserResponseDTO;
import net.souldev07.mindscribe.entity.User;
import net.souldev07.mindscribe.service.UserService;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @GetMapping("/all-users")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<User> users = userService.getAll();
        List<UserResponseDTO> response = users.stream().map(user -> UserResponseDTO.builder()
                .id(user.getId() != null ? user.getId().toString() : null)
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles())
                .sentimentAnalysis(user.isSentimentAnalysis())
                .build()).collect(Collectors.toList());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/create-admin-user")
    public ResponseEntity<?> createAdminUser(@RequestBody User user) {
        userService.saveAdmin(user);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
