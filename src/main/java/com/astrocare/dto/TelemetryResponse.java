package com.astrocare.dto;
import com.astrocare.entity.*; import java.time.*;
public record TelemetryResponse(Long id,Long astronautId,LocalDateTime recordedAt,Integer heartRateBpm,Double spo2Pct,Integer systolicMmHg,Integer diastolicMmHg,Double coreTempC,Integer respRateBpm,Double suitPressurePsi,Double radiationMsvH,VitalSource source,double[] ecgLeadIi) {
 public static TelemetryResponse from(VitalSign v, boolean ecg){return new TelemetryResponse(v.getId(),v.getAstronaut().getId(),v.getRecordedAt(),v.getHeartRateBpm(),v.getSpo2Pct(),v.getSystolicMmHg(),v.getDiastolicMmHg(),v.getCoreTempC(),v.getRespRateBpm(),v.getSuitPressurePsi(),v.getRadiationMsvH(),v.getSource(),ecg?v.getEcgLeadIi():null);}
}
