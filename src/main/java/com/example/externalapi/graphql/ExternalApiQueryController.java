package com.example.externalapi.graphql;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.example.externalapi.domain.Employee;
import com.example.externalapi.domain.Item;
import com.example.externalapi.domain.RecipeSearchResult;
import com.example.externalapi.domain.CommerceModels.AddressValidation;
import com.example.externalapi.domain.CommerceModels.BulkPriceQuote;
import com.example.externalapi.domain.CommerceModels.Brand;
import com.example.externalapi.domain.CommerceModels.Cart;
import com.example.externalapi.domain.CommerceModels.Category;
import com.example.externalapi.domain.CommerceModels.Coupon;
import com.example.externalapi.domain.CommerceModels.CustomerProfile;
import com.example.externalapi.domain.CommerceModels.DeliverySlotResult;
import com.example.externalapi.domain.CommerceModels.InventoryStatus;
import com.example.externalapi.domain.CommerceModels.LoyaltySummary;
import com.example.externalapi.domain.CommerceModels.Order;
import com.example.externalapi.domain.CommerceModels.OrderSummary;
import com.example.externalapi.domain.CommerceModels.PaymentMethod;
import com.example.externalapi.domain.CommerceModels.PriceLine;
import com.example.externalapi.domain.CommerceModels.Product;
import com.example.externalapi.domain.CommerceModels.ProductReview;
import com.example.externalapi.domain.CommerceModels.QuoteItemInput;
import com.example.externalapi.domain.CommerceModels.ReturnPolicy;
import com.example.externalapi.domain.CommerceModels.ShippingAddressInput;
import com.example.externalapi.domain.CommerceModels.ShippingFeeQuote;
import com.example.externalapi.domain.CommerceModels.Wishlist;
import com.example.externalapi.usecase.GetEmployeeByIdUseCase;
import com.example.externalapi.usecase.GetItemByIdUseCase;
import com.example.externalapi.usecase.CommerceUseCase;
import com.example.externalapi.usecase.SearchRecipeByKeywordUseCase;

import java.util.List;

@Controller
public class ExternalApiQueryController {

    private final GetEmployeeByIdUseCase getEmployeeByIdUseCase;
    private final SearchRecipeByKeywordUseCase searchRecipeByKeywordUseCase;
    private final GetItemByIdUseCase getItemByIdUseCase;
    private final CommerceUseCase commerceUseCase;

    public ExternalApiQueryController(
            GetEmployeeByIdUseCase getEmployeeByIdUseCase,
            SearchRecipeByKeywordUseCase searchRecipeByKeywordUseCase,
            GetItemByIdUseCase getItemByIdUseCase,
            CommerceUseCase commerceUseCase
    ) {
        this.getEmployeeByIdUseCase = getEmployeeByIdUseCase;
        this.searchRecipeByKeywordUseCase = searchRecipeByKeywordUseCase;
        this.getItemByIdUseCase = getItemByIdUseCase;
        this.commerceUseCase = commerceUseCase;
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

    @QueryMapping
    public List<Product> products(@Argument String query, @Argument String categoryId, @Argument String brandId, @Argument Integer limit) {
        return commerceUseCase.products(query, categoryId, brandId, limit);
    }

    @QueryMapping
    public Product productById(@Argument String itemId) {
        return commerceUseCase.productById(itemId);
    }

    @QueryMapping
    public List<Category> categories() {
        return commerceUseCase.categories();
    }

    @QueryMapping
    public List<Product> categoryProducts(@Argument String categoryId, @Argument Integer limit) {
        return commerceUseCase.categoryProducts(categoryId, limit);
    }

    @QueryMapping
    public List<Brand> brands() {
        return commerceUseCase.brands();
    }

    @QueryMapping
    public List<Product> brandProducts(@Argument String brandId, @Argument Integer limit) {
        return commerceUseCase.brandProducts(brandId, limit);
    }

    @QueryMapping
    public List<Product> featuredProducts(@Argument Integer limit) {
        return commerceUseCase.featuredProducts(limit);
    }

    @QueryMapping
    public List<Product> recommendedProducts(@Argument String customerId, @Argument String basedOnItemId, @Argument Integer limit) {
        return commerceUseCase.recommendedProducts(customerId, basedOnItemId, limit);
    }

    @QueryMapping
    public List<Coupon> coupons() {
        return commerceUseCase.coupons();
    }

    @QueryMapping
    public List<Coupon> availableCoupons(@Argument String customerTier) {
        return commerceUseCase.availableCoupons(customerTier);
    }

    @QueryMapping
    public InventoryStatus inventoryStatus(@Argument String itemId) {
        return commerceUseCase.inventoryStatus(itemId);
    }

    @QueryMapping
    public PriceLine priceQuote(@Argument String itemId, @Argument Integer quantity, @Argument String customerTier) {
        return commerceUseCase.priceQuote(itemId, quantity, customerTier);
    }

    @QueryMapping
    public BulkPriceQuote bulkPriceQuote(@Argument List<QuoteItemInput> items, @Argument String customerTier, @Argument String couponCode) {
        return commerceUseCase.bulkPriceQuote(items, customerTier, couponCode);
    }

    @QueryMapping
    public Cart currentCart() {
        return commerceUseCase.currentCart();
    }

    @QueryMapping
    public CustomerProfile customerProfile(@Argument String customerId) {
        return commerceUseCase.customerProfile(customerId);
    }

    @QueryMapping
    public LoyaltySummary loyaltySummary(@Argument String customerId) {
        return commerceUseCase.loyaltySummary(customerId);
    }

    @QueryMapping
    public Wishlist wishlist(@Argument String customerId) {
        return commerceUseCase.wishlist(customerId);
    }

    @QueryMapping
    public List<OrderSummary> orderHistory(@Argument String customerId, @Argument Integer limit) {
        return commerceUseCase.orderHistory(customerId, limit);
    }

    @QueryMapping
    public Order orderById(@Argument String orderId) {
        return commerceUseCase.orderById(orderId);
    }

    @QueryMapping
    public List<ProductReview> productReviews(@Argument String itemId, @Argument Integer limit) {
        return commerceUseCase.productReviews(itemId, limit);
    }

    @QueryMapping
    public List<PaymentMethod> paymentMethods() {
        return commerceUseCase.paymentMethods();
    }

    @QueryMapping
    public ReturnPolicy returnPolicy(@Argument String categoryId) {
        return commerceUseCase.returnPolicy(categoryId);
    }

    @QueryMapping
    public DeliverySlotResult deliverySlots(@Argument String postalCode, @Argument String prefecture) {
        return commerceUseCase.deliverySlots(postalCode, prefecture);
    }

    @QueryMapping
    public ShippingFeeQuote shippingFeeQuote(@Argument String postalCode, @Argument String prefecture, @Argument String shippingMethod, @Argument Integer cartTotal) {
        return commerceUseCase.shippingFeeQuote(postalCode, prefecture, shippingMethod, cartTotal);
    }

    @QueryMapping
    public AddressValidation validateShippingAddress(@Argument ShippingAddressInput input) {
        return commerceUseCase.validateShippingAddress(input);
    }
}
