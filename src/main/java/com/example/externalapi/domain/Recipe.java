package com.example.externalapi.domain;

import java.util.List;

public record Recipe(
        String recipeId,
        String recipeName,
        int servings,
        List<RequiredIngredient> requiredIngredients
) {
}
