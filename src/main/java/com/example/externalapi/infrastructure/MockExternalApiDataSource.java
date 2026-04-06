package com.example.externalapi.infrastructure;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.externalapi.app.AppException;
import com.example.externalapi.domain.Employee;
import com.example.externalapi.domain.Item;
import com.example.externalapi.domain.Recipe;
import com.example.externalapi.domain.RequiredIngredient;
import com.example.externalapi.domain.CommerceModels.Address;
import com.example.externalapi.domain.CommerceModels.AddressValidation;
import com.example.externalapi.domain.CommerceModels.BulkPriceQuote;
import com.example.externalapi.domain.CommerceModels.BulkPriceQuoteSummary;
import com.example.externalapi.domain.CommerceModels.Brand;
import com.example.externalapi.domain.CommerceModels.Cart;
import com.example.externalapi.domain.CommerceModels.CartSummary;
import com.example.externalapi.domain.CommerceModels.Category;
import com.example.externalapi.domain.CommerceModels.Coupon;
import com.example.externalapi.domain.CommerceModels.CustomerProfile;
import com.example.externalapi.domain.CommerceModels.DeliverySlotResult;
import com.example.externalapi.domain.CommerceModels.InventoryStatus;
import com.example.externalapi.domain.CommerceModels.LoyaltySummary;
import com.example.externalapi.domain.CommerceModels.Order;
import com.example.externalapi.domain.CommerceModels.OrderItem;
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

@Component
public class MockExternalApiDataSource {

    private static final String DEFAULT_CUSTOMER_TIER = "bronze";
    private static final String DEFAULT_SHIPPING_METHOD = "standard";
    private static final int STANDARD_SHIPPING_FEE = 550;
    private static final int EXPRESS_SHIPPING_FEE = 880;
    private static final int STANDARD_FREE_SHIPPING_THRESHOLD = 3500;
    private static final int EXPRESS_FREE_SHIPPING_THRESHOLD = 6000;
    private static final int REMOTE_SURCHARGE = 420;

    private final String timeoutEmployeeId;
    private final String timeoutItemId;
    private final String timeoutKeyword;

    private final Object stateLock = new Object();

    private final Map<String, Employee> employees = new LinkedHashMap<>();
    private final Map<String, Category> categories = new LinkedHashMap<>();
    private final Map<String, Brand> brands = new LinkedHashMap<>();
    private final Map<String, Product> products = new LinkedHashMap<>();
    private final List<RecipeCatalogEntry> recipes = new ArrayList<>();
    private final List<String> featuredProductIds = new ArrayList<>();
    private final Map<String, List<String>> recommendationMap = new LinkedHashMap<>();
    private final Map<String, Coupon> coupons = new LinkedHashMap<>();
    private final Map<String, CustomerProfile> customerProfiles = new LinkedHashMap<>();
    private final Map<String, LoyaltySummary> loyaltySummaries = new LinkedHashMap<>();
    private final Map<String, List<String>> wishlistByCustomer = new ConcurrentHashMap<>();
    private final Map<String, List<ProductReview>> reviewsByItem = new LinkedHashMap<>();
    private final List<PaymentMethod> paymentMethods = new ArrayList<>();
    private final Map<String, ReturnPolicy> returnPolicyByCategory = new LinkedHashMap<>();
    private ReturnPolicy defaultReturnPolicy;
    private List<String> defaultDeliverySlots = List.of();
    private List<String> remoteDeliverySlots = List.of();
    private final List<Order> orders = new ArrayList<>();
    private CartState cartState;

    public MockExternalApiDataSource(
            @Value("${app.mock.timeout-employee-id:E999}") String timeoutEmployeeId,
            @Value("${app.mock.timeout-item-id:G999}") String timeoutItemId,
            @Value("${app.mock.timeout-keyword:timeout}") String timeoutKeyword
    ) {
        this.timeoutEmployeeId = timeoutEmployeeId;
        this.timeoutItemId = timeoutItemId;
        this.timeoutKeyword = timeoutKeyword;
        seed();
    }

    public void reset() {
        synchronized (stateLock) {
            clearAll();
            seed();
        }
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
        return findProductById(itemId).map(this::toItem);
    }

