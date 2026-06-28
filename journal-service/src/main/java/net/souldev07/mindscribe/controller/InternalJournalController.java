package net.souldev07.mindscribe.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.souldev07.mindscribe.service.JournalEntryService;

@RestController
@RequestMapping("/internal")
public class InternalJournalController {

    @Autowired
    private JournalEntryService journalEntryService;

    @PutMapping("/{entryId}/sentiment")
    public ResponseEntity<?> updateSentiment(@PathVariable String entryId,
            @RequestBody Map<String, String> body) {
        try {
            if (body == null || !body.containsKey("sentiment") || body.get("sentiment") == null) {
                return ResponseEntity.badRequest().body("Sentiment is required");
            }
            String sentimentStr = body.get("sentiment");
            boolean updated = journalEntryService.updateSentiment(entryId, sentimentStr);
            if (updated) {
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid sentiment value");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
