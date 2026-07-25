package com.pedacinhodemaria.modules.menu.repository;

import com.pedacinhodemaria.modules.menu.domain.Drink;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface DrinkRepository extends MongoRepository<Drink, String> {

    List<Drink> findByActiveTrueOrderByDisplayOrderAsc();

    Optional<Drink> findByIdAndActiveTrue(String id);
}