    public Optional<Recipe> findRecipeByKeyword(String keyword) {
        if (timeoutKeyword.equalsIgnoreCase(keyword)) {
            throw AppException.upstreamTimeout("mock upstream timeout while fetching recipe");
        }

        String normalizedKeyword = normalize(keyword);
        return recipes.stream()
                .filter(recipe -> recipe.matches(normalizedKeyword, this::normalize))
                .map(RecipeCatalogEntry::recipe)
                .findFirst();
    }

    public List<Product> searchProducts(String query, String categoryId, String brandId, int limit) {
        String normalizedQuery = query == null ? null : normalize(query);
        return products.values().stream()
                .filter(product -> categoryId == null || product.categoryId().equals(categoryId))
                .filter(product -> brandId == null || product.brandId().equals(brandId))
                .filter(product -> matchesProductQuery(product, normalizedQuery))
                .limit(limit)
                .toList();
    }

    public Optional<Product> findProductById(String itemId) {
        return Optional.ofNullable(products.get(itemId));
    }

    public List<Category> listCategories() {
        return List.copyOf(categories.values());
    }

    public List<Product> listCategoryProducts(String categoryId, int limit) {
        ensureCategoryExists(categoryId);
        return searchProducts(null, categoryId, null, limit);
    }

    public List<Brand> listBrands() {
        return List.copyOf(brands.values());
    }

    public List<Product> listBrandProducts(String brandId, int limit) {
        ensureBrandExists(brandId);
        return searchProducts(null, null, brandId, limit);
    }

    public List<Product> listFeaturedProducts(int limit) {
        return featuredProductIds.stream()
                .map(products::get)
                .filter(Objects::nonNull)
                .limit(limit)
                .toList();
    }

    public List<Product> listRecommendedProducts(String customerId, String basedOnItemId, int limit) {
        if (basedOnItemId != null) {
            ensureProductExists(basedOnItemId);
        }
        if (customerId != null) {
            ensureCustomerExists(customerId);
        }

        LinkedHashSet<String> candidateIds = new LinkedHashSet<>();

        if (basedOnItemId != null) {
            candidateIds.addAll(recommendationMap.getOrDefault(basedOnItemId, recommendationMap.getOrDefault("default", List.of())));
        } else if (customerId != null) {
            CustomerProfile profile = customerProfiles.get(customerId);
            profile.favoriteCategoryIds().forEach(categoryId -> searchProducts(null, categoryId, null, limit)
                    .stream()
                    .map(Product::itemId)
                    .forEach(candidateIds::add));
            if (candidateIds.isEmpty()) {
                candidateIds.addAll(recommendationMap.getOrDefault("default", List.of()));
            }
        }

        return candidateIds.stream()
                .map(products::get)
                .filter(Objects::nonNull)
                .limit(limit)
                .toList();
    }

    public List<Coupon> listCoupons() {
        return List.copyOf(coupons.values());
    }

    public List<Coupon> listAvailableCoupons(String customerTier) {
        return coupons.values().stream()
                .filter(coupon -> coupon.eligibleTiers().contains(customerTier))
                .toList();
    }

    public InventoryStatus getInventoryStatus(String itemId) {
        Product product = getRequiredProduct(itemId);
        return new InventoryStatus(itemId, product.stock(), availabilityForStock(product.stock()), estimatedRestockDays(product.stock()));
    }

    public PriceLine getPriceQuote(String itemId, int quantity, String customerTier) {
        return toPriceLine(getRequiredProduct(itemId), quantity, customerTier);
    }

    public BulkPriceQuote getBulkPriceQuote(List<QuoteItemInput> items, String customerTier, String couponCode) {
        List<PriceLine> lines = items.stream()
                .map(item -> toPriceLine(getRequiredProduct(item.itemId()), item.quantity(), customerTier))
                .toList();
        int subtotal = lines.stream().mapToInt(PriceLine::subtotal).sum();
        int couponDiscount = calculateCouponDiscount(couponCode, subtotal, customerTier);
        return new BulkPriceQuote(
                customerTier,
                couponCode,
                lines,
                new BulkPriceQuoteSummary(subtotal, couponDiscount, subtotal - couponDiscount)
        );
    }

