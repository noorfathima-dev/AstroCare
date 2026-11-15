package com.astrocare.dto;
import com.astrocare.entity.*; import java.time.*;
public record MarkerResponse(Long id,Long astronautId,BodyZone bodyZone,Double coordX,Double coordY,Severity severity,String clinicalNote,boolean resolved,LocalDateTime createdAt) { public static MarkerResponse from(AnatomicalMarker m){return new MarkerResponse(m.getId(),m.getAstronaut().getId(),m.getBodyZone(),m.getCoordX(),m.getCoordY(),m.getSeverity(),m.getClinicalNote(),m.isResolved(),m.getCreatedAt());} }
