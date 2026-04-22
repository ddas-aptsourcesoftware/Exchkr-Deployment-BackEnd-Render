package com.exchkr.club.management.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter 
public class StringStatusToSmallIntConverter implements AttributeConverter<String, Short> {

    @Override
    public Short convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return 0; 
        }
        
        return "Active".equalsIgnoreCase(attribute) ? (short) 1 : (short) 0;
    }

    @Override
    public String convertToEntityAttribute(Short dbData) {
        if (dbData == null) {
            return "InActive"; 
        }
        
        return dbData.intValue() == 1 ? "Active" : "InActive";
    }
}