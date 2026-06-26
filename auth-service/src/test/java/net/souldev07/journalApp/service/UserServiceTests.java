package net.souldev07.journalApp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import net.souldev07.journalApp.component.AuditPublisher;
import net.souldev07.journalApp.entity.User;
import net.souldev07.journalApp.repository.UserRepository;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
})
public class UserServiceTests {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private AuditPublisher auditPublisher;

    private static final String TEST_USERNAME = "production_test_user";

    @BeforeEach
    public void setUp() {
        userRepository.deleteByUsername(TEST_USERNAME);
    }

    @AfterEach
    public void tearDown() {
        userRepository.deleteByUsername(TEST_USERNAME);
    }

    @Test
    public void testCreateAndRetrieveUser() {
        User user = new User();
        user.setUsername(TEST_USERNAME);
        user.setPassword("secure_password_123");
        user.setEmail("test@journalapp.com");

        boolean isSaved = userService.saveNewUser(user);
        assertTrue(isSaved);

        User retrieved = userService.findByUsername(TEST_USERNAME);
        assertNotNull(retrieved);
        assertEquals(TEST_USERNAME, retrieved.getUsername());
        assertEquals("test@journalapp.com", retrieved.getEmail());
    }
}
