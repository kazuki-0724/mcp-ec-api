package com.example.externalapi.infrastructure;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.externalapi.app.AppException;
import com.example.externalapi.domain.Employee;
import com.example.externalapi.domain.Item;
import com.example.externalapi.domain.Recipe;
import com.example.externalapi.domain.RequiredIngredient;

@Component
public class MockExternalApiDataSource {

    private final String timeoutEmployeeId;
    private final String timeoutItemId;
    private final String timeoutKeyword;

    private final Map<String, Employee> employees = Map.of(
            "E001", new Employee("E001", "山田 太郎", "営業部"),
            "E002", new Employee("E002", "佐藤 花子", "開発部"),
            "E003", new Employee("E003", "鈴木 次郎", "人事部")
    );

    private final Map<String, Item> items = Map.of(
            "G001", new Item("G001", "たまねぎ", 120, "個", 58),
            "G002", new Item("G002", "にんじん", 98, "本", 34),
            "G003", new Item("G003", "じゃがいも", 65, "個", 76),
            "G004", new Item("G004", "カレールー", 280, "箱", 24),
            "G005", new Item("G005", "鶏もも肉", 420, "パック", 12),
            "G006", new Item("G006", "米", 1980, "袋", 19)
    );

    private final List<Recipe> recipes = List.of(
            new Recipe(
                    "R001",
                    "カレーライス",
                    4,
                    List.of(
                            new RequiredIngredient("たまねぎ", "2個", "G001"),
                            new RequiredIngredient("にんじん", "1本", "G002"),
                            new RequiredIngredient("じゃがいも", "3個", "G003"),
                            new RequiredIngredient("カレールー", "1箱", "G004")
                    )
            ),
            new Recipe(
                    "R002",
                    "親子丼",
                    2,
                    List.of(
                            new RequiredIngredient("鶏もも肉", "1パック", "G005"),
                            new RequiredIngredient("たまねぎ", "1個", "G001"),
                            new RequiredIngredient("米", "2合", "G006")
                    )
            )
    );

    public MockExternalApiDataSource(
            @Value("${app.mock.timeout-employee-id:E999}") String timeoutEmployeeId,
            @Value("${app.mock.timeout-item-id:G999}") String timeoutItemId,
            @Value("${app.mock.timeout-keyword:timeout}") String timeoutKeyword
    ) {
        this.timeoutEmployeeId = timeoutEmployeeId;
        this.timeoutItemId = timeoutItemId;
        this.timeoutKeyword = timeoutKeyword;
    }

    public Optional<Employee> findEmployeeById(String employeeId) {
        if (timeoutEmployeeId.equals(employeeId)) {
            throw AppException.upstreamTimeout("mock upstream timeout while fetching employee");
        }
        return Optional.ofNullable(employees.get(employeeId));
    }

    public Optional<Item> findItemById(String itemId) {
        if (timeoutItemId.equals(itemId)) {
            throw AppException.upstreamTimeout("mock upstream timeout while fetching item");
        }
        return Optional.ofNullable(items.get(itemId));
    }

    public Optional<Recipe> findRecipeByKeyword(String keyword) {
        if (timeoutKeyword.equalsIgnoreCase(keyword)) {
            throw AppException.upstreamTimeout("mock upstream timeout while fetching recipe");
        }

        String normalizedKeyword = normalize(keyword);
        return recipes.stream()
                .filter(recipe -> matchesKeyword(recipe, normalizedKeyword))
                .findFirst();
    }

    private boolean matchesKeyword(Recipe recipe, String normalizedKeyword) {
        if (normalize(recipe.recipeName()).contains(normalizedKeyword)) {
            return true;
        }
        return recipe.requiredIngredients().stream()
                .map(RequiredIngredient::ingredientName)
                .map(this::normalize)
                .anyMatch(ingredient -> ingredient.contains(normalizedKeyword));
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
