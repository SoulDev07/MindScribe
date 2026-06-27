package net.souldev07.mindscribe.controller;

import java.util.Map;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.souldev07.mindscribe.entity.JournalEntry;
import net.souldev07.mindscribe.enums.Sentiment;
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
            ObjectId id = new ObjectId(entryId);
            Optional<JournalEntry> entry = journalEntryService.findById(id);
            if (entry.isPresent()) {
                JournalEntry e = entry.get();
                String sentimentStr = body.get("sentiment");

                if (sentimentStr != null) {
                    e.setSentiment(Sentiment.valueOf(sentimentStr.toUpperCase()));
                    journalEntryService.saveEntry(e);
                    return ResponseEntity.ok().build();
                }
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid sentiment value");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.notFound().build();
    }
}
