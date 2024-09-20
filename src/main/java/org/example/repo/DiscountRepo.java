package org.example.repo;

import org.example.model.Discount;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

/**
 * Репозиторий для работы с сущностью Discount.
 */
public interface DiscountRepo extends CrudRepository<Discount, UUID> {
    /**
     * Найти первую запись скидки в базе данных.
     *
     * @return объект Discount, если запись существует, иначе null
     */
    Discount findFirstBy();
}
