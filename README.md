# External API Mock GraphQL Service

Spring Boot 3 + Java 21 + Spring for GraphQL で構成した、MCP 向け外部 API のモック実装です。実DBは使わず、メモリ上のモックデータを返します。

## 提供 Query

- employeeById(employeeId: ID!): Employee
- recipeByKeyword(keyword: String!): RecipeSearchResult
- itemById(itemId: ID!): ItemBasic
- products(query: String, categoryId: ID, brandId: ID, limit: Int): [Product!]!
- productById(itemId: ID!): Product
- categories: [Category!]!
- categoryProducts(categoryId: ID!, limit: Int): [Product!]!
- brands: [Brand!]!
- brandProducts(brandId: ID!, limit: Int): [Product!]!
- featuredProducts(limit: Int): [Product!]!
- recommendedProducts(customerId: ID, basedOnItemId: ID, limit: Int): [Product!]!
- coupons: [Coupon!]!
- availableCoupons(customerTier: String): [Coupon!]!
- inventoryStatus(itemId: ID!): InventoryStatus
- priceQuote(itemId: ID!, quantity: Int, customerTier: String): PriceLine
- bulkPriceQuote(items: [QuoteItemInput!]!, customerTier: String, couponCode: String): BulkPriceQuote!
- currentCart: Cart!
- customerProfile(customerId: ID!): CustomerProfile
- loyaltySummary(customerId: ID!): LoyaltySummary
- wishlist(customerId: ID!): Wishlist!
- orderHistory(customerId: ID!, limit: Int): [OrderSummary!]!
- orderById(orderId: ID!): Order
- productReviews(itemId: ID!, limit: Int): [ProductReview!]!
- paymentMethods: [PaymentMethod!]!
- returnPolicy(categoryId: ID): ReturnPolicy!
- deliverySlots(postalCode: String!, prefecture: String!): DeliverySlotResult!
- shippingFeeQuote(postalCode: String!, prefecture: String!, shippingMethod: String, cartTotal: Int): ShippingFeeQuote!
- validateShippingAddress(input: ShippingAddressInput!): AddressValidation!

## 提供 Mutation

- addItemToCart(itemId: ID!, quantity: Int = 1): Cart!
- updateCartItemQuantity(itemId: ID!, quantity: Int!): Cart!
- removeItemFromCart(itemId: ID!): Cart!
- applyCouponToCart(couponCode: String!): Cart!
- addItemToWishlist(customerId: ID!, itemId: ID!): Wishlist!

## ローカル起動

1. PowerShell でプロジェクト直下へ移動
2. 次を実行

```powershell
.\mvnw.cmd spring-boot:run
```

GraphiQL:

- http://localhost:8081/graphiql
- http://localhost:8081/graphiql?path=/graphql

GraphQL endpoint:

- http://localhost:8081/graphql

## テスト実行

```powershell
.\mvnw.cmd test
```

## GraphiQL でのテスト方法

1. アプリを起動する
2. ブラウザで http://localhost:8081/graphiql?path=/graphql を開く
3. 左側の大きいエディタに Query または Mutation を貼り付けて実行する

注意:

- query や mutation の文字列は Headers ではなくクエリエディタに貼る
- Headers タブには JSON だけを書ける
- Headers が不要なら空のままでよい

Headers の例:

```json
{
  "X-Correlation-Id": "local-test-001",
  "X-User-Id": "demo-user"
}
```

もし Variables を使う場合は、Query 本体と分けて Variables タブへ JSON を入れる

Variables の例:

```json
{
  "keyword": "カレー"
}
```

## GraphiQL 用サンプル Query

```graphql
query EmployeeById {
  employeeById(employeeId: "E001") {
    employeeId
    name
    department
    role
    email
  }
}
```

```graphql
query RecipeByKeyword {
  recipeByKeyword(keyword: "カレー") {
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
```

```graphql
query RecipeByKeyword($keyword: String!) {
  recipeByKeyword(keyword: $keyword) {
    keyword
    nextActionHint
    recipe {
      recipeId
      recipeName
    }
  }
}
```

```graphql
query ItemById {
  itemById(itemId: "G001") {
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
```

```graphql
query Products {
  products(query: "カレー", limit: 5) {
    itemId
    itemName
    categoryName
    brandName
    unitPrice
    stock
    featured
    averageRating
    reviewCount
  }
}
```

```graphql
query ProductById {
  productById(itemId: "G005") {
    itemId
    itemName
    categoryName
    brandName
    description
    tags
    unitPrice
    stock
    averageRating
    reviewCount
  }
}
```

```graphql
query CategoriesAndBrands {
  categories {
    categoryId
    categoryName
    productCount
  }
  brands {
    brandId
    brandName
    country
    productCount
  }
}
```

```graphql
query FeaturedAndRecommended {
  featuredProducts(limit: 4) {
    itemId
    itemName
  }
  recommendedProducts(customerId: "C001", limit: 4) {
    itemId
    itemName
    categoryName
  }
}
```

