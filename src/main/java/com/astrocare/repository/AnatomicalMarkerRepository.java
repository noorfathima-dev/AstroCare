package com.astrocare.repository;

import com.astrocare.entity.AnatomicalMarker;
import com.astrocare.entity.BodyZone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnatomicalMarkerRepository extends JpaRepository<AnatomicalMarker, Long> {
    List<AnatomicalMarker> findByAstronautIdOrderByCreatedAtDesc(Long astronautId);
    List<AnatomicalMarker> findByAstronautIdAndResolvedFalseOrderByCreatedAtDesc(Long astronautId);
    List<AnatomicalMarker> findByBodyZoneAndResolvedFalse(BodyZone bodyZone);
    long countByAstronautIdAndResolvedFalse(Long astronautId);
}
