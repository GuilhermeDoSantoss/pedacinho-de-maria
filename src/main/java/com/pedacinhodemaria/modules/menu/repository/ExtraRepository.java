package com.pedacinhodemaria.modules.menu.repository;

import com.pedacinhodemaria.modules.menu.domain.Extra;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ExtraRepository extends MongoRepository<Extra, String> {
    List<Extra> findByActiveTrueOrderByDisplayOrderAsc();
    Optional<Extra> findByIdAndActiveTrue(String id);
}