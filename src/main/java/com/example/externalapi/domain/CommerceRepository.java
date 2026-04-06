package com.example.externalapi.domain;

import java.util.List;
import java.util.Optional;

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

public interface CommerceRepository {

    List<Product> searchProducts(String query, String categoryId, String brandId, int limit);

    Optional<Product> findProductById(String itemId);

    List<Category> findAllCategories();

    List<Product> findProductsByCategory(String categoryId, int limit);

    List<Brand> findAllBrands();

    List<Product> findProductsByBrand(String brandId, int limit);

    List<Product> findFeaturedProducts(int limit);

    List<Product> findRecommendedProducts(String customerId, String basedOnItemId, int limit);

    List<Coupon> findCoupons();

    List<Coupon> findAvailableCoupons(String customerTier);

    InventoryStatus getInventoryStatus(String itemId);

    PriceLine getPriceQuote(String itemId, int quantity, String customerTier);

    BulkPriceQuote getBulkPriceQuote(List<QuoteItemInput> items, String customerTier, String couponCode);

    Cart getCurrentCart();

    Optional<CustomerProfile> findCustomerProfile(String customerId);

    Optional<LoyaltySummary> findLoyaltySummary(String customerId);

    Optional<Wishlist> findWishlist(String customerId);

    Cart addItemToCart(String itemId, int quantity);

    Cart updateCartItemQuantity(String itemId, int quantity);

    Cart removeItemFromCart(String itemId);

    Cart applyCouponToCart(String couponCode);

    Wishlist addItemToWishlist(String customerId, String itemId);

    List<OrderSummary> findOrderHistory(String customerId, int limit);

    Optional<Order> findOrderById(String orderId);

    List<ProductReview> findProductReviews(String itemId, int limit);

    List<PaymentMethod> findPaymentMethods();

    ReturnPolicy getReturnPolicy(String categoryId);

    DeliverySlotResult getDeliverySlots(String postalCode, String prefecture);

    ShippingFeeQuote getShippingFeeQuote(String postalCode, String prefecture, String shippingMethod, int cartTotal);

    AddressValidation validateShippingAddress(ShippingAddressInput input);
}