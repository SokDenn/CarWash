package org.example.converter;

import org.flywaydb.core.internal.util.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Конвертер для преобразования строк в объекты времени и даты.
 */
@Component
public class StringConverter {

    /**
     * Преобразовать строку в объект LocalTime.
     *
     * @param timeStr строка с временем
     * @return объект LocalTime
     */
    public LocalTime convertTime(String timeStr) {
        try {
            return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Некорректный формат времени: " + timeStr, e);
        }
    }

    /**
     * Преобразовать строку в объект LocalDateTime.
     *
     * @param dateTimeStr строка с датой и временем
     * @return объект LocalDateTime
     */
    public LocalDateTime convertDateTime(String dateTimeStr) {
        try {
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Некорректный формат даты и времени: " + dateTimeStr, e);
        }
    }

    /**
     * Преобразовать строки времени открытия и закрытия в массив объектов LocalTime.
     *
     * @param openingTimeStr строка с временем открытия
     * @param closingTimeStr строка с временем закрытия
     * @return массив из объектов LocalTime для времени открытия и закрытия
     */
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

    /**
     * Преобразовать строки промежутка времени в массив объектов LocalDateTime.
     *
     * @param startDateTimeStr строка с датой и временем начала
     * @param endDateTimeStr строка с датой и временем окончания
     * @return массив из объектов LocalDateTime промежутков времени
     */
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
