package net.souldev07.mindscribe.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import net.souldev07.mindscribe.entity.ConfigJournalAppEntity;

public interface ConfigJournalAppRepository extends MongoRepository<ConfigJournalAppEntity, String> {

}
