package com.example.externalapi.graphql;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.ResponseError;
import org.springframework.graphql.test.tester.GraphQlTester;

import com.example.externalapi.infrastructure.MockExternalApiDataSource;

@SpringBootTest
@AutoConfigureGraphQlTester
class ExternalApiGraphQlIntegrationTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @Autowired
    private MockExternalApiDataSource dataSource;

    @BeforeEach
    void setUp() {
      dataSource.reset();
    }

    @Test
    void employeeByIdReturnsMockEmployee() {
        graphQlTester.document("""
                query EmployeeById {
                  employeeById(employeeId: \"E001\") {
                    employeeId
                    name
                    department
                    role
                  }
                }
                """)
                .execute()
                .path("employeeById.employeeId").entity(String.class).isEqualTo("E001")
                .path("employeeById.name").entity(String.class).isEqualTo("山田太郎")
                .path("employeeById.department").entity(String.class).isEqualTo("システム開発部")
                .path("employeeById.role").entity(String.class).isEqualTo("ストア運営責任者");
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
                .path("recipeByKeyword.recipe.recipeId").entity(String.class).isEqualTo("RECIPE001")
                .path("recipeByKeyword.recipe.requiredIngredients[0].itemId").entity(String.class).isEqualTo("G001");
    }

    @Test
    void itemByIdReturnsMockItem() {
        graphQlTester.document("""
                query ItemById {
                  itemById(itemId: \"G001\") {
                    itemId
                    itemName
                    categoryId
                    brandId
                    unitPrice
                    unit
                    stock
                    tags
                  }
                }
                """)
                .execute()
                .path("itemById.itemName").entity(String.class).isEqualTo("国産 玉ねぎ")
                .path("itemById.categoryId").entity(String.class).isEqualTo("CAT-FRESH")
                .path("itemById.unitPrice").entity(Integer.class).isEqualTo(120)
                .path("itemById.tags[0]").entity(String.class).isEqualTo("野菜");
    }

    @Test
    void productsReturnsCatalogMatches() {
        graphQlTester.document("""
                query Products {
                  products(query: \"カレー\", limit: 3) {
                    itemId
                    itemName
                    categoryName
                    brandName
                    averageRating
                    reviewCount
                  }
                }
                """)
                .execute()
                .path("products[0].itemId").entity(String.class).isEqualTo("G001")
                .path("products[1].itemId").entity(String.class).isEqualTo("G002")
                .path("products[2].itemId").entity(String.class).isEqualTo("G003");
    }

    @Test
    void currentCartMutationFlowReturnsUpdatedSnapshot() {
        graphQlTester.document("""
                mutation AddItem {
                  addItemToCart(itemId: \"G005\", quantity: 2) {
                    cartId
                    items {
                      itemId
                      quantity
                      subtotal
                    }
                    summary {
                      originalSubtotal
                      lineDiscountTotal
                      subtotal
                      shippingFee
                      grandTotal
                    }
                  }
                }
                """)
                .execute()
                .path("addItemToCart.cartId").entity(String.class).isEqualTo("CART-001")
                .path("addItemToCart.items[0].itemId").entity(String.class).isEqualTo("G005")
                .path("addItemToCart.items[0].quantity").entity(Integer.class).isEqualTo(2)
                .path("addItemToCart.summary.originalSubtotal").entity(Integer.class).isEqualTo(520);

        graphQlTester.document("""
                query CurrentCart {
                  currentCart {
                    customerId
                    items {
                      itemId
                      quantity
                    }
                    summary {
                      shippingFee
                    }
                  }
                }
                """)
                .execute()
                .path("currentCart.customerId").entity(String.class).isEqualTo("C001")
                .path("currentCart.items[0].quantity").entity(Integer.class).isEqualTo(2)
                .path("currentCart.summary.shippingFee").entity(Integer.class).isEqualTo(550);
    }

    @Test
    void wishlistAndOrderHistoryReturnMockCommerceData() {
        graphQlTester.document("""
                query WishlistAndOrders {
                  wishlist(customerId: \"C001\") {
                    items {
                      itemId
                    }
                  }
                  orderHistory(customerId: \"C001\") {
                    orderId
                    itemCount
                  }
                }
                """)
                .execute()
                .path("wishlist.items[0].itemId").entity(String.class).isEqualTo("G009")
                .path("wishlist.items[1].itemId").entity(String.class).isEqualTo("G010")
                .path("orderHistory[0].orderId").entity(String.class).isEqualTo("O1001")
                .path("orderHistory[0].itemCount").entity(Integer.class).isEqualTo(4);
    }

    @Test
    void shippingQuoteAndAddressValidationFollowBusinessRules() {
        graphQlTester.document("""
                query ShippingAndAddress {
                  shippingFeeQuote(postalCode: \"9000001\", prefecture: \"沖縄県\", shippingMethod: \"standard\", cartTotal: 1000) {
                    shippingFee
                    freeShippingThreshold
                    remoteSurcharge
                  }
                  validateShippingAddress(input: {
                    postalCode: \"1500001\"
                    prefecture: \"東京都\"
                    city: \"渋谷区\"
                    line1: \"12\"
                    line2: \"Apt 502\"
                  }) {
                    isValid
                    warnings
                    normalizedAddress {
                      postalCode
                    }
                  }
                }
                """)
                .execute()
                .path("shippingFeeQuote.shippingFee").entity(Integer.class).isEqualTo(970)
                .path("shippingFeeQuote.remoteSurcharge").entity(Integer.class).isEqualTo(420)
                .path("validateShippingAddress.isValid").entity(Boolean.class).isEqualTo(true)
                .path("validateShippingAddress.warnings[0]").entity(String.class).isEqualTo("line1 should be at least 3 characters long");
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
