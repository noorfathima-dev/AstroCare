package com.astrocare.dto;
import com.astrocare.entity.*; import jakarta.validation.constraints.*;
public record SosRequest(@NotNull EmergencyType eventType,@NotNull Severity severity,@NotBlank @Size(max=1000) String description) {}
