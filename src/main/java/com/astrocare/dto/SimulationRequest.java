package com.astrocare.dto;
import jakarta.validation.constraints.*;
public record SimulationRequest(@NotNull Long astronautId,@NotNull Scenario scenario) { public enum Scenario { NORMAL,WARNING,CRITICAL,EVA_STRAIN } }
