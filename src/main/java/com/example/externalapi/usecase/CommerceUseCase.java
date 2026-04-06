package com.example.externalapi.usecase;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.externalapi.app.AppException;
import com.example.externalapi.domain.CommerceRepository;
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

@Service
public class CommerceUseCase {

    private final CommerceRepository commerceRepository;
    private final InputValidator inputValidator;

    public CommerceUseCase(CommerceRepository commerceRepository, InputValidator inputValidator) {
        this.commerceRepository = commerceRepository;
        this.inputValidator = inputValidator;
    }

    public List<Product> products(String query, String categoryId, String brandId, Integer limit) {
        return commerceRepository.searchProducts(query, categoryId, brandId, inputValidator.validatePositiveLimit(limit, 8));
    }

    public Product productById(String itemId) {
        String validatedItemId = inputValidator.validateItemId(itemId);
        return commerceRepository.findProductById(validatedItemId)
                .orElseThrow(() -> AppException.notFound("product not found: " + validatedItemId));
    }

    public List<Category> categories() {
        return commerceRepository.findAllCategories();
    }

    public List<Product> categoryProducts(String categoryId, Integer limit) {
        return commerceRepository.findProductsByCategory(inputValidator.validateRequiredText(categoryId, "categoryId"), inputValidator.validatePositiveLimit(limit, 12));
    }

    public List<Brand> brands() {
        return commerceRepository.findAllBrands();
    }

    public List<Product> brandProducts(String brandId, Integer limit) {
        return commerceRepository.findProductsByBrand(inputValidator.validateRequiredText(brandId, "brandId"), inputValidator.validatePositiveLimit(limit, 12));
    }

    public List<Product> featuredProducts(Integer limit) {
        return commerceRepository.findFeaturedProducts(inputValidator.validatePositiveLimit(limit, 6));
    }

    public List<Product> recommendedProducts(String customerId, String basedOnItemId, Integer limit) {
        if ((customerId == null || customerId.isBlank()) && (basedOnItemId == null || basedOnItemId.isBlank())) {
            throw AppException.badUserInput("either customerId or basedOnItemId is required");
        }
        String validatedCustomerId = customerId == null || customerId.isBlank() ? null : inputValidator.validateCustomerId(customerId);
        String validatedItemId = basedOnItemId == null || basedOnItemId.isBlank() ? null : inputValidator.validateItemId(basedOnItemId);
        return commerceRepository.findRecommendedProducts(validatedCustomerId, validatedItemId, inputValidator.validatePositiveLimit(limit, 6));
    }

    public List<Coupon> coupons() {
        return commerceRepository.findCoupons();
    }

    public List<Coupon> availableCoupons(String customerTier) {
        return commerceRepository.findAvailableCoupons(inputValidator.normalizeTier(customerTier));
    }

    public InventoryStatus inventoryStatus(String itemId) {
        return commerceRepository.getInventoryStatus(inputValidator.validateItemId(itemId));
    }

    public PriceLine priceQuote(String itemId, Integer quantity, String customerTier) {
        return commerceRepository.getPriceQuote(
                inputValidator.validateItemId(itemId),
                inputValidator.validatePositiveQuantity(quantity == null ? 1 : quantity, "quantity"),
                inputValidator.normalizeTier(customerTier)
        );
    }

    public BulkPriceQuote bulkPriceQuote(List<QuoteItemInput> items, String customerTier, String couponCode) {
        if (items == null || items.isEmpty()) {
            throw AppException.badUserInput("items must not be empty");
        }
        items.forEach(item -> {
            inputValidator.validateItemId(item.itemId());
            inputValidator.validatePositiveQuantity(item.quantity(), "quantity");
        });
        String normalizedCouponCode = couponCode == null || couponCode.isBlank() ? null : inputValidator.validateRequiredText(couponCode, "couponCode").toUpperCase();
        return commerceRepository.getBulkPriceQuote(items, inputValidator.normalizeTier(customerTier), normalizedCouponCode);
    }

    public Cart currentCart() {
        return commerceRepository.getCurrentCart();
    }

    public CustomerProfile customerProfile(String customerId) {
        String validatedCustomerId = inputValidator.validateCustomerId(customerId);
        return commerceRepository.findCustomerProfile(validatedCustomerId)
                .orElseThrow(() -> AppException.notFound("customer not found: " + validatedCustomerId));
    }

