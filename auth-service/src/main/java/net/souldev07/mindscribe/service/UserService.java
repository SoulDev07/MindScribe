package net.souldev07.mindscribe.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import net.souldev07.mindscribe.component.AuditPublisher;
import net.souldev07.mindscribe.entity.User;
import net.souldev07.mindscribe.repository.UserRepository;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditPublisher auditPublisher;

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public boolean saveNewUser(User user) {
        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRoles(Arrays.asList("USER"));
            User saved = userRepository.save(user);

            Map<String, Object> details = new HashMap<>();
            details.put("email", saved.getEmail());
            auditPublisher.publish(
                    "USER_CREATED",
                    saved.getUsername(),
                    "User",
                    saved.getId().toString(),
                    details);
            return true;
        } catch (Exception e) {
            log.error("Error saving user: ", e);
            return false;
        }
    }

    public void saveUser(User user) {
        User saved = userRepository.save(user);

        Map<String, Object> details = new HashMap<>();
        details.put("email", saved.getEmail());
        auditPublisher.publish(
                "USER_UPDATED",
                saved.getUsername(),
                "User",
                saved.getId().toString(),
                details);
    }

    public void saveAdmin(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Arrays.asList("USER", "ADMIN"));
        User saved = userRepository.save(user);

        Map<String, Object> details = new HashMap<>();
        details.put("email", saved.getEmail());
        auditPublisher.publish(
                "ADMIN_CREATED",
                saved.getUsername(),
                "User",
                saved.getId().toString(),
                details);
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(ObjectId id) {
        return userRepository.findById(id);
    }

    public void deleteById(ObjectId id) {
        userRepository.deleteById(id);

        auditPublisher.publish(
                "USER_DELETED",
                "SYSTEM",
                "User",
                id.toString(),
                null);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public void deleteByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            userRepository.deleteByUsername(username);
            auditPublisher.publish(
                    "USER_DELETED",
                    username,
                    "User",
                    user.getId().toString(),
                    null);
        }
    }
}
