package com.astrocare.repository;

import com.astrocare.domain.entity.Astronaut;
import com.astrocare.domain.enums.AstronautStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AstronautRepository extends JpaRepository<Astronaut, Long> {
    Optional<Astronaut> findByCallSign(String callSign);
    Optional<Astronaut> findByUserId(Long userId);
    Optional<Astronaut> findByUserUsername(String username);
    boolean existsByCallSign(String callSign);
    List<Astronaut> findByMissionId(Long missionId);
    List<Astronaut> findByStatus(AstronautStatus status);
}
