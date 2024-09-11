package org.example.converter;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public class StringConverter {
    public LocalTime convertTime(String timeStr) {
        LocalTime localTime = null;
        try {
            localTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));

        } catch (Exception e) {
            System.out.println("Ошибка парсинга времени: " + e.getMessage());
            return null;
        }
        return localTime;
    }

    public LocalDateTime convertDataTime(String dataTimeStr) {
        LocalDateTime localDateTime = null;
        try {
            localDateTime = LocalDateTime.parse(dataTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

        } catch (Exception e) {
            System.out.println("Ошибка парсинга даты и времени: " + e.getMessage());
            return null;
        }
        return localDateTime;
    }
}
