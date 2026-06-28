package net.souldev07.mindscribe.controller;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.souldev07.mindscribe.entity.JournalEntry;
import net.souldev07.mindscribe.service.JournalEntryService;

@RestController
@RequestMapping("/journal")
public class JournalEntryController {

    @Autowired
    private JournalEntryService journalEntryService;

    @GetMapping
    public ResponseEntity<?> getAllJournalEntriesOfUser(@RequestHeader("X-User-Id") String username) {
        List<JournalEntry> all = journalEntryService.getEntriesByUsername(username);
        if (all.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(all, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<JournalEntry> createEntry(@RequestBody JournalEntry myEntry,
            @RequestHeader("X-User-Id") String username) {
        try {
            journalEntryService.saveEntry(myEntry, username);
            return new ResponseEntity<>(myEntry, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/id/{journalId}")
    public ResponseEntity<JournalEntry> getJournalEntryById(@PathVariable ObjectId journalId,
            @RequestHeader("X-User-Id") String username) {
        Optional<JournalEntry> journalEntry = journalEntryService.findById(journalId);
        if (journalEntry.isPresent() && journalEntry.get().getUserName().equals(username)) {
            return new ResponseEntity<>(journalEntry.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/id/{journalId}")
    public ResponseEntity<?> deleteJournalEntryById(@PathVariable ObjectId journalId,
            @RequestHeader("X-User-Id") String username) {
        boolean removed = journalEntryService.deleteById(journalId, username);
        if (removed) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping("/id/{journalId}")
    public ResponseEntity<JournalEntry> updateJournalEntryById(
            @PathVariable ObjectId journalId,
            @RequestBody JournalEntry newEntry,
            @RequestHeader("X-User-Id") String username) {

        Optional<JournalEntry> journalEntryOpt = journalEntryService.findById(journalId);
        if (journalEntryOpt.isPresent() && journalEntryOpt.get().getUserName().equals(username)) {
            JournalEntry old = journalEntryOpt.get();
            old.setTitle(newEntry.getTitle() != null && StringUtils.hasText(newEntry.getTitle()) ? newEntry.getTitle()
                    : old.getTitle());
            old.setContent(newEntry.getContent() != null && StringUtils.hasText(newEntry.getContent()) ? newEntry.getContent()
                    : old.getContent());
            journalEntryService.saveEntry(old);
            return new ResponseEntity<>(old, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
