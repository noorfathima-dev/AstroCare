package com.astrocare.repository;

import com.astrocare.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Notification> findByUserIdAndSeenFalseOrderByCreatedAtDesc(Long userId);
    long countByUserIdAndSeenFalse(Long userId);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update Notification n set n.seen = true, n.seenAt = :now where n.user.id = :userId and n.seen = false")
    int markAllSeen(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}
