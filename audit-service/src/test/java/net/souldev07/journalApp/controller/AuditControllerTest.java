package net.souldev07.journalApp.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import net.souldev07.journalApp.entity.AuditEventEntity;
import net.souldev07.journalApp.repository.AuditEventRepository;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
})
@AutoConfigureMockMvc
public class AuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditEventRepository repository;

    @Test
    public void testGetEventsSuccess() throws Exception {
        AuditEventEntity event = new AuditEventEntity();
        event.setService("AUTH-SERVICE");
        event.setAction("USER_CREATED");

        when(repository.findByServiceAndAction("AUTH-SERVICE", "USER_CREATED"))
                .thenReturn(Arrays.asList(event));

        mockMvc.perform(get("/audit/events")
                .param("service", "AUTH-SERVICE")
                .param("action", "USER_CREATED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].service").value("AUTH-SERVICE"))
                .andExpect(jsonPath("$[0].action").value("USER_CREATED"));
    }
}
