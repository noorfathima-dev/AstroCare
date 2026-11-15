package com.astrocare.repository;

import com.astrocare.domain.entity.HealthAlert;
import com.astrocare.domain.enums.AlertStatus;
import com.astrocare.domain.enums.Severity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface HealthAlertRepository extends JpaRepository<HealthAlert, Long> {
    List<HealthAlert> findByAstronautIdOrderByCreatedAtDesc(Long astronautId);
    Page<HealthAlert> findByAstronautId(Long astronautId, Pageable pageable);
    List<HealthAlert> findByStatusOrderByCreatedAtDesc(AlertStatus status);
    List<HealthAlert> findByAstronautIdAndStatusOrderByCreatedAtDesc(Long astronautId, AlertStatus status);
    long countByStatusAndSeverityIn(AlertStatus status, Collection<Severity> severities);
}
