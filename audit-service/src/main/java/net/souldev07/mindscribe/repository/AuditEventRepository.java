package net.souldev07.mindscribe.repository;

import java.util.List;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import net.souldev07.mindscribe.entity.AuditEventEntity;

public interface AuditEventRepository extends MongoRepository<AuditEventEntity, ObjectId> {
    List<AuditEventEntity> findByServiceAndAction(String service, String action);

    Page<AuditEventEntity> findByServiceAndAction(String service, String action, Pageable pageable);

    List<AuditEventEntity> findByActor(String actor);

    Page<AuditEventEntity> findByActor(String actor, Pageable pageable);

    List<AuditEventEntity> findByResourceTypeAndResourceId(String type, String id);

    Page<AuditEventEntity> findByResourceTypeAndResourceId(String type, String id, Pageable pageable);
}
