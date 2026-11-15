package com.astrocare.domain.entity;

import com.astrocare.domain.enums.MedicalRecordType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "medical_history")
public class MedicalHistory extends CreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "astronaut_id", nullable = false)
    private Astronaut astronaut;

    @Enumerated(EnumType.STRING)
    @Column(name = "record_type", nullable = false, length = 24)
    private MedicalRecordType recordType;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "recorded_on", nullable = false)
    private LocalDate recordedOn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by")
    private User recordedBy;
}