    public Cart getCurrentCart() {
        synchronized (stateLock) {
            return buildCart(cartState);
        }
    }

    public Optional<CustomerProfile> findCustomerProfile(String customerId) {
        return Optional.ofNullable(customerProfiles.get(customerId));
    }

    public Optional<LoyaltySummary> findLoyaltySummary(String customerId) {
        return Optional.ofNullable(loyaltySummaries.get(customerId));
    }

    public Optional<Wishlist> findWishlist(String customerId) {
        if (!customerProfiles.containsKey(customerId)) {
            return Optional.empty();
        }
        return Optional.of(new Wishlist(customerId, resolveProducts(wishlistByCustomer.getOrDefault(customerId, List.of()))));
    }

    public Cart addItemToCart(String itemId, int quantity) {
        ensureProductExists(itemId);
        synchronized (stateLock) {
            int currentQuantity = cartState.items().getOrDefault(itemId, 0);
            cartState.items().put(itemId, currentQuantity + quantity);
            return buildCart(cartState);
        }
    }

    public Cart updateCartItemQuantity(String itemId, int quantity) {
        synchronized (stateLock) {
            if (!cartState.items().containsKey(itemId)) {
                throw AppException.notFound("cart item not found: " + itemId);
            }
            if (quantity == 0) {
                cartState.items().remove(itemId);
            } else {
                ensureProductExists(itemId);
                cartState.items().put(itemId, quantity);
            }
            return buildCart(cartState);
        }
    }

    public Cart removeItemFromCart(String itemId) {
        synchronized (stateLock) {
            cartState.items().remove(itemId);
            return buildCart(cartState);
        }
    }

    public Cart applyCouponToCart(String couponCode) {
        Coupon coupon = coupons.get(couponCode);
        if (coupon == null) {
            throw AppException.notFound("coupon not found: " + couponCode);
        }
        synchronized (stateLock) {
            cartState.couponCode = coupon.couponCode();
            return buildCart(cartState);
        }
    }

    public Wishlist addItemToWishlist(String customerId, String itemId) {
        ensureCustomerExists(customerId);
        ensureProductExists(itemId);
        synchronized (stateLock) {
            List<String> current = new ArrayList<>(wishlistByCustomer.getOrDefault(customerId, List.of()));
            if (!current.contains(itemId)) {
                current.add(itemId);
            }
            wishlistByCustomer.put(customerId, List.copyOf(current));
            return new Wishlist(customerId, resolveProducts(current));
        }
    }

    public List<OrderSummary> listOrderHistory(String customerId, int limit) {
        ensureCustomerExists(customerId);
        return orders.stream()
                .filter(order -> order.customerId().equals(customerId))
                .sorted(Comparator.comparing(Order::orderedAt).reversed())
                .limit(limit)
                .map(order -> new OrderSummary(
                        order.orderId(),
                        order.orderedAt(),
                        order.status(),
                        order.total(),
                        order.items().stream().mapToInt(OrderItem::quantity).sum()))
                .toList();
    }

    public Optional<Order> findOrderById(String orderId) {
        return orders.stream()
                .filter(order -> order.orderId().equals(orderId))
                .findFirst();
    }

    public List<ProductReview> listProductReviews(String itemId, int limit) {
        ensureProductExists(itemId);
        return reviewsByItem.getOrDefault(itemId, List.of()).stream()
                .limit(limit)
                .toList();
    }

    public List<PaymentMethod> listPaymentMethods() {
        return List.copyOf(paymentMethods);
    }

    public ReturnPolicy getReturnPolicy(String categoryId) {
        if (categoryId == null) {
            return defaultReturnPolicy;
        }
        return returnPolicyByCategory.getOrDefault(categoryId, defaultReturnPolicy);
    }

    public DeliverySlotResult getDeliverySlots(String postalCode, String prefecture) {
        List<String> slots = isRemotePrefecture(prefecture) ? remoteDeliverySlots : defaultDeliverySlots;
        return new DeliverySlotResult(postalCode, prefecture, slots);
    }

