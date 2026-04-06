package com.example.externalapi.graphql;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

import com.example.externalapi.domain.CommerceModels.Cart;
import com.example.externalapi.domain.CommerceModels.Wishlist;
import com.example.externalapi.usecase.CommerceUseCase;

@Controller
public class ExternalApiMutationController {

    private final CommerceUseCase commerceUseCase;

    public ExternalApiMutationController(CommerceUseCase commerceUseCase) {
        this.commerceUseCase = commerceUseCase;
    }

    @MutationMapping
    public Cart addItemToCart(@Argument String itemId, @Argument Integer quantity) {
        return commerceUseCase.addItemToCart(itemId, quantity);
    }

    @MutationMapping
    public Cart updateCartItemQuantity(@Argument String itemId, @Argument int quantity) {
        return commerceUseCase.updateCartItemQuantity(itemId, quantity);
    }

    @MutationMapping
    public Cart removeItemFromCart(@Argument String itemId) {
        return commerceUseCase.removeItemFromCart(itemId);
    }

    @MutationMapping
    public Cart applyCouponToCart(@Argument String couponCode) {
        return commerceUseCase.applyCouponToCart(couponCode);
    }

    @MutationMapping
    public Wishlist addItemToWishlist(@Argument String customerId, @Argument String itemId) {
        return commerceUseCase.addItemToWishlist(customerId, itemId);
    }
}