package net.souldev07.mindscribe.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.souldev07.mindscribe.component.AuditPublisher;
import net.souldev07.mindscribe.entity.User;
import net.souldev07.mindscribe.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

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
        user.setEmail("test@mindscribe.com");

        boolean isSaved = userService.saveNewUser(user);
        assertTrue(isSaved);

        User retrieved = userService.findByUsername(TEST_USERNAME);
        assertNotNull(retrieved);
        assertEquals(TEST_USERNAME, retrieved.getUsername());
        assertEquals("test@mindscribe.com", retrieved.getEmail());
    }
}
