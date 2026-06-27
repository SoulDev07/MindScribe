package net.souldev07.mindscribe.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.souldev07.mindscribe.entity.AuditEventEntity;
import net.souldev07.mindscribe.repository.AuditEventRepository;

@RestController
@RequestMapping("/audit")
public class AuditController {

    @Autowired
    private AuditEventRepository repository;

    @GetMapping("/events")
    public List<AuditEventEntity> getEvents(
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String action) {
        if (service != null && action != null)
            return repository.findByServiceAndAction(service, action);

        return repository.findAll();
    }

    @GetMapping("/events/user/{userId}")
    public List<AuditEventEntity> getEventsByUser(@PathVariable String userId) {
        return repository.findByActor(userId);
    }

    @GetMapping("/events/resource/{type}/{id}")
    public List<AuditEventEntity> getEventsByResource(
            @PathVariable String type,
            @PathVariable String id) {
        return repository.findByResourceTypeAndResourceId(type, id);
    }
}
