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

@Service
public class BoxService {
    @Autowired
    private BoxRepo boxRepo;
    @Autowired
    private UserService userService;
    @Autowired
    private StringConverter stringConverter;

    public Box getBoxById(UUID id) {
        return boxRepo.findById(id).orElse(null);
    }

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

    public List<Box> findAllActive() {
        return boxRepo.findAllActive();
    }

    public Box saveBox(Box box) {
        return boxRepo.save(box);
    }

    public void deleteBox(UUID boxId) {
        Box box = boxRepo.findById(boxId)
                .orElseThrow(() -> new EntityNotFoundException("Box c ID: " + boxId + " не найден"));

        if (box != null) {
            box.setIsDeleted(true);
            boxRepo.save(box);
        }
    }

}
