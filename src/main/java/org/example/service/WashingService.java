package org.example.service;

import org.example.model.Washing;
import org.example.repo.WashingRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Сервис для управления типами мойки.
 */
@Service
public class WashingService {
    @Autowired
    private WashingRepo washingRepo;

    /**
     * Получить список всех активных типов мойки.
     *
     * @return список активных типов мойки
     */
    public List<Washing> getAllWashing() {
        return washingRepo.findAllActive();
    }

    /**
     * Получить тип мойки по его идентификатору.
     *
     * @param id идентификатор типа мойки
     * @return тип мойки или null, если не найден
     */
    public Washing getWashingById(UUID id) {
        return washingRepo.findById(id).orElse(null);
    }

    /**
     * Создать новый тип мойки.
     *
     * @param name название типа мойки
     * @param price цена за мойку
     * @param durationMinute продолжительность мойки в минутах
     * @return true, если тип мойки успешно создан
     */
    public boolean createWashing(String name, Integer price, Integer durationMinute) {

        washingRepo.save(new Washing(name, price, durationMinute));
        return true;
    }

    /**
     * Обновить существующий тип мойки.
     *
     * @param id идентификатор типа мойки
     * @param name новое название типа мойки
     * @param price новая цена за мойку
     * @param durationMinute новая продолжительность мойки в минутах
     * @return true, если тип мойки успешно обновлен
     */
    public boolean updateWashing(UUID id, String name, Integer price, Integer durationMinute) {

        Washing washing = washingRepo.findById(id).orElse(null);
        if (washing != null) {
            washing.setName(name);
            washing.setPrice(price);
            washing.setDurationMinute(durationMinute);
            washingRepo.save(washing);
            return true;
        }

        return false;
    }

    /**
     * Удалить тип мойки, пометив его как неактивный.
     *
     * @param id идентификатор типа мойки
     * @return true, если тип мойки успешно удален
     */
    public boolean deleteWashing(UUID id) {
        Washing washing = washingRepo.findById(id).orElse(null);

        if (washing != null) {
            washing.setIsDeleted(true);
            washingRepo.save(washing);
            return true;
        }
        return false;
    }
}
