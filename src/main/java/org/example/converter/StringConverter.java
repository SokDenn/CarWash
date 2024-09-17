package org.example.converter;

import org.flywaydb.core.internal.util.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public class StringConverter {

    public LocalTime convertTime(String timeStr) {
        try {
            return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Некорректный формат времени: " + timeStr, e);
        }
    }

    public LocalDateTime convertDateTime(String dateTimeStr) {
        try {
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Некорректный формат даты и времени: " + dateTimeStr, e);
        }
    }

    public LocalTime[] parseTimeRange(String openingTimeStr, String closingTimeStr) {
        LocalTime openingTime = null;
        LocalTime closingTime = null;

        if (StringUtils.hasText(openingTimeStr)) {
            openingTime = convertTime(openingTimeStr);
        }
        if (StringUtils.hasText(closingTimeStr)) {
            closingTime = convertTime(closingTimeStr);
        }

        return new LocalTime[]{openingTime, closingTime};
    }

    public LocalDateTime[] parseDateTimeRange(String startDateTimeStr, String endDateTimeStr) {
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;

        if (StringUtils.hasText(startDateTimeStr)) {
            startDateTime = convertDateTime(startDateTimeStr);
        }
        if (StringUtils.hasText(endDateTimeStr)) {
            endDateTime = convertDateTime(endDateTimeStr);
        }

        return new LocalDateTime[]{startDateTime, endDateTime};
    }
}
