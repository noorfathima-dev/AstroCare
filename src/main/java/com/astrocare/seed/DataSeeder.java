package com.astrocare.seed;

import com.astrocare.domain.entity.*;
import com.astrocare.domain.enums.*;
import com.astrocare.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 * Seeds a fictional 4-person crew, 2 flight surgeons, a mission, 2 h of simulated telemetry per
 * astronaut, plus markers, alerts, a drill, medical history and notifications.
 *
 * Telemetry is SIMULATED around resting in-flight ranges that are consistent with published
 * Inspiration4 / OSDR-style observations (see README for caveats). It is demo data, not clinical data.
 */
@Slf4j
@Component
@Profile("!prod")
@ConditionalOnProperty(prefix = "astrocare.seed", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final int SAMPLES = 24;                 // 24 samples x 5 min = 2 h of telemetry
    private static final int INTERVAL_MIN = 5;
    private static final int ECG_HZ = 250;
    private static final int ECG_SECONDS = 2;
    private static final double RADIATION_BASE_MSV_H = 0.030;   // ~0.7 mSv/day, LEO order of magnitude
    private static final double RADIATION_SAA_BUMP = 0.022;     // South Atlantic Anomaly transit
    private static final double EVA_SUIT_PSI = 4.30;            // EMU-class operating pressure

    private final RoleRepository roleRepo;
    private final UserRepository userRepo;
    private final AstronautRepository astronautRepo;
    private final MissionDetailsRepository missionRepo;
    private final VitalSignRepository vitalRepo;
    private final AnatomicalMarkerRepository markerRepo;
    private final HealthAlertRepository alertRepo;
    private final EmergencyEventRepository emergencyRepo;
    private final MedicalHistoryRepository historyRepo;
    private final NotificationRepository notificationRepo;
    private final PasswordEncoder encoder;
    private final TransactionTemplate tx;

    @Value("${astrocare.seed.default-password}")
    private String defaultPassword;

    /** Static description of one crew member and their resting in-flight baseline. */
    private record CrewSeed(String username, String email, String fullName, String callSign, Sex sex,
                            LocalDate dob, double heightCm, double weightKg, String bloodType,
                            String nationality, String crewRole, boolean eva,
                            int hr, double spo2, int sys, int dia, double tempC, int rr) {}

    @Override
    public void run(String... args) {
        if (userRepo.existsByUsername("surgeon.vance")) {
            log.info("AstroCare seed skipped: data already present.");
            return;
        }
        tx.executeWithoutResult(status -> seedAll());
        log.info("AstroCare seed complete: 2 flight surgeons, 4 astronauts, {} telemetry rows.", vitalRepo.count());
    }

    private void seedAll() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC).withNano(0);
        LocalDate today = now.toLocalDate();

        Role surgeonRole = ensureRole(RoleName.ROLE_FLIGHT_SURGEON, "Ground-based physician with full clinical access");
        Role astronautRole = ensureRole(RoleName.ROLE_ASTRONAUT, "Crew member with access to own health data");

        MissionDetails mission = missionRepo.save(buildMission(now));

        User vance = createUser("surgeon.vance", "elena.vance@astrocare.example", "Dr. Elena Vance", surgeonRole);
        User rivera = createUser("surgeon.rivera", "tomas.rivera@astrocare.example", "Dr. Tomas Rivera", surgeonRole);
        List<User> surgeons = List.of(vance, rivera);

        Map<String, Astronaut> crew = new LinkedHashMap<>();
        Map<String, List<VitalSign>> telemetry = new HashMap<>();
        List<CrewSeed> roster = roster();

        for (int i = 0; i < roster.size(); i++) {
            CrewSeed c = roster.get(i);
            User u = createUser(c.username(), c.email(), c.fullName(), astronautRole);
            Astronaut a = astronautRepo.save(buildAstronaut(c, u, mission));
            crew.put(c.callSign(), a);
            telemetry.put(c.callSign(), vitalRepo.saveAll(buildTelemetry(a, c, i, now)));
            historyRepo.saveAll(buildHistory(a, c, vance, today));
        }

        seedMarkers(crew, vance, now);
        List<HealthAlert> alerts = seedAlerts(crew, telemetry, vance, now);
        EmergencyEvent drill = seedDrill(mission, vance, now);
        seedNotifications(surgeons, crew, alerts, drill, vance, now);
    }

    // ------------------------------------------------------------------ identity

    private Role ensureRole(RoleName name, String description) {
        return roleRepo.findByName(name).orElseGet(() -> roleRepo.save(new Role(name, description)));
    }

    private User createUser(String username, String email, String fullName, Role role) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setFullName(fullName);
        u.setPasswordHash(encoder.encode(defaultPassword));
        u.setEnabled(true);
        u.getRoles().add(role);
        return userRepo.save(u);
    }

    // ------------------------------------------------------------------ mission and crew

    private List<CrewSeed> roster() {
        // Resting in-flight baselines (HR bpm, SpO2 %, BP mmHg, core temp C, RR /min)
        return List.of(
            new CrewSeed("astro.okonkwo", "mara.okonkwo@astrocare.example", "Cdr. Mara Okonkwo", "ATLAS", Sex.FEMALE,
                LocalDate.of(1985, 3, 14), 168.0, 64.5, "O+", "Nigerian", "Commander", false,
                64, 97.5, 112, 72, 36.90, 14),
            new CrewSeed("astro.menon", "ravi.menon@astrocare.example", "Dr. Ravi Menon", "HELIX", Sex.MALE,
                LocalDate.of(1988, 9, 2), 176.0, 74.0, "A+", "Indian", "Mission Specialist / Medical Officer", true,
                60, 98.0, 108, 68, 36.80, 13),
            new CrewSeed("astro.voss", "lena.voss@astrocare.example", "Lena Voss", "ZENITH", Sex.FEMALE,
                LocalDate.of(1992, 6, 21), 165.0, 58.0, "B+", "German", "Pilot", false,
                72, 97.0, 114, 74, 37.00, 15),
            new CrewSeed("astro.arai", "kenji.arai@astrocare.example", "Kenji Arai", "ORBIT", Sex.MALE,
                LocalDate.of(1981, 11, 30), 180.0, 82.0, "O-", "Japanese", "Mission Specialist", true,
                68, 97.2, 118, 76, 36.90, 14)
        );
    }

    private MissionDetails buildMission(LocalDateTime now) {
        MissionDetails m = new MissionDetails();
        m.setMissionCode("AC-ORB-01");
        m.setName("AstroCare Orbital Demonstration 1");
        m.setVehicle("Aegis-1 crewed capsule (fictional)");
        m.setOrbitAltitudeKm(575.0);
        m.setOrbitInclinationDeg(51.6);
        m.setLaunchAt(now.minusHours(36));
        m.setPlannedEndAt(now.minusHours(36).plusDays(3));
        m.setStatus(MissionStatus.ACTIVE);
        m.setObjectives("Validate continuous physiological telemetry, radiation dosimetry and "
                + "countermeasure workflows for short-duration high-altitude LEO flight.");
        return m;
    }

    private Astronaut buildAstronaut(CrewSeed c, User user, MissionDetails mission) {
        Astronaut a = new Astronaut();
        a.setUser(user);
        a.setMission(mission);
        a.setCallSign(c.callSign());
        a.setFullName(c.fullName());
        a.setDateOfBirth(c.dob());
        a.setSex(c.sex());
        a.setHeightCm(c.heightCm());
        a.setBaselineWeightKg(c.weightKg());
        a.setBloodType(c.bloodType());
        a.setNationality(c.nationality());
        a.setCrewRole(c.crewRole());
        a.setEvaQualified(c.eva());
        a.setStatus(AstronautStatus.IN_FLIGHT);
        a.setBaselineHeartRateBpm(c.hr());
        a.setBaselineSpo2Pct(c.spo2());
        a.setBaselineSystolicMmHg(c.sys());
        a.setBaselineDiastolicMmHg(c.dia());
        a.setBaselineCoreTempC(c.tempC());
        a.setBaselineRespRateBpm(c.rr());
        return a;
    }

    // ------------------------------------------------------------------ telemetry

    private List<VitalSign> buildTelemetry(Astronaut a, CrewSeed c, int crewIndex, LocalDateTime end) {
        Random rnd = new Random(2026L + crewIndex);          // deterministic per astronaut
        List<VitalSign> out = new ArrayList<>(SAMPLES);

        for (int i = 0; i < SAMPLES; i++) {
            LocalDateTime ts = end.minusMinutes((long) INTERVAL_MIN * (SAMPLES - 1 - i));
            boolean suitCheck = c.eva() && i >= 18 && i <= 21;       // pressurised-suit check window
            double saa = (i >= 10 && i <= 13) ? RADIATION_SAA_BUMP : 0.0;

            int hr = (int) Math.round(gauss(rnd, c.hr() + (suitCheck ? 40 : 0), 3.0));
            int dia = (int) Math.round(gauss(rnd, c.dia(), 3.0));
            int sys = (int) Math.round(gauss(rnd, c.sys() + (suitCheck ? 8 : 0), 4.0));
            if (sys - dia < 25) sys = dia + 25;

            VitalSign v = new VitalSign();
            v.setAstronaut(a);
            v.setRecordedAt(ts);
            v.setHeartRateBpm(hr);
            v.setSpo2Pct(round(clamp(gauss(rnd, c.spo2(), 0.5), 93.0, 100.0), 1));
            v.setSystolicMmHg(sys);
            v.setDiastolicMmHg(dia);
            v.setCoreTempC(round(gauss(rnd, c.tempC() + (suitCheck ? 0.3 : 0.0), 0.08), 2));
            v.setRespRateBpm((int) clamp(Math.round(gauss(rnd, c.rr() + (suitCheck ? 4 : 0), 1.2)), 8, 30));
            v.setEcgLeadIi(ecgLeadII(hr, rnd));
            v.setEcgSampleRateHz(ECG_HZ);
            v.setSuitPressurePsi(suitCheck ? round(gauss(rnd, EVA_SUIT_PSI, 0.04), 2) : null);
            v.setRadiationMsvH(round(Math.max(0.005, gauss(rnd, RADIATION_BASE_MSV_H + saa, 0.003)), 6));
            v.setSource(VitalSource.SIMULATED);
            out.add(v);
        }
        return out;
    }

    /** Synthetic Lead II: sum of Gaussian P, Q, R, S, T waves per beat, in millivolts. */
    private double[] ecgLeadII(int hrBpm, Random rnd) {
        int n = ECG_HZ * ECG_SECONDS;
        double period = 60.0 / Math.max(hrBpm, 30);
        double[] wave = new double[n];
        for (int i = 0; i < n; i++) {
            double t = i / (double) ECG_HZ;
            double phase = (t % period) / period;
            double mv = bump(phase, 0.160, 0.14, 0.025)     // P
                      + bump(phase, 0.285, -0.12, 0.008)    // Q
                      + bump(phase, 0.300, 1.10, 0.010)     // R
                      + bump(phase, 0.315, -0.25, 0.010)    // S
                      + bump(phase, 0.550, 0.30, 0.045)     // T
                      + rnd.nextGaussian() * 0.01;          // baseline noise
            wave[i] = Math.round(mv * 1000.0) / 1000.0;
        }
        return wave;
    }

    // ------------------------------------------------------------------ clinical records

    private List<MedicalHistory> buildHistory(Astronaut a, CrewSeed c, User by, LocalDate today) {
        List<MedicalHistory> list = new ArrayList<>();
        list.add(history(a, MedicalRecordType.PRE_FLIGHT_PHYSICAL, "Pre-flight medical clearance",
                "Cleared for flight. Resting ECG normal sinus rhythm, no orthostatic intolerance on tilt test.",
                today.minusDays(45), by));
        switch (c.callSign()) {
            case "HELIX" -> list.add(history(a, MedicalRecordType.DIAGNOSIS, "Mild lumbar strain (resolved)",
                    "Recurrent mild lumbar strain in 2023, resolved with physiotherapy. Monitor for in-flight back pain.",
                    today.minusDays(400), by));
            case "ATLAS" -> list.add(history(a, MedicalRecordType.IMMUNIZATION, "Pre-flight immunisation series",
                    "Influenza, Tdap and hepatitis B boosters completed.", today.minusDays(60), by));
            case "ZENITH" -> list.add(history(a, MedicalRecordType.ALLERGY, "Seasonal allergic rhinitis",
                    "Mild seasonal rhinitis. No known drug allergies.", today.minusDays(45), by));
            case "ORBIT" -> list.add(history(a, MedicalRecordType.COUNTERMEASURE, "Resistive exercise protocol",
                    "Enrolled in daily resistive and aerobic exercise countermeasure schedule.", today.minusDays(30), by));
            default -> { }
        }
        return list;
    }

    private MedicalHistory history(Astronaut a, MedicalRecordType type, String title, String desc,
                                   LocalDate on, User by) {
        MedicalHistory h = new MedicalHistory();
        h.setAstronaut(a);
        h.setRecordType(type);
        h.setTitle(title);
        h.setDescription(desc);
        h.setRecordedOn(on);
        h.setRecordedBy(by);
        return h;
    }

    private void seedMarkers(Map<String, Astronaut> crew, User by, LocalDateTime now) {
        markerRepo.save(marker(crew.get("HELIX"), BodyZone.LOWER_BACK, 0.50, 0.62, Severity.MODERATE,
                "Spaceflight-associated back pain since flight day 1. Stretching countermeasure prescribed.",
                false, by, now.minusHours(20), null));
        markerRepo.save(marker(crew.get("ATLAS"), BodyZone.EYES, 0.47, 0.09, Severity.LOW,
                "Mild transient visual blur reported. Monitor for neuro-ocular changes, repeat check in 24 h.",
                false, by, now.minusHours(8), null));
        markerRepo.save(marker(crew.get("ZENITH"), BodyZone.HEAD, 0.50, 0.07, Severity.LOW,
                "Transient headache consistent with space adaptation. Resolved after hydration and rest.",
                true, by, now.minusHours(30), now.minusHours(26)));
    }

    private AnatomicalMarker marker(Astronaut a, BodyZone zone, double x, double y, Severity sev, String note,
                                    boolean resolved, User by, LocalDateTime created, LocalDateTime resolvedAt) {
        AnatomicalMarker m = new AnatomicalMarker();
        m.setAstronaut(a);
        m.setBodyZone(zone);
        m.setCoordX(x);
        m.setCoordY(y);
        m.setSeverity(sev);
        m.setClinicalNote(note);
        m.setResolved(resolved);
        m.setCreatedBy(by);
        m.setCreatedAt(created);
        if (resolved) {
            m.setResolvedBy(by);
            m.setResolvedAt(resolvedAt);
        }
        return m;
    }

    // ------------------------------------------------------------------ alerts, emergency, notifications

    private List<HealthAlert> seedAlerts(Map<String, Astronaut> crew, Map<String, List<VitalSign>> telemetry,
                                         User ackBy, LocalDateTime now) {
        // 1) Radiation spike during the simulated SAA window, already acknowledged
        VitalSign saaSample = telemetry.get("ATLAS").get(11);
        HealthAlert radiation = new HealthAlert();
        radiation.setAstronaut(crew.get("ATLAS"));
        radiation.setVitalSign(saaSample);
        radiation.setAlertType(AlertType.RADIATION_SPIKE);
        radiation.setSeverity(Severity.MODERATE);
        radiation.setStatus(AlertStatus.ACKNOWLEDGED);
        radiation.setMetricName("radiation_msv_h");
        radiation.setObservedValue(saaSample.getRadiationMsvH());
        radiation.setThresholdValue(0.045);
        radiation.setMessage("Dose rate elevated during South Atlantic Anomaly transit. Crew advised to limit time in exposed areas.");
        radiation.setCreatedAt(saaSample.getRecordedAt());
        radiation.setAcknowledgedBy(ackBy);
        radiation.setAcknowledgedAt(saaSample.getRecordedAt().plusMinutes(6));

        // 2) Transient tachycardia on ORBIT during the suit-pressurisation check, still open
        VitalSign peak = telemetry.get("ORBIT").stream()
                .max(Comparator.comparingInt(VitalSign::getHeartRateBpm)).orElseThrow();
        HealthAlert tachy = new HealthAlert();
        tachy.setAstronaut(crew.get("ORBIT"));
        tachy.setVitalSign(peak);
        tachy.setAlertType(AlertType.TACHYCARDIA);
        tachy.setSeverity(Severity.LOW);
        tachy.setStatus(AlertStatus.OPEN);
        tachy.setMetricName("heart_rate_bpm");
        tachy.setObservedValue(peak.getHeartRateBpm().doubleValue());
        tachy.setThresholdValue(100.0);
        tachy.setMessage("Heart rate above 100 bpm during suit pressurisation check. Verify recovery to resting baseline.");
        tachy.setCreatedAt(peak.getRecordedAt());

        return alertRepo.saveAll(List.of(radiation, tachy));
    }

    private EmergencyEvent seedDrill(MissionDetails mission, User declaredBy, LocalDateTime now) {
        EmergencyEvent e = new EmergencyEvent();
        e.setMission(mission);
        e.setAstronaut(null);                                  // crew-wide
        e.setEventType(EmergencyType.DECOMPRESSION);
        e.setSeverity(Severity.HIGH);
        e.setStatus(EmergencyStatus.RESOLVED);
        e.setDrill(true);
        e.setDescription("Scheduled rapid-decompression response drill.");
        e.setTriggeredAt(now.minusHours(20));
        e.setResolvedAt(now.minusHours(20).plusMinutes(25));
        e.setDeclaredBy(declaredBy);
        e.setResolutionNotes("All four crew donned pressure suits and reached the safe configuration within target time. No physiological flags.");
        return emergencyRepo.save(e);
    }

    private void seedNotifications(List<User> surgeons, Map<String, Astronaut> crew, List<HealthAlert> alerts,
                                   EmergencyEvent drill, User ackBy, LocalDateTime now) {
        List<Notification> batch = new ArrayList<>();

        for (HealthAlert alert : alerts) {
            for (User surgeon : surgeons) {
                boolean seen = alert.getStatus() == AlertStatus.ACKNOWLEDGED && surgeon.getId().equals(ackBy.getId());
                batch.add(notification(surgeon, NotificationType.ALERT,
                        alert.getAlertType() + " - " + alert.getAstronaut().getCallSign(),
                        alert.getMessage(), alert, null, seen, alert.getCreatedAt().plusSeconds(5)));
            }
        }
        for (Astronaut a : crew.values()) {
            batch.add(notification(a.getUser(), NotificationType.EMERGENCY, "Decompression drill completed",
                    "Drill closed out with no physiological flags. Thank you, crew.", null, drill, true,
                    now.minusHours(19)));
        }
        batch.add(notification(crew.get("ORBIT").getUser(), NotificationType.COUNTERMEASURE,
                "Resistive exercise session scheduled",
                "Your next resistive exercise block is due within the hour. Hydrate beforehand.",
                null, null, false, now.minusMinutes(10)));
        batch.add(notification(crew.get("HELIX").getUser(), NotificationType.COUNTERMEASURE,
                "Lumbar stretching protocol",
                "Flight Surgeon recommends the lumbar stretching routine twice daily while the back marker is open.",
                null, null, false, now.minusHours(19)));

        notificationRepo.saveAll(batch);
    }

    private Notification notification(User user, NotificationType type, String title, String message,
                                      HealthAlert alert, EmergencyEvent emergency, boolean seen, LocalDateTime created) {
        Notification n = new Notification();
        n.setUser(user);
        n.setNotificationType(type);
        n.setTitle(title.length() > 150 ? title.substring(0, 150) : title);
        n.setMessage(message.length() > 500 ? message.substring(0, 500) : message);
        n.setHealthAlert(alert);
        n.setEmergencyEvent(emergency);
        n.setSeen(seen);
        n.setCreatedAt(created);
        if (seen) n.setSeenAt(created.plusMinutes(2));
        return n;
    }

    // ------------------------------------------------------------------ math helpers

    private static double gauss(Random rnd, double mean, double sd) {
        return mean + rnd.nextGaussian() * sd;
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static double round(double v, int places) {
        double f = Math.pow(10, places);
        return Math.round(v * f) / f;
    }

    private static double bump(double x, double mu, double amp, double sigma) {
        double d = x - mu;
        return amp * Math.exp(-(d * d) / (2 * sigma * sigma));
    }
}
