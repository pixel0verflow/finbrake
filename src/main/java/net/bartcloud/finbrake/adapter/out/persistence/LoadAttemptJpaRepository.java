package net.bartcloud.finbrake.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoadAttemptJpaRepository extends JpaRepository<LoadAttemptEntity, Long> {

    boolean existsByCustomerIdAndAttemptId(String customerId, String attemptId);

    @Query(
            """
            select coalesce(sum(e.amount), 0)
            from LoadAttemptEntity e
            where e.customerId = :customerId
              and e.accepted = true
              and e.attemptTime >= :from
              and e.attemptTime < :to
            """)
    BigDecimal sumAcceptedAmount(
            @Param("customerId") String customerId, @Param("from") Instant from, @Param("to") Instant to);

    @Query(
            """
            select count(e)
            from LoadAttemptEntity e
            where e.customerId = :customerId
              and e.accepted = true
              and e.attemptTime >= :from
              and e.attemptTime < :to
            """)
    long countAccepted(@Param("customerId") String customerId, @Param("from") Instant from, @Param("to") Instant to);
}