```graphql
query CouponsAndQuote {
  availableCoupons(customerTier: "gold") {
    couponCode
    description
    discountType
    discountValue
  }
  priceQuote(itemId: "G005", quantity: 5, customerTier: "gold") {
    itemId
    itemName
    quantity
    originalSubtotal
    discountAmount
    subtotal
    appliedDiscountRate
  }
}
```

```graphql
query CartAndCustomer {
  currentCart {
    cartId
    customerId
    couponCode
    items {
      itemId
      itemName
      quantity
      subtotal
    }
    summary {
      subtotal
      shippingFee
      grandTotal
    }
  }
  customerProfile(customerId: "C001") {
    customerId
    fullName
    tier
    defaultAddress {
      postalCode
      prefecture
      city
      line1
    }
  }
  loyaltySummary(customerId: "C001") {
    customerId
    tier
    points
    perks
  }
}
```

```graphql
query WishlistAndOrders {
  wishlist(customerId: "C001") {
    customerId
    items {
      itemId
      itemName
      unitPrice
    }
  }
  orderHistory(customerId: "C001", limit: 3) {
    orderId
    orderedAt
    status
    total
    itemCount
  }
}
```

```graphql
query OrderDetailAndReviews {
  orderById(orderId: "O1001") {
    orderId
    customerId
    orderedAt
    status
    paymentMethod
    items {
      itemId
      itemName
      quantity
      unitPrice
    }
    total
  }
  productReviews(itemId: "G005", limit: 5) {
    reviewId
    rating
    title
    author
    createdAt
  }
}
```

```graphql
query DeliveryAndAddress {
  deliverySlots(postalCode: "9000001", prefecture: "沖縄県") {
    postalCode
    prefecture
    slots
  }
  shippingFeeQuote(postalCode: "9000001", prefecture: "沖縄県", shippingMethod: "standard", cartTotal: 1000) {
    shippingMethod
    shippingFee
    freeShippingThreshold
    remoteSurcharge
  }
  validateShippingAddress(input: {
    postalCode: "1500001"
    prefecture: "東京都"
    city: "渋谷区"
    line1: "神宮前1-2-3"
    line2: "Apt 502"
  }) {
    isValid
    warnings
    normalizedAddress {
      postalCode
      prefecture
      city
      line1
      line2
    }
  }
}
```

## GraphiQL 用サンプル Mutation

```graphql
mutation AddItemToCart {
  addItemToCart(itemId: "G005", quantity: 2) {
    cartId
    items {
      itemId
      itemName
      quantity
      subtotal
    }
    summary {
      subtotal
      shippingFee
      grandTotal
    }
  }
}
```

```graphql
mutation UpdateCartItemQuantity {
  updateCartItemQuantity(itemId: "G005", quantity: 5) {
    cartId
    items {
      itemId
      quantity
      appliedDiscountRate
    }
    summary {
      lineDiscountTotal
      grandTotal
    }
  }
}
```

```graphql
mutation ApplyCouponToCart {
  applyCouponToCart(couponCode: "WELCOME10") {
    cartId
    couponCode
    summary {
      couponDiscount
      grandTotal
    }
  }
}
```

```graphql
mutation RemoveItemFromCart {
  removeItemFromCart(itemId: "G005") {
    cartId
    items {
      itemId
      quantity
    }
    summary {
      grandTotal
    }
  }
}
```

```graphql
mutation AddItemToWishlist {
  addItemToWishlist(customerId: "C001", itemId: "G008") {
    customerId
    items {
      itemId
      itemName
    }
  }
}
```

## モック挙動

- 正常系
  - employeeId: E001, E002, E003
  - customerId: C001, C002
  - orderId: O1001, O1002
  - itemId: G001 から G010
  - keyword: カレー, 肉じゃが, 玉ねぎ など
- 未存在
  - employeeId: E998
  - customerId: C998
  - orderId: O9999
  - itemId: G998
  - keyword: 該当なしの文字列
- タイムアウト模擬
  - employeeId: E999
  - itemId: G999
  - keyword: timeout

## 環境変数

- PORT: デフォルト 8081
- ALLOWED_ORIGINS: CORS 許可 Origin。カンマ区切り
- GRAPHQL_INTROSPECTION_ENABLED: introspection 制御

## 監視/運用

- Health: http://localhost:8081/actuator/health
- Metrics: http://localhost:8081/actuator/metrics
- Prometheus: http://localhost:8081/actuator/prometheus
- 相関 ID: X-Correlation-Id
- ユーザー識別子: X-User-Id

## Docker

```powershell
docker build -t external-api-mock .
docker run --rm -p 8081:8081 -e PORT=8081 external-api-mock
```

## Cloud Run 例

```powershell
gcloud run deploy external-api \
  --source . \
  --region asia-northeast1 \
  --platform managed \
  --allow-unauthenticated \
  --set-env-vars ALLOWED_ORIGINS=https://example.com,GRAPHQL_INTROSPECTION_ENABLED=false
```
