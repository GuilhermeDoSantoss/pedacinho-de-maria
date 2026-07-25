package com.pedacinhodemaria.modules.menu.repository;

import com.pedacinhodemaria.modules.menu.domain.SideDish;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SideDishRepository extends MongoRepository<SideDish, String> {
    List<SideDish> findByActiveTrueOrderByDisplayOrderAsc();
    Optional<SideDish> findByIdAndActiveTrue(String id);
}