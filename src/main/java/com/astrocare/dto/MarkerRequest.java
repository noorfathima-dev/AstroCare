package com.astrocare.dto;
import com.astrocare.entity.*; import jakarta.validation.constraints.*;
public record MarkerRequest(@NotNull Long astronautId,@NotNull BodyZone bodyZone,@NotNull @DecimalMin("0.0") @DecimalMax("1.0") Double coordX,@NotNull @DecimalMin("0.0") @DecimalMax("1.0") Double coordY,@NotNull Severity severity,@Size(max=1000) String clinicalNote) {}
