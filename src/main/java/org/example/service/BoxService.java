package org.example.service;

import org.example.converter.StringConverter;
import org.example.model.Box;
import org.example.model.Reservation;
import org.example.model.Washing;
import org.example.repo.BoxRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
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

    public boolean createBox(int boxNumber, UUID userOperatorId, BigDecimal washingСoefficient,
                             String openingTimeStr, String closingTimeStr) {

        try {
            LocalTime openingTime = stringConverter.convertTime(openingTimeStr);
            LocalTime closingTime = stringConverter.convertTime(closingTimeStr);

            Box box = new Box(boxNumber, washingСoefficient, openingTime, closingTime);
            if (userOperatorId != null) {
                box.setUserOperator(userService.getUserById(userOperatorId));
            }
            saveBox(box);
            return true;

        } catch (Exception e) {
            System.out.println("Ошибка создания бокса: " + e.getMessage());
            return false;
        }
    }

    public boolean updateBox(UUID boxId, int boxNumber, UUID userOperatorId, BigDecimal washingСoefficient, String openingTimeStr, String closingTimeStr) {

        try {
            Box box = boxRepo.findById(boxId).orElseThrow();
            LocalTime openingTime = stringConverter.convertTime(openingTimeStr);
            LocalTime closingTime = stringConverter.convertTime(closingTimeStr);

            box.setBoxNumber(boxNumber);
            box.setWashingСoefficient(washingСoefficient);
            box.setOpeningTime(openingTime);
            box.setClosingTime(closingTime);
            if (userOperatorId != null) {
                box.setUserOperator(userService.getUserById(userOperatorId));
            } else{
                box.setUserOperator(null);
            }

            saveBox(box);
            return true;

        } catch (Exception e) {
            System.out.println("Ошибка обновления бокса: " + e.getMessage());
            return false;
        }

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
        Box box = boxRepo.findById(boxId).orElse(null);

        if (box != null) {
            box.setIsDeleted(true);
            boxRepo.save(box);
        }
    }

}
