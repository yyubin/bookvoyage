package org.yyubin.infrastructure.persistence.outbox;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.yyubin.application.event.OutboxEvent;

@Repository
public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventEntity, Long> {

    List<OutboxEventEntity> findByStatusOrderByOccurredAtAsc(OutboxEvent.OutboxStatus status, Pageable pageable);

    int deleteByStatusAndOccurredAtBefore(OutboxEvent.OutboxStatus status, Instant cutoff);
}
