package com.astrocare.dto;
import java.util.Set;
public record LoginResponse(String token, String tokenType, long expiresInSeconds, String username, Set<String> roles) {}
