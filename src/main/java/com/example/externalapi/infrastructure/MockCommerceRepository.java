package com.example.externalapi.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

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

@Repository
public class MockCommerceRepository implements CommerceRepository {

    private final MockExternalApiDataSource dataSource;

    public MockCommerceRepository(MockExternalApiDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<Product> searchProducts(String query, String categoryId, String brandId, int limit) {
        return dataSource.searchProducts(query, categoryId, brandId, limit);
    }

    @Override
    public Optional<Product> findProductById(String itemId) {
        return dataSource.findProductById(itemId);
    }

    @Override
    public List<Category> findAllCategories() {
        return dataSource.listCategories();
    }

    @Override
    public List<Product> findProductsByCategory(String categoryId, int limit) {
        return dataSource.listCategoryProducts(categoryId, limit);
    }

    @Override
    public List<Brand> findAllBrands() {
        return dataSource.listBrands();
    }

    @Override
    public List<Product> findProductsByBrand(String brandId, int limit) {
        return dataSource.listBrandProducts(brandId, limit);
    }

    @Override
    public List<Product> findFeaturedProducts(int limit) {
        return dataSource.listFeaturedProducts(limit);
    }

    @Override
    public List<Product> findRecommendedProducts(String customerId, String basedOnItemId, int limit) {
        return dataSource.listRecommendedProducts(customerId, basedOnItemId, limit);
    }

    @Override
    public List<Coupon> findCoupons() {
        return dataSource.listCoupons();
    }

    @Override
    public List<Coupon> findAvailableCoupons(String customerTier) {
        return dataSource.listAvailableCoupons(customerTier);
    }

    @Override
    public InventoryStatus getInventoryStatus(String itemId) {
        return dataSource.getInventoryStatus(itemId);
    }

    @Override
    public PriceLine getPriceQuote(String itemId, int quantity, String customerTier) {
        return dataSource.getPriceQuote(itemId, quantity, customerTier);
    }

    @Override
    public BulkPriceQuote getBulkPriceQuote(List<QuoteItemInput> items, String customerTier, String couponCode) {
        return dataSource.getBulkPriceQuote(items, customerTier, couponCode);
    }

    @Override
    public Cart getCurrentCart() {
        return dataSource.getCurrentCart();
    }

    @Override
    public Optional<CustomerProfile> findCustomerProfile(String customerId) {
        return dataSource.findCustomerProfile(customerId);
    }

    @Override
    public Optional<LoyaltySummary> findLoyaltySummary(String customerId) {
        return dataSource.findLoyaltySummary(customerId);
    }

    @Override
    public Optional<Wishlist> findWishlist(String customerId) {
        return dataSource.findWishlist(customerId);
    }

    @Override
    public Cart addItemToCart(String itemId, int quantity) {
        return dataSource.addItemToCart(itemId, quantity);
    }

    @Override
    public Cart updateCartItemQuantity(String itemId, int quantity) {
        return dataSource.updateCartItemQuantity(itemId, quantity);
    }

    @Override
    public Cart removeItemFromCart(String itemId) {
        return dataSource.removeItemFromCart(itemId);
    }

    @Override
    public Cart applyCouponToCart(String couponCode) {
        return dataSource.applyCouponToCart(couponCode);
    }

    @Override
    public Wishlist addItemToWishlist(String customerId, String itemId) {
        return dataSource.addItemToWishlist(customerId, itemId);
    }

    @Override
    public List<OrderSummary> findOrderHistory(String customerId, int limit) {
        return dataSource.listOrderHistory(customerId, limit);
    }

    @Override
    public Optional<Order> findOrderById(String orderId) {
        return dataSource.findOrderById(orderId);
    }

    @Override
    public List<ProductReview> findProductReviews(String itemId, int limit) {
        return dataSource.listProductReviews(itemId, limit);
    }

    @Override
    public List<PaymentMethod> findPaymentMethods() {
        return dataSource.listPaymentMethods();
    }

    @Override
    public ReturnPolicy getReturnPolicy(String categoryId) {
        return dataSource.getReturnPolicy(categoryId);
    }

    @Override
    public DeliverySlotResult getDeliverySlots(String postalCode, String prefecture) {
        return dataSource.getDeliverySlots(postalCode, prefecture);
    }

    @Override
    public ShippingFeeQuote getShippingFeeQuote(String postalCode, String prefecture, String shippingMethod, int cartTotal) {
        return dataSource.getShippingFeeQuote(postalCode, prefecture, shippingMethod, cartTotal);
    }

    @Override
    public AddressValidation validateShippingAddress(ShippingAddressInput input) {
        return dataSource.validateShippingAddress(input);
    }
}