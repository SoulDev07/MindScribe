package net.souldev07.mindscribe.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.souldev07.mindscribe.entity.User;
import net.souldev07.mindscribe.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
})
@AutoConfigureMockMvc
public class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private org.springframework.kafka.core.KafkaTemplate<String, net.souldev07.mindscribe.model.AuditEvent> kafkaTemplate;

    @Test
    @WithMockUser(username = "mock_user")
    public void testUpdateUserSuccess() throws Exception {
        User existingUser = new User();
        existingUser.setUsername("mock_user");
        existingUser.setPassword("old_pass");

        when(userService.findByUsername("mock_user")).thenReturn(existingUser);
        doNothing().when(userService).saveUser(any(User.class));

        User updatedUser = new User();
        updatedUser.setUsername("new_username");
        updatedUser.setPassword("new_pass");
        updatedUser.setEmail("new@email.com");

        mockMvc.perform(put("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "mock_user")
    public void testDeleteUserSuccess() throws Exception {
        doNothing().when(userService).deleteByUsername("mock_user");

        mockMvc.perform(delete("/user"))
                .andExpect(status().isNoContent());
    }
}