    public LoyaltySummary loyaltySummary(String customerId) {
        String validatedCustomerId = inputValidator.validateCustomerId(customerId);
        return commerceRepository.findLoyaltySummary(validatedCustomerId)
                .orElseThrow(() -> AppException.notFound("loyalty summary not found: " + validatedCustomerId));
    }

    public Wishlist wishlist(String customerId) {
        String validatedCustomerId = inputValidator.validateCustomerId(customerId);
        return commerceRepository.findWishlist(validatedCustomerId)
                .orElseThrow(() -> AppException.notFound("wishlist not found: " + validatedCustomerId));
    }

    public List<OrderSummary> orderHistory(String customerId, Integer limit) {
        return commerceRepository.findOrderHistory(inputValidator.validateCustomerId(customerId), inputValidator.validatePositiveLimit(limit, 5));
    }

    public Order orderById(String orderId) {
        String validatedOrderId = inputValidator.validateOrderId(orderId);
        return commerceRepository.findOrderById(validatedOrderId)
                .orElseThrow(() -> AppException.notFound("order not found: " + validatedOrderId));
    }

    public List<ProductReview> productReviews(String itemId, Integer limit) {
        return commerceRepository.findProductReviews(inputValidator.validateItemId(itemId), inputValidator.validatePositiveLimit(limit, 5));
    }

    public List<PaymentMethod> paymentMethods() {
        return commerceRepository.findPaymentMethods();
    }

    public ReturnPolicy returnPolicy(String categoryId) {
        return commerceRepository.getReturnPolicy(categoryId == null || categoryId.isBlank() ? null : inputValidator.validateRequiredText(categoryId, "categoryId"));
    }

    public DeliverySlotResult deliverySlots(String postalCode, String prefecture) {
        return commerceRepository.getDeliverySlots(inputValidator.validatePostalCode(postalCode), inputValidator.validateRequiredText(prefecture, "prefecture"));
    }

    public ShippingFeeQuote shippingFeeQuote(String postalCode, String prefecture, String shippingMethod, Integer cartTotal) {
        return commerceRepository.getShippingFeeQuote(
                inputValidator.validatePostalCode(postalCode),
                inputValidator.validateRequiredText(prefecture, "prefecture"),
                shippingMethod == null || shippingMethod.isBlank() ? "standard" : shippingMethod.trim().toLowerCase(),
                inputValidator.validateNonNegativeQuantity(cartTotal == null ? 0 : cartTotal, "cartTotal")
        );
    }

    public AddressValidation validateShippingAddress(ShippingAddressInput input) {
        if (input == null) {
            throw AppException.badUserInput("input must not be null");
        }
        ShippingAddressInput normalizedInput = new ShippingAddressInput(
                inputValidator.validatePostalCode(input.postalCode()),
                inputValidator.validateRequiredText(input.prefecture(), "prefecture"),
                inputValidator.validateRequiredText(input.city(), "city"),
                inputValidator.validateRequiredText(input.line1(), "line1"),
                input.line2() == null ? "" : input.line2().trim()
        );
        return commerceRepository.validateShippingAddress(normalizedInput);
    }

    public Cart addItemToCart(String itemId, Integer quantity) {
        return commerceRepository.addItemToCart(
                inputValidator.validateItemId(itemId),
                inputValidator.validatePositiveQuantity(quantity == null ? 1 : quantity, "quantity")
        );
    }

    public Cart updateCartItemQuantity(String itemId, int quantity) {
        return commerceRepository.updateCartItemQuantity(
                inputValidator.validateItemId(itemId),
                inputValidator.validateNonNegativeQuantity(quantity, "quantity")
        );
    }

    public Cart removeItemFromCart(String itemId) {
        return commerceRepository.removeItemFromCart(inputValidator.validateItemId(itemId));
    }

    public Cart applyCouponToCart(String couponCode) {
        return commerceRepository.applyCouponToCart(inputValidator.validateRequiredText(couponCode, "couponCode").toUpperCase());
    }

    public Wishlist addItemToWishlist(String customerId, String itemId) {
        return commerceRepository.addItemToWishlist(inputValidator.validateCustomerId(customerId), inputValidator.validateItemId(itemId));
    }
}