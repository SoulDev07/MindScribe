package net.souldev07.mindscribe.controller;

import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 50;
    private static final int MAX_PAGE_SIZE = 100;

    @Autowired
    private AuditEventRepository repository;

    @GetMapping("/events")
    public List<AuditEventEntity> getEvents(
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = createPageable(page, size);
        if (service != null && !service.trim().isEmpty() && action != null && !action.trim().isEmpty()) {
            return repository.findByServiceAndAction(service.trim(), action.trim(), pageable).getContent();
        }

        return repository.findAll(pageable).getContent();
    }

    @GetMapping("/events/user/{userId}")
    public List<AuditEventEntity> getEventsByUser(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        if (userId == null || userId.trim().isEmpty()) {
            return Collections.emptyList();
        }
        Pageable pageable = createPageable(page, size);
        return repository.findByActor(userId.trim(), pageable).getContent();
    }

    @GetMapping("/events/resource/{type}/{id}")
    public List<AuditEventEntity> getEventsByResource(
            @PathVariable String type,
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        if (type == null || type.trim().isEmpty() || id == null || id.trim().isEmpty()) {
            return Collections.emptyList();
        }
        Pageable pageable = createPageable(page, size);
        return repository.findByResourceTypeAndResourceId(type.trim(), id.trim(), pageable).getContent();
    }

    private Pageable createPageable(int page, int size) {
        int pageNumber = Math.max(DEFAULT_PAGE, page);
        int pageSize = (size <= 0) ? DEFAULT_SIZE : Math.min(size, MAX_PAGE_SIZE);
        return PageRequest.of(pageNumber, pageSize);
    }
}
