package com.astrocare.dto;
import com.astrocare.entity.*; import java.time.*;
public record EmergencyResponse(Long id,Long astronautId,EmergencyType eventType,Severity severity,EmergencyStatus status,String description,LocalDateTime triggeredAt) { public static EmergencyResponse from(EmergencyEvent e){return new EmergencyResponse(e.getId(),e.getAstronaut()==null?null:e.getAstronaut().getId(),e.getEventType(),e.getSeverity(),e.getStatus(),e.getDescription(),e.getTriggeredAt());} }
