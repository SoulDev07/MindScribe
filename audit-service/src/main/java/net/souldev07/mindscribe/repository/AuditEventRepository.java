package net.souldev07.mindscribe.repository;

import java.util.List;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import net.souldev07.mindscribe.entity.AuditEventEntity;

public interface AuditEventRepository extends MongoRepository<AuditEventEntity, ObjectId> {
    List<AuditEventEntity> findByServiceAndAction(String service, String action);

    List<AuditEventEntity> findByActor(String actor);

    List<AuditEventEntity> findByResourceTypeAndResourceId(String type, String id);
}
