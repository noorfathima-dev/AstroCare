package com.astrocare.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** double[] <-> JSON text, for the vital_signs.ecg_lead_ii JSON column. */
@Converter
public class DoubleArrayJsonConverter implements AttributeConverter<double[], String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(double[] attribute) {
        if (attribute == null) return null;
        try {
            return MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Cannot serialise ECG waveform", e);
        }
    }

    @Override
    public double[] convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return null;
        try {
            return MAPPER.readValue(dbData, double[].class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Cannot parse ECG waveform", e);
        }
    }
}
