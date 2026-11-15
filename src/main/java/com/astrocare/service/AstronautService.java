package com.astrocare.service;
import com.astrocare.dto.*; import com.astrocare.repository.*; import lombok.RequiredArgsConstructor; import org.springframework.stereotype.Service; import java.util.*;
@Service @RequiredArgsConstructor public class AstronautService { private final AstronautRepository repo; public List<AstronautResponse> list(){return repo.findAll().stream().map(AstronautResponse::from).toList();} }
