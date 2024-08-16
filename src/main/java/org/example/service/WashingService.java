package org.example.service;

import org.example.model.Box;
import org.example.model.Washing;
import org.example.repo.WashingRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
@Service
public class WashingService {
    @Autowired
    private WashingRepo washingRepo;

    public List<Washing> getAllWashing() {
        return washingRepo.findAllActive();
    }

    public Washing getWashingById(UUID id) {
        return washingRepo.findById(id).orElse(null);
    }

    public boolean createWashing(String name, Integer price, Integer durationMinute) {

        washingRepo.save(new Washing(name, price, durationMinute));
        return true;
    }

    public boolean updateWashing(UUID id,String name, Integer price, Integer durationMinute) {

        Washing washing = washingRepo.findById(id).orElse(null);
        if(washing != null){
            washing.setName(name);
            washing.setPrice(price);
            washing.setDurationMinute(durationMinute);
            washingRepo.save(washing);
            return true;
        }

        return false;
    }

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
