package org.example.repo;

import org.example.model.Box;
import org.example.model.Discount;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DiscountRepo extends CrudRepository<Discount, UUID> {
    Discount findFirstBy();
}
