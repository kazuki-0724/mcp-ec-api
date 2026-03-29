package com.example.externalapi.domain;

public record RequiredIngredient(
        String ingredientName,
        String requiredQty,
        String itemId
) {
}
