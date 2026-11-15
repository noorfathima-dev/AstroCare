package com.astrocare.domain.entity;

import com.astrocare.domain.enums.EmergencyStatus;
import com.astrocare.domain.enums.EmergencyType;
import com.astrocare.domain.enums.Severity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "emergency_events")
public class EmergencyEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id")
    private MissionDetails mission;

    /** Null means a crew-wide event. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "astronaut_id")
    private Astronaut astronaut;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 24)
    private EmergencyType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 10)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 12)
    private EmergencyStatus status = EmergencyStatus.ACTIVE;

    @Column(name = "is_drill", nullable = false)
    private boolean drill;

    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    @Column(name = "triggered_at", nullable = false)
    private LocalDateTime triggeredAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "declared_by")
    private User declaredBy;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @PrePersist
    protected void onCreate() {
        if (triggeredAt == null) triggeredAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
