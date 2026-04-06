package com.example.externalapi.domain;

import java.time.OffsetDateTime;
import java.util.List;

public final class CommerceModels {

    private CommerceModels() {
    }

    public record Category(
            String categoryId,
            String categoryName,
            String description,
            int productCount
    ) {
    }

    public record Brand(
            String brandId,
            String brandName,
            String country,
            int productCount
    ) {
    }

    public record Product(
            String itemId,
            String itemName,
            String categoryId,
            String categoryName,
            String brandId,
            String brandName,
            int unitPrice,
            String unit,
            int stock,
            List<String> tags,
            String description,
            boolean featured,
            Double averageRating,
            int reviewCount
    ) {
    }

    public record Coupon(
            String couponCode,
            String description,
            String discountType,
            int discountValue,
            int minTotal,
            List<String> eligibleTiers
    ) {
    }

    public record InventoryStatus(
            String itemId,
            int stock,
            String availability,
            int estimatedRestockDays
    ) {
    }

    public record PriceLine(
            String itemId,
            String itemName,
            int quantity,
            int unitPrice,
            int originalSubtotal,
            int discountAmount,
            int subtotal,
            double appliedDiscountRate,
            String unit
    ) {
    }

    public record QuoteItemInput(
            String itemId,
            int quantity
    ) {
    }

    public record BulkPriceQuote(
            String customerTier,
            String couponCode,
            List<PriceLine> items,
            BulkPriceQuoteSummary summary
    ) {
    }

    public record BulkPriceQuoteSummary(
            int subtotal,
            int couponDiscount,
            int grandTotal
    ) {
    }

    public record Cart(
            String cartId,
            String customerId,
            String couponCode,
            List<PriceLine> items,
            CartSummary summary
    ) {
    }

    public record CartSummary(
            int originalSubtotal,
            int lineDiscountTotal,
            int couponDiscount,
            int subtotal,
            int shippingFee,
            int grandTotal
    ) {
    }

    public record Address(
            String postalCode,
            String prefecture,
            String city,
            String line1,
            String line2
    ) {
    }

    public record CustomerProfile(
            String customerId,
            String fullName,
            String email,
            String tier,
            Address defaultAddress,
            List<String> favoriteCategoryIds
    ) {
    }

    public record LoyaltySummary(
            String customerId,
            String tier,
            int points,
            String nextTier,
            Integer nextTierRequiredPoints,
            List<String> perks
    ) {
    }

    public record Wishlist(
            String customerId,
            List<Product> items
    ) {
    }

    public record OrderSummary(
            String orderId,
            OffsetDateTime orderedAt,
            String status,
            int total,
            int itemCount
    ) {
    }

    public record OrderItem(
            String itemId,
            int quantity,
            int unitPrice,
            String itemName
    ) {
    }

    public record Order(
            String orderId,
            String customerId,
            OffsetDateTime orderedAt,
            String status,
            String paymentMethod,
            Address shippingAddress,
            List<OrderItem> items,
            int shippingFee,
            int discountTotal,
            int total
    ) {
    }

    public record ProductReview(
            String reviewId,
            int rating,
            String title,
            String comment,
            String author,
            String createdAt
    ) {
    }

    public record PaymentMethod(
            String code,
            String label,
            int fees
    ) {
    }

    public record ReturnPolicy(
            int days,
            boolean openedPackageAllowed,
            List<String> notes
    ) {
    }

    public record DeliverySlotResult(
            String postalCode,
            String prefecture,
            List<String> slots
    ) {
    }

    public record ShippingFeeQuote(
            String postalCode,
            String prefecture,
            String shippingMethod,
            int shippingFee,
            int freeShippingThreshold,
            int remoteSurcharge
    ) {
    }

    public record ShippingAddressInput(
            String postalCode,
            String prefecture,
            String city,
            String line1,
            String line2
    ) {
    }

    public record AddressValidation(
            boolean isValid,
            Address normalizedAddress,
            List<String> warnings
    ) {
    }
}