    public ShippingFeeQuote getShippingFeeQuote(String postalCode, String prefecture, String shippingMethod, int cartTotal) {
        String normalizedMethod = shippingMethod == null ? DEFAULT_SHIPPING_METHOD : shippingMethod.toLowerCase(Locale.ROOT);
        int baseFee = "express".equals(normalizedMethod) ? EXPRESS_SHIPPING_FEE : STANDARD_SHIPPING_FEE;
        int freeShippingThreshold = "express".equals(normalizedMethod) ? EXPRESS_FREE_SHIPPING_THRESHOLD : STANDARD_FREE_SHIPPING_THRESHOLD;
        int remoteSurcharge = isRemotePrefecture(prefecture) ? REMOTE_SURCHARGE : 0;
        int shippingFee = cartTotal >= freeShippingThreshold ? 0 : baseFee + remoteSurcharge;
        return new ShippingFeeQuote(postalCode, prefecture, normalizedMethod, shippingFee, freeShippingThreshold, remoteSurcharge);
    }

    public AddressValidation validateShippingAddress(ShippingAddressInput input) {
        List<String> warnings = new ArrayList<>();
        if (input.line1().length() < 3) {
            warnings.add("line1 should be at least 3 characters long");
        }
        Address normalized = new Address(input.postalCode(), input.prefecture(), input.city(), input.line1(), input.line2() == null ? "" : input.line2());
        return new AddressValidation(true, normalized, List.copyOf(warnings));
    }

    private void clearAll() {
        employees.clear();
        categories.clear();
        brands.clear();
        products.clear();
        recipes.clear();
        featuredProductIds.clear();
        recommendationMap.clear();
        coupons.clear();
        customerProfiles.clear();
        loyaltySummaries.clear();
        wishlistByCustomer.clear();
        reviewsByItem.clear();
        paymentMethods.clear();
        returnPolicyByCategory.clear();
        orders.clear();
        defaultReturnPolicy = null;
        defaultDeliverySlots = List.of();
        remoteDeliverySlots = List.of();
        cartState = null;
    }

    private void seed() {
        seedEmployees();
        seedCatalog();
        seedRecipes();
        seedRecommendations();
        seedCoupons();
        seedCustomers();
        seedReviews();
        seedPaymentMethods();
        seedReturnPolicy();
        seedDeliverySlots();
        seedOrders();
        seedWishlist();
        cartState = new CartState("CART-001", "C001", null, new LinkedHashMap<>());
    }

    private void seedEmployees() {
        employees.put("E001", new Employee("E001", "山田太郎", "システム開発部", "ストア運営責任者", "taro.yamada@example.com"));
        employees.put("E002", new Employee("E002", "佐藤花子", "インフラ推進部", "SRE", "hanako.sato@example.com"));
        employees.put("E003", new Employee("E003", "鈴木一郎", "EC商品企画部", "MD", "ichiro.suzuki@example.com"));
    }

