package com.astrocare.dto;
import com.astrocare.entity.*;
public record AstronautResponse(Long id,String callSign,String fullName,String crewRole,String nationality,AstronautStatus status,boolean evaQualified,Long missionId) {
 public static AstronautResponse from(Astronaut a){return new AstronautResponse(a.getId(),a.getCallSign(),a.getFullName(),a.getCrewRole(),a.getNationality(),a.getStatus(),a.isEvaQualified(),a.getMission()==null?null:a.getMission().getId());}
}
