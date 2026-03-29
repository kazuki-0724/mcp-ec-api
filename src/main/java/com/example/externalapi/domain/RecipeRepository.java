package com.example.externalapi.domain;

import java.util.Optional;

public interface RecipeRepository {
    Optional<Recipe> findByKeyword(String keyword);
}