    private void seedCatalog() {
        categories.put("CAT-FRESH", new Category("CAT-FRESH", "生鮮食品", "野菜・肉・乳製品", 4));
        categories.put("CAT-PANTRY", new Category("CAT-PANTRY", "加工食品", "レトルト・調味料・乾物", 3));
        categories.put("CAT-DAILY", new Category("CAT-DAILY", "日用品", "洗剤・キッチン用品", 1));
        categories.put("CAT-BEAUTY", new Category("CAT-BEAUTY", "ビューティー", "スキンケア・ヘアケア", 1));
        categories.put("CAT-BEVERAGE", new Category("CAT-BEVERAGE", "飲料", "水・お茶・コーヒー", 1));

        brands.put("BR-LOCAL", new Brand("BR-LOCAL", "Local Farm", "日本", 4));
        brands.put("BR-AURA", new Brand("BR-AURA", "Aura Select", "日本", 3));
        brands.put("BR-CLEAN", new Brand("BR-CLEAN", "Clean Home", "日本", 1));
        brands.put("BR-GLOW", new Brand("BR-GLOW", "Glow Lab", "日本", 1));
        brands.put("BR-BREW", new Brand("BR-BREW", "Morning Brew", "日本", 1));

        addProduct("G001", "国産 玉ねぎ", "CAT-FRESH", "BR-LOCAL", 120, "個", 82, List.of("野菜", "カレー", "定番"), "甘みの強い国産玉ねぎ。", true);
        addProduct("G002", "北海道 じゃがいも", "CAT-FRESH", "BR-LOCAL", 80, "個", 105, List.of("野菜", "カレー", "煮込み"), "煮崩れしにくい男爵系。", true);
        addProduct("G003", "にんじん", "CAT-FRESH", "BR-LOCAL", 90, "本", 66, List.of("野菜", "カレー", "サラダ"), "みずみずしい国産にんじん。", false);
        addProduct("G004", "牛こま切れ肉", "CAT-FRESH", "BR-LOCAL", 450, "100g", 40, List.of("肉", "カレー", "炒め物"), "旨みのある国産牛こま。", true);
        addProduct("G005", "カレールー 中辛", "CAT-PANTRY", "BR-AURA", 260, "箱", 120, List.of("カレー", "ルー", "定番"), "コクと香りのバランスが良い中辛。", true);
        addProduct("G006", "国産 福神漬け", "CAT-PANTRY", "BR-AURA", 150, "袋", 90, List.of("カレー", "付け合わせ"), "甘さ控えめの福神漬け。", false);
        addProduct("G007", "濃縮だしつゆ", "CAT-PANTRY", "BR-AURA", 380, "本", 54, List.of("調味料", "和食"), "煮物や麺つゆに使える濃縮タイプ。", false);
        addProduct("G008", "台所用洗剤 シトラス", "CAT-DAILY", "BR-CLEAN", 298, "本", 73, List.of("洗剤", "キッチン"), "油汚れに強い台所用洗剤。", true);
        addProduct("G009", "高保湿フェイスマスク 7枚入", "CAT-BEAUTY", "BR-GLOW", 680, "袋", 31, List.of("美容", "保湿", "スキンケア"), "夜の集中ケア向けフェイスマスク。", false);
        addProduct("G010", "ドリップコーヒー 10袋", "CAT-BEVERAGE", "BR-BREW", 540, "箱", 88, List.of("コーヒー", "朝食"), "すっきりした後味のブレンド。", true);
    }

    private void seedRecipes() {
        recipes.add(new RecipeCatalogEntry(
                new Recipe(
                        "RECIPE001",
                        "カレー",
                        4,
                        List.of(
                                new RequiredIngredient("玉ねぎ", "2個", "G001"),
                                new RequiredIngredient("じゃがいも", "3個", "G002"),
                                new RequiredIngredient("にんじん", "1本", "G003"),
                                new RequiredIngredient("牛こま切れ肉", "300g", "G004"),
                                new RequiredIngredient("カレールー", "1箱", "G005"),
                                new RequiredIngredient("福神漬け", "1袋", "G006")
                        )
                ),
                List.of("カレー", "カレーライス", "カレーの材料")
        ));
        recipes.add(new RecipeCatalogEntry(
                new Recipe(
                        "RECIPE002",
                        "肉じゃが",
                        3,
                        List.of(
                                new RequiredIngredient("玉ねぎ", "1個", "G001"),
                                new RequiredIngredient("じゃがいも", "4個", "G002"),
                                new RequiredIngredient("牛こま切れ肉", "250g", "G004"),
                                new RequiredIngredient("濃縮だしつゆ", "50ml", "G007")
                        )
                ),
                List.of("肉じゃが", "煮物")
        ));
    }

    private void seedRecommendations() {
        featuredProductIds.addAll(List.of("G001", "G002", "G004", "G005", "G008", "G010"));
        recommendationMap.put("G005", List.of("G001", "G002", "G003", "G004", "G006"));
        recommendationMap.put("G010", List.of("G009", "G008"));
        recommendationMap.put("default", List.of("G005", "G010", "G008", "G001"));
    }

