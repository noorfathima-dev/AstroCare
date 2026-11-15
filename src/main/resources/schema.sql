-- =====================================================================
-- AstroCare - Astronaut Health Monitoring & Spaceflight Countermeasure System
-- MySQL 8.0.16+ / InnoDB / utf8mb4
-- All timestamps are stored in UTC.
-- Standalone use: uncomment the next two lines.
-- CREATE DATABASE IF NOT EXISTS astrocare CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
-- USE astrocare;
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. Identity and access
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS roles (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  name        VARCHAR(32)  NOT NULL,
  description VARCHAR(255) NULL,
  PRIMARY KEY (id),
  CONSTRAINT uq_roles_name  UNIQUE (name),
  CONSTRAINT chk_roles_name CHECK (name IN ('ROLE_FLIGHT_SURGEON','ROLE_ASTRONAUT'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS users (
  id                    BIGINT       NOT NULL AUTO_INCREMENT,
  username              VARCHAR(50)  NOT NULL,
  email                 VARCHAR(120) NOT NULL,
  password_hash         VARCHAR(100) NOT NULL,
  full_name             VARCHAR(120) NOT NULL,
  enabled               BOOLEAN      NOT NULL DEFAULT TRUE,
  failed_login_attempts INT          NOT NULL DEFAULT 0,
  last_login_at         DATETIME(3)  NULL,
  created_at            DATETIME(3)  NOT NULL,
  updated_at            DATETIME(3)  NULL,
  PRIMARY KEY (id),
  CONSTRAINT uq_users_username UNIQUE (username),
  CONSTRAINT uq_users_email    UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_roles (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  KEY idx_user_roles_role (role_id),
  CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- 2. Mission and crew
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS mission_details (
  id                    BIGINT        NOT NULL AUTO_INCREMENT,
  mission_code          VARCHAR(32)   NOT NULL,
  name                  VARCHAR(150)  NOT NULL,
  vehicle               VARCHAR(100)  NULL,
  orbit_altitude_km     DECIMAL(7,2)  NULL,
  orbit_inclination_deg DECIMAL(5,2)  NULL,
  launch_at             DATETIME(3)   NULL,
  planned_end_at        DATETIME(3)   NULL,
  actual_end_at         DATETIME(3)   NULL,
  status                VARCHAR(16)   NOT NULL DEFAULT 'PLANNED',
  objectives            TEXT          NULL,
  created_at            DATETIME(3)   NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT uq_mission_code   UNIQUE (mission_code),
  CONSTRAINT chk_mission_status CHECK (status IN ('PLANNED','ACTIVE','COMPLETED','ABORTED')),
  KEY idx_mission_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS astronauts (
  id                     BIGINT       NOT NULL AUTO_INCREMENT,
  user_id                BIGINT       NOT NULL,
  mission_id             BIGINT       NULL,
  call_sign              VARCHAR(32)  NOT NULL,
  full_name              VARCHAR(120) NOT NULL,
  date_of_birth          DATE         NOT NULL,
  sex                    VARCHAR(10)  NOT NULL,
  height_cm              DECIMAL(5,1) NULL,
  baseline_weight_kg     DECIMAL(5,2) NULL,
  blood_type             VARCHAR(3)   NULL,
  nationality            VARCHAR(60)  NULL,
  crew_role              VARCHAR(60)  NULL,
  eva_qualified          BOOLEAN      NOT NULL DEFAULT FALSE,
  status                 VARCHAR(16)  NOT NULL DEFAULT 'PRE_FLIGHT',
  baseline_heart_rate_bpm SMALLINT    NULL,
  baseline_spo2_pct      DECIMAL(4,1) NULL,
  baseline_systolic_mmhg SMALLINT     NULL,
  baseline_diastolic_mmhg SMALLINT    NULL,
  baseline_core_temp_c   DECIMAL(4,2) NULL,
  baseline_resp_rate_bpm SMALLINT     NULL,
  created_at             DATETIME(3)  NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT uq_astronauts_user      UNIQUE (user_id),
  CONSTRAINT uq_astronauts_call_sign UNIQUE (call_sign),
  CONSTRAINT chk_astronauts_sex      CHECK (sex IN ('FEMALE','MALE','OTHER')),
  CONSTRAINT chk_astronauts_status   CHECK (status IN ('PRE_FLIGHT','IN_FLIGHT','POST_FLIGHT','GROUNDED')),
  KEY idx_astronauts_mission (mission_id),
  KEY idx_astronauts_status  (status),
  CONSTRAINT fk_astronauts_user    FOREIGN KEY (user_id)    REFERENCES users (id)           ON DELETE CASCADE,
  CONSTRAINT fk_astronauts_mission FOREIGN KEY (mission_id) REFERENCES mission_details (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- 3. Telemetry (high volume: BIGINT PK, composite time-series index)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS vital_signs (
  id                 BIGINT        NOT NULL AUTO_INCREMENT,
  astronaut_id       BIGINT        NOT NULL,
  recorded_at        DATETIME(3)   NOT NULL,
  heart_rate_bpm     SMALLINT      NOT NULL,
  spo2_pct           DECIMAL(4,1)  NOT NULL,
  systolic_mmhg      SMALLINT      NOT NULL,
  diastolic_mmhg     SMALLINT      NOT NULL,
  core_temp_c        DECIMAL(4,2)  NOT NULL,
  resp_rate_bpm      SMALLINT      NOT NULL,
  ecg_lead_ii        JSON          NULL,          -- array of mV samples
  ecg_sample_rate_hz SMALLINT      NULL,
  suit_pressure_psi  DECIMAL(5,2)  NULL,          -- NULL when not suited / not pressurised
  radiation_msv_h    DECIMAL(10,6) NULL,          -- ionizing radiation dose rate, mSv per hour
  source             VARCHAR(16)   NOT NULL DEFAULT 'SENSOR_SUITE',
  PRIMARY KEY (id),
  CONSTRAINT chk_vs_hr     CHECK (heart_rate_bpm BETWEEN 0 AND 300),
  CONSTRAINT chk_vs_spo2   CHECK (spo2_pct BETWEEN 0 AND 100),
  CONSTRAINT chk_vs_sys    CHECK (systolic_mmhg BETWEEN 40 AND 300),
  CONSTRAINT chk_vs_dia    CHECK (diastolic_mmhg BETWEEN 20 AND 200),
  CONSTRAINT chk_vs_temp   CHECK (core_temp_c BETWEEN 25 AND 45),
  CONSTRAINT chk_vs_rr     CHECK (resp_rate_bpm BETWEEN 0 AND 80),
  CONSTRAINT chk_vs_suit   CHECK (suit_pressure_psi IS NULL OR suit_pressure_psi BETWEEN 0 AND 20),
  CONSTRAINT chk_vs_rad    CHECK (radiation_msv_h IS NULL OR radiation_msv_h >= 0),
  CONSTRAINT chk_vs_source CHECK (source IN ('SENSOR_SUITE','MANUAL','SIMULATED')),
  KEY idx_vs_astronaut_time (astronaut_id, recorded_at),
  KEY idx_vs_recorded_at    (recorded_at),
  CONSTRAINT fk_vs_astronaut FOREIGN KEY (astronaut_id) REFERENCES astronauts (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS anatomical_markers (
  id            BIGINT        NOT NULL AUTO_INCREMENT,
  astronaut_id  BIGINT        NOT NULL,
  body_zone     VARCHAR(24)   NOT NULL,
  coord_x       DECIMAL(6,5)  NOT NULL,          -- normalised 0..1 on the anatomical diagram
  coord_y       DECIMAL(6,5)  NOT NULL,
  severity      VARCHAR(10)   NOT NULL,
  clinical_note VARCHAR(1000) NULL,
  resolved      BOOLEAN       NOT NULL DEFAULT FALSE,
  created_by    BIGINT        NULL,
  created_at    DATETIME(3)   NOT NULL,
  resolved_by   BIGINT        NULL,
  resolved_at   DATETIME(3)   NULL,
  PRIMARY KEY (id),
  CONSTRAINT chk_am_zone CHECK (body_zone IN ('HEAD','EYES','NECK','CHEST','UPPER_BACK','LOWER_BACK','ABDOMEN','PELVIS',
    'LEFT_SHOULDER','RIGHT_SHOULDER','LEFT_ARM','RIGHT_ARM','LEFT_HAND','RIGHT_HAND','LEFT_LEG','RIGHT_LEG','LEFT_FOOT','RIGHT_FOOT')),
  CONSTRAINT chk_am_x   CHECK (coord_x BETWEEN 0 AND 1),
  CONSTRAINT chk_am_y   CHECK (coord_y BETWEEN 0 AND 1),
  CONSTRAINT chk_am_sev CHECK (severity IN ('INFO','LOW','MODERATE','HIGH','CRITICAL')),
  KEY idx_am_astronaut_resolved (astronaut_id, resolved),
  KEY idx_am_zone_resolved      (body_zone, resolved),
  CONSTRAINT fk_am_astronaut   FOREIGN KEY (astronaut_id) REFERENCES astronauts (id) ON DELETE CASCADE,
  CONSTRAINT fk_am_created_by  FOREIGN KEY (created_by)   REFERENCES users (id)      ON DELETE SET NULL,
  CONSTRAINT fk_am_resolved_by FOREIGN KEY (resolved_by)  REFERENCES users (id)      ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- 4. Alerts, emergencies, history, notifications
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS health_alerts (
  id              BIGINT         NOT NULL AUTO_INCREMENT,
  astronaut_id    BIGINT         NOT NULL,
  vital_sign_id   BIGINT         NULL,
  alert_type      VARCHAR(32)    NOT NULL,
  severity        VARCHAR(10)    NOT NULL,
  status          VARCHAR(16)    NOT NULL DEFAULT 'OPEN',
  metric_name     VARCHAR(40)    NULL,
  observed_value  DECIMAL(12,4)  NULL,
  threshold_value DECIMAL(12,4)  NULL,
  message         VARCHAR(500)   NOT NULL,
  created_at      DATETIME(3)    NOT NULL,
  acknowledged_by BIGINT         NULL,
  acknowledged_at DATETIME(3)    NULL,
  resolved_at     DATETIME(3)    NULL,
  PRIMARY KEY (id),
  CONSTRAINT chk_ha_type CHECK (alert_type IN ('TACHYCARDIA','BRADYCARDIA','HYPOXIA','HYPERTENSION','HYPOTENSION',
    'HYPERTHERMIA','HYPOTHERMIA','TACHYPNEA','ARRHYTHMIA','SUIT_PRESSURE_DEVIATION','RADIATION_SPIKE','MANUAL')),
  CONSTRAINT chk_ha_sev    CHECK (severity IN ('INFO','LOW','MODERATE','HIGH','CRITICAL')),
  CONSTRAINT chk_ha_status CHECK (status IN ('OPEN','ACKNOWLEDGED','RESOLVED')),
  KEY idx_ha_astronaut_status_time (astronaut_id, status, created_at),
  KEY idx_ha_status_severity       (status, severity),
  KEY idx_ha_vital                 (vital_sign_id),
  CONSTRAINT fk_ha_astronaut FOREIGN KEY (astronaut_id)    REFERENCES astronauts (id)  ON DELETE CASCADE,
  CONSTRAINT fk_ha_vital     FOREIGN KEY (vital_sign_id)   REFERENCES vital_signs (id) ON DELETE SET NULL,
  CONSTRAINT fk_ha_ack_by    FOREIGN KEY (acknowledged_by) REFERENCES users (id)       ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS emergency_events (
  id               BIGINT       NOT NULL AUTO_INCREMENT,
  mission_id       BIGINT       NULL,
  astronaut_id     BIGINT       NULL,               -- NULL = crew-wide event
  event_type       VARCHAR(24)  NOT NULL,
  severity         VARCHAR(10)  NOT NULL,
  status           VARCHAR(12)  NOT NULL DEFAULT 'ACTIVE',
  is_drill         BOOLEAN      NOT NULL DEFAULT FALSE,
  description      VARCHAR(1000) NOT NULL,
  triggered_at     DATETIME(3)  NOT NULL,
  resolved_at      DATETIME(3)  NULL,
  declared_by      BIGINT       NULL,
  resolution_notes TEXT         NULL,
  PRIMARY KEY (id),
  CONSTRAINT chk_ee_type   CHECK (event_type IN ('DECOMPRESSION','CARDIAC','RESPIRATORY','RADIATION_STORM','FIRE','TOXIC_ATMOSPHERE','TRAUMA','OTHER')),
  CONSTRAINT chk_ee_sev    CHECK (severity IN ('INFO','LOW','MODERATE','HIGH','CRITICAL')),
  CONSTRAINT chk_ee_status CHECK (status IN ('ACTIVE','CONTAINED','RESOLVED')),
  KEY idx_ee_status_time (status, triggered_at),
  KEY idx_ee_mission     (mission_id),
  KEY idx_ee_astronaut   (astronaut_id),
  CONSTRAINT fk_ee_mission     FOREIGN KEY (mission_id)   REFERENCES mission_details (id) ON DELETE SET NULL,
  CONSTRAINT fk_ee_astronaut   FOREIGN KEY (astronaut_id) REFERENCES astronauts (id)      ON DELETE CASCADE,
  CONSTRAINT fk_ee_declared_by FOREIGN KEY (declared_by)  REFERENCES users (id)           ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS medical_history (
  id           BIGINT       NOT NULL AUTO_INCREMENT,
  astronaut_id BIGINT       NOT NULL,
  record_type  VARCHAR(24)  NOT NULL,
  title        VARCHAR(150) NOT NULL,
  description  TEXT         NULL,
  recorded_on  DATE         NOT NULL,
  recorded_by  BIGINT       NULL,
  created_at   DATETIME(3)  NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT chk_mh_type CHECK (record_type IN ('PRE_FLIGHT_PHYSICAL','DIAGNOSIS','ALLERGY','MEDICATION','SURGERY',
    'IMMUNIZATION','COUNTERMEASURE','IN_FLIGHT_NOTE')),
  KEY idx_mh_astronaut_date (astronaut_id, recorded_on),
  KEY idx_mh_type           (record_type),
  CONSTRAINT fk_mh_astronaut   FOREIGN KEY (astronaut_id) REFERENCES astronauts (id) ON DELETE CASCADE,
  CONSTRAINT fk_mh_recorded_by FOREIGN KEY (recorded_by)  REFERENCES users (id)      ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS notifications (
  id                 BIGINT       NOT NULL AUTO_INCREMENT,
  user_id            BIGINT       NOT NULL,
  notification_type  VARCHAR(16)  NOT NULL,
  title              VARCHAR(150) NOT NULL,
  message            VARCHAR(500) NOT NULL,
  health_alert_id    BIGINT       NULL,
  emergency_event_id BIGINT       NULL,
  is_seen            BOOLEAN      NOT NULL DEFAULT FALSE,
  created_at         DATETIME(3)  NOT NULL,
  seen_at            DATETIME(3)  NULL,
  PRIMARY KEY (id),
  CONSTRAINT chk_nt_type CHECK (notification_type IN ('ALERT','EMERGENCY','COUNTERMEASURE','SYSTEM')),
  KEY idx_nt_user_seen_time (user_id, is_seen, created_at),
  KEY idx_nt_alert          (health_alert_id),
  KEY idx_nt_emergency      (emergency_event_id),
  CONSTRAINT fk_nt_user      FOREIGN KEY (user_id)            REFERENCES users (id)             ON DELETE CASCADE,
  CONSTRAINT fk_nt_alert     FOREIGN KEY (health_alert_id)    REFERENCES health_alerts (id)     ON DELETE CASCADE,
  CONSTRAINT fk_nt_emergency FOREIGN KEY (emergency_event_id) REFERENCES emergency_events (id)  ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
