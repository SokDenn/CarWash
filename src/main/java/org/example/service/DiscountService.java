package org.example.service;

import org.example.model.Discount;
import org.example.repo.DiscountRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class DiscountService {
    @Autowired
    private DiscountRepo discountRepo;
    @Autowired
    private ReservationService reservationService;

    public void saveDiscount(Integer minDiscount, Integer maxDiscount) {
        Discount discount = findDiscount();
        discount.setMin(minDiscount);
        discount.setMax(maxDiscount);

        discountRepo.save(discount);
    }

    public Discount findDiscount() {
        return discountRepo.findFirstBy();
    }

    public List<Integer> getPermittedDiscountList() {

        Discount discount = findDiscount();

        List<Integer> allPermittedDiscountList = IntStream
                .rangeClosed(discount.getMin(), discount.getMax())
                .boxed()
                .collect(Collectors.toList());

        return allPermittedDiscountList;
    }

}