    private void seedCoupons() {
        coupons.put("WELCOME10", new Coupon("WELCOME10", "初回購入10%オフ", "percentage", 10, 1500, List.of("bronze", "silver", "gold")));
        coupons.put("PANTRY200", new Coupon("PANTRY200", "加工食品カテゴリ200円オフ", "fixed", 200, 1200, List.of("silver", "gold")));
        coupons.put("GOLD15", new Coupon("GOLD15", "ゴールド会員15%オフ", "percentage", 15, 3000, List.of("gold")));
    }

    private void seedCustomers() {
        customerProfiles.put("C001", new CustomerProfile(
                "C001",
                "橋本和樹",
                "kazuki.hashimoto@example.com",
                "gold",
                new Address("1500001", "東京都", "渋谷区", "神宮前1-2-3", "Apt 502"),
                List.of("CAT-FRESH", "CAT-BEVERAGE")
        ));
        customerProfiles.put("C002", new CustomerProfile(
                "C002",
                "中村悠",
                "yu.nakamura@example.com",
                "silver",
                new Address("5300001", "大阪府", "大阪市北区", "梅田2-4-9", ""),
                List.of("CAT-PANTRY", "CAT-DAILY")
        ));

        loyaltySummaries.put("C001", new LoyaltySummary("C001", "gold", 4820, null, null, List.of("送料無料", "先行セール", "誕生月クーポン")));
        loyaltySummaries.put("C002", new LoyaltySummary("C002", "silver", 1840, "gold", 3000, List.of("限定クーポン", "レビュー特典")));
    }

    private void seedReviews() {
        reviewsByItem.put("G005", List.of(
                new ProductReview("R001", 5, "コクが深い", "野菜の甘みとよく合います。", "M.T", "2026-03-14"),
                new ProductReview("R002", 4, "家族向け", "辛すぎず食べやすいです。", "K.S", "2026-02-26")
        ));
        reviewsByItem.put("G010", List.of(
                new ProductReview("R003", 5, "朝にちょうど良い", "香りが良く毎朝飲んでいます。", "Y.N", "2026-03-01")
        ));
        reviewsByItem.put("G008", List.of(
                new ProductReview("R004", 4, "油落ちが良い", "少量で十分に落ちます。", "H.K", "2026-01-18")
        ));
        recomputeProductRatings();
    }

    private void seedPaymentMethods() {
        paymentMethods.add(new PaymentMethod("credit_card", "クレジットカード", 0));
        paymentMethods.add(new PaymentMethod("apple_pay", "Apple Pay", 0));
        paymentMethods.add(new PaymentMethod("cod", "代金引換", 330));
        paymentMethods.add(new PaymentMethod("bank_transfer", "銀行振込", 0));
    }

    private void seedReturnPolicy() {
        defaultReturnPolicy = new ReturnPolicy(14, false, List.of("到着後14日以内に申請してください。", "生鮮食品は品質不良時を除き返品不可です。"));
        returnPolicyByCategory.put("CAT-FRESH", new ReturnPolicy(2, false, List.of("生鮮食品は到着翌日までにご連絡ください。")));
        returnPolicyByCategory.put("CAT-BEAUTY", new ReturnPolicy(30, false, List.of("未開封品のみ返品可能です。")));
    }

    private void seedDeliverySlots() {
        defaultDeliverySlots = List.of("08:00-12:00", "12:00-14:00", "14:00-16:00", "16:00-18:00", "18:00-20:00");
        remoteDeliverySlots = List.of("12:00-14:00", "16:00-18:00");
    }

    private void seedOrders() {
        orders.add(new Order(
                "O1001",
                "C001",
                OffsetDateTime.parse("2026-03-28T10:30:00+09:00"),
                "shipped",
                "credit_card",
                new Address("1500001", "東京都", "渋谷区", "神宮前1-2-3", "Apt 502"),
                List.of(
                        new OrderItem("G005", 2, 260, products.get("G005").itemName()),
                        new OrderItem("G006", 1, 150, products.get("G006").itemName()),
                        new OrderItem("G010", 1, 540, products.get("G010").itemName())
                ),
                0,
                104,
                1106
        ));
        orders.add(new Order(
                "O1002",
                "C002",
                OffsetDateTime.parse("2026-03-20T18:20:00+09:00"),
                "delivered",
                "cod",
                new Address("5300001", "大阪府", "大阪市北区", "梅田2-4-9", ""),
                List.of(
                        new OrderItem("G008", 2, 298, products.get("G008").itemName()),
                        new OrderItem("G007", 1, 380, products.get("G007").itemName())
                ),
                480,
                0,
                1456
        ));
    }

