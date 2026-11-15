package com.astrocare.repository;

import com.astrocare.domain.entity.MedicalHistory;
import com.astrocare.domain.enums.MedicalRecordType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicalHistoryRepository extends JpaRepository<MedicalHistory, Long> {
    List<MedicalHistory> findByAstronautIdOrderByRecordedOnDesc(Long astronautId);
    List<MedicalHistory> findByAstronautIdAndRecordTypeOrderByRecordedOnDesc(Long astronautId, MedicalRecordType recordType);
}
