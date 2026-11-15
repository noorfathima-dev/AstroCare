package com.astrocare.repository;

import com.astrocare.domain.entity.MissionDetails;
import com.astrocare.domain.enums.MissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MissionDetailsRepository extends JpaRepository<MissionDetails, Long> {
    Optional<MissionDetails> findByMissionCode(String missionCode);
    List<MissionDetails> findByStatus(MissionStatus status);
}
