package com.astrocare.domain.entity;

import com.astrocare.domain.enums.AstronautStatus;
import com.astrocare.domain.enums.Sex;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "astronauts")
public class Astronaut extends CreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id")
    private MissionDetails mission;

    @Column(name = "call_sign", nullable = false, unique = true, length = 32)
    private String callSign;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "sex", nullable = false, length = 10)
    private Sex sex;

    @Column(name = "height_cm")
    private Double heightCm;

    @Column(name = "baseline_weight_kg")
    private Double baselineWeightKg;

    @Column(name = "blood_type", length = 3)
    private String bloodType;

    @Column(name = "nationality", length = 60)
    private String nationality;

    @Column(name = "crew_role", length = 60)
    private String crewRole;

    @Column(name = "eva_qualified", nullable = false)
    private boolean evaQualified;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private AstronautStatus status = AstronautStatus.PRE_FLIGHT;

    // Per-astronaut physiological baselines, used for alert thresholds / deviation scoring
    @Column(name = "baseline_heart_rate_bpm")
    private Integer baselineHeartRateBpm;

    @Column(name = "baseline_spo2_pct")
    private Double baselineSpo2Pct;

    @Column(name = "baseline_systolic_mmhg")
    private Integer baselineSystolicMmHg;

    @Column(name = "baseline_diastolic_mmhg")
    private Integer baselineDiastolicMmHg;

    @Column(name = "baseline_core_temp_c")
    private Double baselineCoreTempC;

    @Column(name = "baseline_resp_rate_bpm")
    private Integer baselineRespRateBpm;
}
