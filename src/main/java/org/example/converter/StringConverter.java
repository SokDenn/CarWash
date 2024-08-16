package org.example.converter;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public class StringConverter {
    public LocalTime convertTime(String timeStr) {
        try {
            LocalTime localTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
            return localTime;

        } catch (Exception e) {
            System.out.println("Ошибка парсинга времени: " + e.getMessage());
        }
        return null;
    }

    public LocalDateTime convertDataTime(String dataTimeStr) {
        try {
            LocalDateTime localDateTime = LocalDateTime.parse(dataTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
            return localDateTime;

        } catch (Exception e) {
            System.out.println("Ошибка парсинга даты и времени: " + e.getMessage());
        }
        return null;
    }
}