    private void seedWishlist() {
        wishlistByCustomer.put("C001", List.of("G009", "G010"));
        wishlistByCustomer.put("C002", List.of("G008"));
    }

    private void addProduct(String itemId, String itemName, String categoryId, String brandId, int unitPrice, String unit, int stock, List<String> tags, String description, boolean featured) {
        Category category = categories.get(categoryId);
        Brand brand = brands.get(brandId);
        products.put(itemId, new Product(
                itemId,
                itemName,
                categoryId,
                category == null ? null : category.categoryName(),
                brandId,
                brand == null ? null : brand.brandName(),
                unitPrice,
                unit,
                stock,
                List.copyOf(tags),
                description,
                featured,
                null,
                0
        ));
    }

    private void recomputeProductRatings() {
        products.replaceAll((itemId, product) -> {
            List<ProductReview> productReviews = reviewsByItem.getOrDefault(itemId, List.of());
            Double averageRating = productReviews.isEmpty()
                    ? null
                    : productReviews.stream().mapToInt(ProductReview::rating).average().orElse(0.0);
            return new Product(
                    product.itemId(),
                    product.itemName(),
                    product.categoryId(),
                    product.categoryName(),
                    product.brandId(),
                    product.brandName(),
                    product.unitPrice(),
                    product.unit(),
                    product.stock(),
                    product.tags(),
                    product.description(),
                    product.featured(),
                    averageRating,
                    productReviews.size()
            );
        });
    }

    private boolean matchesProductQuery(Product product, String normalizedQuery) {
        if (normalizedQuery == null || normalizedQuery.isEmpty()) {
            return true;
        }
        return contains(product.itemName(), normalizedQuery)
                || contains(product.description(), normalizedQuery)
                || contains(product.categoryName(), normalizedQuery)
                || contains(product.brandName(), normalizedQuery)
                || product.tags().stream().map(this::normalize).anyMatch(tag -> tag.contains(normalizedQuery));
    }

    private boolean contains(String value, String normalizedQuery) {
        return value != null && normalize(value).contains(normalizedQuery);
    }

    private List<Product> resolveProducts(Collection<String> itemIds) {
        return itemIds.stream()
                .map(products::get)
                .filter(Objects::nonNull)
                .toList();
    }

    private Product getRequiredProduct(String itemId) {
        return findProductById(itemId)
                .orElseThrow(() -> AppException.notFound("product not found: " + itemId));
    }

    private Item toItem(Product product) {
        return new Item(
                product.itemId(),
                product.itemName(),
                product.categoryId(),
                product.brandId(),
                product.unitPrice(),
                product.unit(),
                product.stock(),
                product.tags()
        );
    }

    private PriceLine toPriceLine(Product product, int quantity, String customerTier) {
        int originalSubtotal = product.unitPrice() * quantity;
        int discountRate = Math.min(memberDiscountRate(customerTier) + quantityDiscountRate(quantity), 25);
        int discountAmount = originalSubtotal * discountRate / 100;
        return new PriceLine(
                product.itemId(),
                product.itemName(),
                quantity,
                product.unitPrice(),
                originalSubtotal,
                discountAmount,
                originalSubtotal - discountAmount,
                discountRate,
                product.unit()
        );
    }

    private int memberDiscountRate(String customerTier) {
        return switch (customerTier == null ? DEFAULT_CUSTOMER_TIER : customerTier.toLowerCase(Locale.ROOT)) {
            case "gold" -> 10;
            case "silver" -> 5;
            default -> 0;
        };
    }

    private int quantityDiscountRate(int quantity) {
        if (quantity >= 10) {
            return 8;
        }
        if (quantity >= 5) {
            return 5;
        }
        return 0;
    }

