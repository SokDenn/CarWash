package org.example.service;

import org.example.model.Discount;
import org.example.repo.DiscountRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Сервис для управления скидками, включая их сохранение и получение списка разрешенных скидок.
 */
@Service
public class DiscountService {
    @Autowired
    private DiscountRepo discountRepo;
    @Autowired
    private ReservationService reservationService;

    /**
     * Сохранить новые минимальные и максимальные значения скидки.
     *
     * @param minDiscount новое минимальное значение скидки
     * @param maxDiscount новое максимальное значение скидки
     */
    public void saveDiscount(Integer minDiscount, Integer maxDiscount) {
        Discount discount = findDiscount();
        discount.setMin(minDiscount);
        discount.setMax(maxDiscount);

        discountRepo.save(discount);
    }

    /**
     * Найти диапазон скидки в БД.
     *
     * @return объект Discount, представляющий текущую скидку
     */
    public Discount findDiscount() {
        return discountRepo.findFirstBy();
    }

    /**
     * Получить список разрешенных значений скидок в диапазоне от минимальной до максимальной.
     *
     * @return список разрешенных значений скидок
     */
    public List<Integer> getPermittedDiscountList() {

        Discount discount = findDiscount();

        List<Integer> allPermittedDiscountList = IntStream
                .rangeClosed(discount.getMin(), discount.getMax())
                .boxed()
                .collect(Collectors.toList());

        return allPermittedDiscountList;
    }

}
