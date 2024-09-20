package org.example.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.converter.StringConverter;
import org.example.model.Box;
import org.example.repo.BoxRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.SortedSet;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Сервис для управления боками, включая создание, обновление, получение и удаление боков.
 */
@Service
public class BoxService {
    @Autowired
    private BoxRepo boxRepo;
    @Autowired
    private UserService userService;
    @Autowired
    private StringConverter stringConverter;

    /**
     * Получить бокс по его ID.
     *
     * @param id идентификатор бокса
     * @return объект Box или null, если не найден
     */
    public Box getBoxById(UUID id) {
        return boxRepo.findById(id).orElse(null);
    }

    /**
     * Создать новый бокс с указанными параметрами.
     *
     * @param boxNumber номер бокса
     * @param userOperatorId идентификатор оператора
     * @param washingСoefficient коэффициент мойки
     * @param openingTimeStr время открытия в строковом формате
     * @param closingTimeStr время закрытия в строковом формате
     * @return идентификатор созданного бокса
     */
    public UUID createBox(int boxNumber, UUID userOperatorId, BigDecimal washingСoefficient,
                          String openingTimeStr, String closingTimeStr) {

        try {
            LocalTime[] timeRange = stringConverter.parseTimeRange(openingTimeStr, closingTimeStr);

            Box box = new Box(boxNumber, washingСoefficient, timeRange[0], timeRange[1]);
            if (userOperatorId != null) {
                box.setUserOperator(userService.getUserById(userOperatorId));
            }
            saveBox(box);
            return box.getId();

        } catch (Exception e) {
            throw new RuntimeException("Ошибка создания бокса: " + e.getMessage(), e);
        }
    }

    /**
     * Обновить существующий бокс с указанными параметрами.
     *
     * @param boxId идентификатор бокса
     * @param boxNumber новый номер бокса
     * @param userOperatorId идентификатор нового оператора
     * @param washingСoefficient новый коэффициент мойки
     * @param openingTimeStr новое время открытия в строковом формате
     * @param closingTimeStr новое время закрытия в строковом формате
     * @return идентификатор обновленного бокса
     */
    public UUID updateBox(UUID boxId, int boxNumber, UUID userOperatorId, BigDecimal washingСoefficient, String openingTimeStr, String closingTimeStr) {

        try {
            Box box = boxRepo.findById(boxId).orElseThrow();
            LocalTime[] timeRange = stringConverter.parseTimeRange(openingTimeStr, closingTimeStr);

            box.setBoxNumber(boxNumber);
            box.setWashingСoefficient(washingСoefficient);
            box.setOpeningTime(timeRange[0]);
            box.setClosingTime(timeRange[1]);
            if (userOperatorId != null) {
                box.setUserOperator(userService.getUserById(userOperatorId));
            } else {
                box.setUserOperator(null);
            }
            saveBox(box);

        } catch (Exception e) {
            throw new RuntimeException("Ошибка обновления бокса: " + e.getMessage(), e);
        }
        return boxId;
    }

    /**
     * Получить список доступных номеров боксов.
     *
     * @param boxId идентификатор бокса (если имеется, добавляет его номер в список)
     * @return список доступных номеров боксов
     */
    public List<Integer> getAvailableBoxNumbers(UUID boxId) {
        SortedSet<Integer> existingBoxNumbers = boxRepo.findAllBoxNumber();

        List<Integer> allBoxNumbers = IntStream.rangeClosed(1, 20)
                .boxed()
                .collect(Collectors.toList());

        allBoxNumbers.removeAll(existingBoxNumbers);
        if (boxId != null) {
            allBoxNumbers.add(getBoxById(boxId).getBoxNumber());

            return allBoxNumbers.stream()
                    .sorted()
                    .collect(Collectors.toList());
        }
        return allBoxNumbers;
    }

    /**
     * Получить список всех активных боксов.
     *
     * @return список объектов Box, представляющих активные боксы
     */
    public List<Box> findAllActive() {
        return boxRepo.findAllActive();
    }

    /**
     * Сохранить указанный бокс в репозитории.
     *
     * @param box объект Box для сохранения
     * @return сохраненный объект Box
     */
    public Box saveBox(Box box) {
        return boxRepo.save(box);
    }

    /**
     * Удалить бокс по его ID, помечая его как удаленный.
     *
     * @param boxId идентификатор бокса для удаления
     * @throws EntityNotFoundException если бокс с указанным ID не найден
     */
    public void deleteBox(UUID boxId) {
        Box box = boxRepo.findById(boxId)
                .orElseThrow(() -> new EntityNotFoundException("Box c ID: " + boxId + " не найден"));

        if (box != null) {
            box.setIsDeleted(true);
            boxRepo.save(box);
        }
    }

}
