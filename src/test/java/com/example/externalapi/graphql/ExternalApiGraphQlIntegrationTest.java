package com.example.externalapi.graphql;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.ResponseError;
import org.springframework.graphql.test.tester.GraphQlTester;

@SpringBootTest
@AutoConfigureGraphQlTester
class ExternalApiGraphQlIntegrationTest {

    @Autowired
  private GraphQlTester graphQlTester;

    @Test
    void employeeByIdReturnsMockEmployee() {
        graphQlTester.document("""
                query EmployeeById {
                  employeeById(employeeId: \"E001\") {
                    employeeId
                    name
                    department
                  }
                }
                """)
                .execute()
                .path("employeeById.employeeId").entity(String.class).isEqualTo("E001")
                .path("employeeById.name").entity(String.class).isEqualTo("山田 太郎")
                .path("employeeById.department").entity(String.class).isEqualTo("営業部");
    }

    @Test
    void recipeByKeywordReturnsRequiredIngredientsWithItemId() {
        graphQlTester.document("""
                query RecipeByKeyword {
                  recipeByKeyword(keyword: \"カレー\") {
                    keyword
                    nextActionHint
                    recipe {
                      recipeId
                      recipeName
                      servings
                      requiredIngredients {
                        ingredientName
                        requiredQty
                        itemId
                      }
                    }
                  }
                }
                """)
                .execute()
                .path("recipeByKeyword.keyword").entity(String.class).isEqualTo("カレー")
                .path("recipeByKeyword.recipe.recipeId").entity(String.class).isEqualTo("R001")
                .path("recipeByKeyword.recipe.requiredIngredients[0].itemId").entity(String.class).isEqualTo("G001");
    }

    @Test
    void itemByIdReturnsMockItem() {
        graphQlTester.document("""
                query ItemById {
                  itemById(itemId: \"G001\") {
                    itemId
                    itemName
                    unitPrice
                    unit
                    stock
                  }
                }
                """)
                .execute()
                .path("itemById.itemName").entity(String.class).isEqualTo("たまねぎ")
                .path("itemById.unitPrice").entity(Integer.class).isEqualTo(120);
    }

    @Test
    void invalidEmployeeIdReturnsBadUserInputError() {
        graphQlTester.document("""
                query InvalidEmployeeId {
                  employeeById(employeeId: \"X001\") {
                    employeeId
                  }
                }
                """)
                .execute()
                .errors()
                .satisfy(errors -> assertErrorCode(errors, "BAD_USER_INPUT"));
    }

    @Test
    void missingItemReturnsNotFoundError() {
        graphQlTester.document("""
                query MissingItem {
                  itemById(itemId: \"G998\") {
                    itemId
                  }
                }
                """)
                .execute()
                .errors()
                .satisfy(errors -> assertErrorCode(errors, "NOT_FOUND"));
    }

    @Test
    void timeoutKeywordReturnsUpstreamTimeoutError() {
        graphQlTester.document("""
                query TimeoutRecipe {
                  recipeByKeyword(keyword: \"timeout\") {
                    keyword
                  }
                }
                """)
                .execute()
                .errors()
                .satisfy(errors -> assertErrorCode(errors, "UPSTREAM_TIMEOUT"));
    }

    private void assertErrorCode(java.util.List<ResponseError> errors, String expectedCode) {
        assertThat(errors)
          .anySatisfy(error -> assertThat(error.getExtensions())
              .containsEntry("code", expectedCode));
    }
}
