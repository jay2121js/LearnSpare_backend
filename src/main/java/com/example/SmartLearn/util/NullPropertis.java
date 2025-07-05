package com.example.SmartLearn.util;

import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;

@Component
public class NullPropertis {

    public String[] getNullProperty(Object source) {
        String[] nullFields = Arrays.stream(source.getClass().getDeclaredFields())
            .filter(field -> {
                try {
                    field.setAccessible(true); // Make the field accessible for reflection
                    return field.get(source) == null; // Check if the field value is null
                } catch (Exception e) {
                    return false; // Handle exceptions gracefully
                }
            })
            .map(Field::getName) // Map to field names
            .toArray(String[]::new); // Convert to an array of strings

        // Print fields with null values for debugging
        System.out.println("Fields with null values: " + Arrays.toString(nullFields));
        return nullFields; // Return the array of null field names

    }
}
