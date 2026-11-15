package com.astrocare.domain.entity;

import com.astrocare.domain.enums.BodyZone;
import com.astrocare.domain.enums.Severity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "anatomical_markers")
public class AnatomicalMarker extends CreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "astronaut_id", nullable = false)
    private Astronaut astronaut;

    @Enumerated(EnumType.STRING)
    @Column(name = "body_zone", nullable = false, length = 24)
    private BodyZone bodyZone;

    /** Normalised 0..1 position on the anatomical diagram. */
    @Column(name = "coord_x", nullable = false)
    private Double coordX;

    @Column(name = "coord_y", nullable = false)
    private Double coordY;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 10)
    private Severity severity;

    @Column(name = "clinical_note", length = 1000)
    private String clinicalNote;

    @Column(name = "resolved", nullable = false)
    private boolean resolved;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}
