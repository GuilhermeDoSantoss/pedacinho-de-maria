package com.pedacinhodemaria.modules.menu.repository;

import com.pedacinhodemaria.modules.menu.domain.Meal;
import com.pedacinhodemaria.modules.menu.domain.MealType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MealRepository extends MongoRepository<Meal, String> {

    List<Meal> findByActiveTrueOrderByDisplayOrderAsc();

    List<Meal> findByActiveTrueAndTypeOrderByDisplayOrderAsc(MealType type);

    Optional<Meal> findByIdAndActiveTrue(String id);
}