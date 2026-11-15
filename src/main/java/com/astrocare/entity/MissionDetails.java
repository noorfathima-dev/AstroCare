package com.astrocare.entity;

import com.astrocare.entity.MissionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "mission_details")
public class MissionDetails extends CreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mission_code", nullable = false, unique = true, length = 32)
    private String missionCode;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "vehicle", length = 100)
    private String vehicle;

    @Column(name = "orbit_altitude_km")
    private Double orbitAltitudeKm;

    @Column(name = "orbit_inclination_deg")
    private Double orbitInclinationDeg;

    @Column(name = "launch_at")
    private LocalDateTime launchAt;

    @Column(name = "planned_end_at")
    private LocalDateTime plannedEndAt;

    @Column(name = "actual_end_at")
    private LocalDateTime actualEndAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private MissionStatus status = MissionStatus.PLANNED;

    @Column(name = "objectives", columnDefinition = "TEXT")
    private String objectives;
}