    private int calculateCouponDiscount(String couponCode, int subtotal, String customerTier) {
        if (couponCode == null) {
            return 0;
        }
        Coupon coupon = coupons.get(couponCode);
        if (coupon == null) {
            throw AppException.notFound("coupon not found: " + couponCode);
        }
        if (!coupon.eligibleTiers().contains(customerTier)) {
            return 0;
        }
        if (subtotal < coupon.minTotal()) {
            return 0;
        }
        return switch (coupon.discountType()) {
            case "fixed" -> Math.min(coupon.discountValue(), subtotal);
            case "percentage" -> subtotal * coupon.discountValue() / 100;
            default -> 0;
        };
    }

    private Cart buildCart(CartState state) {
        String tier = customerProfiles.getOrDefault(state.customerId(), new CustomerProfile(state.customerId(), "", "", DEFAULT_CUSTOMER_TIER, new Address("", "", "", "", ""), List.of())).tier();
        List<PriceLine> lines = state.items().entrySet().stream()
                .map(entry -> toPriceLine(getRequiredProduct(entry.getKey()), entry.getValue(), tier))
                .toList();
        int originalSubtotal = lines.stream().mapToInt(PriceLine::originalSubtotal).sum();
        int lineDiscountTotal = lines.stream().mapToInt(PriceLine::discountAmount).sum();
        int subtotal = lines.stream().mapToInt(PriceLine::subtotal).sum();
        int couponDiscount = calculateCouponDiscount(state.couponCode, subtotal, tier);
        int discountedTotal = subtotal - couponDiscount;
        int shippingFee = discountedTotal >= STANDARD_FREE_SHIPPING_THRESHOLD ? 0 : STANDARD_SHIPPING_FEE;
        return new Cart(
                state.cartId(),
                state.customerId(),
                state.couponCode,
                lines,
                new CartSummary(originalSubtotal, lineDiscountTotal, couponDiscount, subtotal, shippingFee, discountedTotal + shippingFee)
        );
    }

    private String availabilityForStock(int stock) {
        if (stock > 50) {
            return "in_stock";
        }
        if (stock > 10) {
            return "limited";
        }
        return "low_stock";
    }

    private int estimatedRestockDays(int stock) {
        return stock <= 10 ? 5 : 0;
    }

    private boolean isRemotePrefecture(String prefecture) {
        return "北海道".equals(prefecture) || "沖縄県".equals(prefecture);
    }

    private void ensureCustomerExists(String customerId) {
        if (!customerProfiles.containsKey(customerId)) {
            throw AppException.notFound("customer not found: " + customerId);
        }
    }

    private void ensureCategoryExists(String categoryId) {
        if (!categories.containsKey(categoryId)) {
            throw AppException.notFound("category not found: " + categoryId);
        }
    }

    private void ensureBrandExists(String brandId) {
        if (!brands.containsKey(brandId)) {
            throw AppException.notFound("brand not found: " + brandId);
        }
    }

    private void ensureProductExists(String itemId) {
        if (!products.containsKey(itemId)) {
            throw AppException.notFound("product not found: " + itemId);
        }
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private record RecipeCatalogEntry(Recipe recipe, List<String> keywords) {
        private boolean matches(String normalizedKeyword, java.util.function.Function<String, String> normalizer) {
            if (normalizer.apply(recipe.recipeName()).contains(normalizedKeyword)) {
                return true;
            }
            if (keywords.stream().map(normalizer).anyMatch(keyword -> keyword.contains(normalizedKeyword))) {
                return true;
            }
            return recipe.requiredIngredients().stream()
                    .map(RequiredIngredient::ingredientName)
                    .map(normalizer)
                    .anyMatch(name -> name.contains(normalizedKeyword));
        }
    }

    private static final class CartState {
        private final String cartId;
        private final String customerId;
        private String couponCode;
        private final Map<String, Integer> items;

        private CartState(String cartId, String customerId, String couponCode, Map<String, Integer> items) {
            this.cartId = cartId;
            this.customerId = customerId;
            this.couponCode = couponCode;
            this.items = items;
        }

        private String cartId() {
            return cartId;
        }

        private String customerId() {
            return customerId;
        }

        private Map<String, Integer> items() {
            return items;
        }
    }
}
