package com.example.externalapi.domain;

import java.util.Optional;

public interface ItemRepository {
    Optional<Item> findById(String itemId);
}
