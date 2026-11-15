package com.astrocare.domain.entity;

import com.astrocare.domain.converter.DoubleArrayJsonConverter;
import com.astrocare.domain.enums.VitalSource;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "vital_signs")
public class VitalSign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "astronaut_id", nullable = false)
    private Astronaut astronaut;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(name = "heart_rate_bpm", nullable = false)
    private Integer heartRateBpm;

    @Column(name = "spo2_pct", nullable = false)
    private Double spo2Pct;

    @Column(name = "systolic_mmhg", nullable = false)
    private Integer systolicMmHg;

    @Column(name = "diastolic_mmhg", nullable = false)
    private Integer diastolicMmHg;

    @Column(name = "core_temp_c", nullable = false)
    private Double coreTempC;

    @Column(name = "resp_rate_bpm", nullable = false)
    private Integer respRateBpm;

    /** ECG Lead II waveform in millivolts, stored as a JSON array. */
    @Convert(converter = DoubleArrayJsonConverter.class)
    @Column(name = "ecg_lead_ii", columnDefinition = "JSON")
    private double[] ecgLeadIi;

    @Column(name = "ecg_sample_rate_hz")
    private Integer ecgSampleRateHz;

    /** EVA suit pressure in PSI, null when not suited. */
    @Column(name = "suit_pressure_psi")
    private Double suitPressurePsi;

    /** Ionizing radiation dose rate in mSv/h. */
    @Column(name = "radiation_msv_h")
    private Double radiationMsvH;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 16)
    private VitalSource source = VitalSource.SENSOR_SUITE;
}
