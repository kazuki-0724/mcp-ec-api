package com.example.externalapi.usecase;

import org.springframework.stereotype.Service;

import com.example.externalapi.app.AppException;
import com.example.externalapi.domain.Recipe;
import com.example.externalapi.domain.RecipeRepository;
import com.example.externalapi.domain.RecipeSearchResult;

@Service
public class SearchRecipeByKeywordUseCase {

    private static final String NEXT_ACTION_HINT = "requiredIngredients[].itemId を使って itemById を呼び出してください。";

    private final RecipeRepository recipeRepository;
    private final InputValidator inputValidator;

    public SearchRecipeByKeywordUseCase(RecipeRepository recipeRepository, InputValidator inputValidator) {
        this.recipeRepository = recipeRepository;
        this.inputValidator = inputValidator;
    }

    public RecipeSearchResult execute(String keyword) {
        String validatedKeyword = inputValidator.validateKeyword(keyword);
        Recipe recipe = recipeRepository.findByKeyword(validatedKeyword)
                .orElseThrow(() -> AppException.notFound("recipe not found for keyword: " + validatedKeyword));
        return new RecipeSearchResult(validatedKeyword, recipe, NEXT_ACTION_HINT);
    }
}
