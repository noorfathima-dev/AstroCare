package com.astrocare.repository;

import com.astrocare.domain.entity.VitalSign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VitalSignRepository extends JpaRepository<VitalSign, Long> {

    /** Light projection: everything except the ECG array, ideal for trend charts. */
    interface VitalSummary {
        LocalDateTime getRecordedAt();
        Integer getHeartRateBpm();
        Double getSpo2Pct();
        Integer getSystolicMmHg();
        Integer getDiastolicMmHg();
        Double getCoreTempC();
        Integer getRespRateBpm();
        Double getSuitPressurePsi();
        Double getRadiationMsvH();
    }

    interface VitalAverages {
        Double getAvgHeartRate();
        Double getAvgSpo2();
        Double getAvgSystolic();
        Double getAvgDiastolic();
        Double getAvgCoreTemp();
        Double getAvgRespRate();
        Double getPeakRadiation();
    }

    Optional<VitalSign> findFirstByAstronautIdOrderByRecordedAtDesc(Long astronautId);

    Page<VitalSign> findByAstronautId(Long astronautId, Pageable pageable);

    List<VitalSign> findByAstronautIdAndRecordedAtBetweenOrderByRecordedAtAsc(
            Long astronautId, LocalDateTime from, LocalDateTime to);

    long countByAstronautId(Long astronautId);

    @Query("select v.recordedAt as recordedAt, v.heartRateBpm as heartRateBpm, v.spo2Pct as spo2Pct, "
            + "v.systolicMmHg as systolicMmHg, v.diastolicMmHg as diastolicMmHg, v.coreTempC as coreTempC, "
            + "v.respRateBpm as respRateBpm, v.suitPressurePsi as suitPressurePsi, v.radiationMsvH as radiationMsvH "
            + "from VitalSign v where v.astronaut.id = :astronautId and v.recordedAt >= :since "
            + "order by v.recordedAt asc")
    List<VitalSummary> findSummariesSince(@Param("astronautId") Long astronautId,
                                          @Param("since") LocalDateTime since);

    @Query("select avg(v.heartRateBpm) as avgHeartRate, avg(v.spo2Pct) as avgSpo2, "
            + "avg(v.systolicMmHg) as avgSystolic, avg(v.diastolicMmHg) as avgDiastolic, "
            + "avg(v.coreTempC) as avgCoreTemp, avg(v.respRateBpm) as avgRespRate, "
            + "max(v.radiationMsvH) as peakRadiation "
            + "from VitalSign v where v.astronaut.id = :astronautId and v.recordedAt between :from and :to")
    VitalAverages averagesBetween(@Param("astronautId") Long astronautId,
                                  @Param("from") LocalDateTime from,
                                  @Param("to") LocalDateTime to);
}
