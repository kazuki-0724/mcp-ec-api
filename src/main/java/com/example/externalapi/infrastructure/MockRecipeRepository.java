package com.example.externalapi.infrastructure;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.externalapi.domain.Recipe;
import com.example.externalapi.domain.RecipeRepository;

@Repository
public class MockRecipeRepository implements RecipeRepository {

    private final MockExternalApiDataSource dataSource;

    public MockRecipeRepository(MockExternalApiDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<Recipe> findByKeyword(String keyword) {
        return dataSource.findRecipeByKeyword(keyword);
    }
}
