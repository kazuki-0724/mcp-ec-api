package com.example.externalapi.infrastructure;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.externalapi.domain.Item;
import com.example.externalapi.domain.ItemRepository;

@Repository
public class MockItemRepository implements ItemRepository {

    private final MockExternalApiDataSource dataSource;

    public MockItemRepository(MockExternalApiDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<Item> findById(String itemId) {
        return dataSource.findItemById(itemId);
    }
}
