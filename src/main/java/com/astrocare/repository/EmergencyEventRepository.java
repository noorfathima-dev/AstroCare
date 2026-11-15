package com.astrocare.repository;

import com.astrocare.entity.EmergencyEvent;
import com.astrocare.entity.EmergencyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmergencyEventRepository extends JpaRepository<EmergencyEvent, Long> {
    List<EmergencyEvent> findByStatusNotOrderByTriggeredAtDesc(EmergencyStatus excludedStatus);
    List<EmergencyEvent> findByMissionIdOrderByTriggeredAtDesc(Long missionId);
    List<EmergencyEvent> findByAstronautIdOrderByTriggeredAtDesc(Long astronautId);
    List<EmergencyEvent> findByDrillFalseAndStatus(EmergencyStatus status);
}
