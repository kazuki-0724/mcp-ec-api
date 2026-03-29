package com.example.externalapi.domain;

public record RecipeSearchResult(
        String keyword,
        Recipe recipe,
        String nextActionHint
) {
}
