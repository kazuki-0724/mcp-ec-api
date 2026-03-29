package com.example.externalapi.graphql;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.example.externalapi.domain.Employee;
import com.example.externalapi.domain.Item;
import com.example.externalapi.domain.RecipeSearchResult;
import com.example.externalapi.usecase.GetEmployeeByIdUseCase;
import com.example.externalapi.usecase.GetItemByIdUseCase;
import com.example.externalapi.usecase.SearchRecipeByKeywordUseCase;

@Controller
public class ExternalApiQueryController {

    private final GetEmployeeByIdUseCase getEmployeeByIdUseCase;
    private final SearchRecipeByKeywordUseCase searchRecipeByKeywordUseCase;
    private final GetItemByIdUseCase getItemByIdUseCase;

    public ExternalApiQueryController(
            GetEmployeeByIdUseCase getEmployeeByIdUseCase,
            SearchRecipeByKeywordUseCase searchRecipeByKeywordUseCase,
            GetItemByIdUseCase getItemByIdUseCase
    ) {
        this.getEmployeeByIdUseCase = getEmployeeByIdUseCase;
        this.searchRecipeByKeywordUseCase = searchRecipeByKeywordUseCase;
        this.getItemByIdUseCase = getItemByIdUseCase;
    }

    @QueryMapping
    public Employee employeeById(@Argument String employeeId) {
        return getEmployeeByIdUseCase.execute(employeeId);
    }

    @QueryMapping
    public RecipeSearchResult recipeByKeyword(@Argument String keyword) {
        return searchRecipeByKeywordUseCase.execute(keyword);
    }

    @QueryMapping
    public Item itemById(@Argument String itemId) {
        return getItemByIdUseCase.execute(itemId);
    }
}
