package net.souldev07.mindscribe.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import net.souldev07.mindscribe.entity.AuditEventEntity;
import net.souldev07.mindscribe.repository.AuditEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

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

        when(repository.findByServiceAndAction(eq("AUTH-SERVICE"), eq("USER_CREATED"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(event)));

        mockMvc.perform(get("/audit/events")
                .param("service", "AUTH-SERVICE")
                .param("action", "USER_CREATED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].service").value("AUTH-SERVICE"))
                .andExpect(jsonPath("$[0].action").value("USER_CREATED"));
    }
}
