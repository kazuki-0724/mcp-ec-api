package com.example.externalapi.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.example.externalapi.app.AppException;
import com.example.externalapi.domain.Recipe;
import com.example.externalapi.domain.RecipeRepository;
import com.example.externalapi.domain.RecipeSearchResult;
import com.example.externalapi.domain.RequiredIngredient;

class SearchRecipeByKeywordUseCaseTest {

    private final InputValidator inputValidator = new InputValidator();

    @Test
    void returnsRecipeSearchResult() {
        RecipeRepository repository = keyword -> Optional.of(new Recipe(
                "R001",
                "カレーライス",
                4,
                List.of(new RequiredIngredient("たまねぎ", "2個", "G001"))
        ));
        SearchRecipeByKeywordUseCase useCase = new SearchRecipeByKeywordUseCase(repository, inputValidator);

        RecipeSearchResult result = useCase.execute(" カレー ");

        assertThat(result.keyword()).isEqualTo("カレー");
        assertThat(result.recipe().recipeName()).isEqualTo("カレーライス");
        assertThat(result.recipe().requiredIngredients()).hasSize(1);
    }

    @Test
    void rejectsBlankKeyword() {
        RecipeRepository repository = keyword -> Optional.empty();
        SearchRecipeByKeywordUseCase useCase = new SearchRecipeByKeywordUseCase(repository, inputValidator);

        assertThatThrownBy(() -> useCase.execute("   "))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("keyword must not be blank");
    }
}
