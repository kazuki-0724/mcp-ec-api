# mock依存機能をGraphQL API化するための実装仕様

このドキュメントは、現在mockデータで提供している機能を、external GraphQL APIとして置き換えるための実装仕様です。

対象は次の2系統です。

1. すでにexternal呼び出し前提で存在するGraphQL API
2. まだmockでしか動いていないEC系API

このドキュメントの目的は、external GraphQLサーバを実装する人が、そのままQuery、Mutation、型、戻り値、業務ルールを定義できる状態にすることです。

## 1. 前提

現在のアプリは次の経路でデータを取得します。

1. フロントエンドがExpress APIを呼ぶ
2. Express APIがMCPツールを呼ぶ
3. MCPツールがGatewayを通じてデータ取得する
4. mockモードではmockDatabaseを参照する
5. local / productionモードではexternal GraphQL APIを参照する想定

現状、external実装がある扱いなのは次の3件だけです。

- EmployeeById
- RecipeByKeyword
- ItemById

それ以外のEC系機能はすべてmock依存です。

## 2. 実装方針

今回GraphQL API化する対象は、現在mockデータソースが持っている責務全体です。

つまり、次のmockデータ取得・更新処理をGraphQLで提供すれば、mock依存を外せます。

| mockデータソースの責務 | GraphQL APIとして必要な操作 | 種別 |
| --- | --- | --- |
| getEmployeeInfo | employeeById | Query |
| getRecipeByKeyword | recipeByKeyword | Query |
| getItemInfoById | itemById | Query |
| listProducts | products | Query |
| getProduct | productById | Query |
| listCategories | categories | Query |
| listBrands | brands | Query |
| listCoupons | coupons, availableCoupons | Query |
| getCustomerProfile | customerProfile | Query |
| getLoyaltySummary | loyaltySummary | Query |
| getWishlistItemIds | wishlist | Query |
| saveWishlistItemIds | addItemToWishlist | Mutation |
| listOrders | orderHistory | Query |
| getOrder | orderById | Query |
| getProductReviews | productReviews | Query |
| getPaymentMethods | paymentMethods | Query |
| getReturnPolicy | returnPolicy | Query |
| getDeliverySlots | deliverySlots | Query |
| getCart | currentCart | Query |
| saveCart | addItemToCart, updateCartItemQuantity, removeItemFromCart, applyCouponToCart | Mutation |
| getFeaturedProductIds | featuredProducts | Query |
| getRecommendationItemIds | recommendedProducts | Query |

## 3. 互換性要件

external GraphQL APIは、現在のユースケース層が返している値と同等の情報を返せる必要があります。

互換性の考え方は次の通りです。

1. フロントエンド互換ではなく、Gateway互換を優先する
2. ただし、employeeById、recipeByKeyword、itemById は既存probeと整合する名前を維持する
3. cartやwishlistのような更新系は、更新後の最新スナップショットを返す
4. このアプリはデモ用途のため、cartは現状どおり単一のcurrentCartとして実装してよい

## 4. 推奨GraphQLスキーマ

以下は、このアプリを置き換えるための推奨SDLです。

```graphql
scalar DateTime

type Query {
  employeeById(employeeId: ID!): Employee
  recipeByKeyword(keyword: String!): RecipeSearchResult
  itemById(itemId: ID!): ItemBasic

  products(query: String, categoryId: ID, brandId: ID, limit: Int = 8): [Product!]!
  productById(itemId: ID!): Product
  categories: [Category!]!
  categoryProducts(categoryId: ID!, limit: Int = 12): [Product!]!
  brands: [Brand!]!
  brandProducts(brandId: ID!, limit: Int = 12): [Product!]!
  featuredProducts(limit: Int = 6): [Product!]!
  recommendedProducts(customerId: ID, basedOnItemId: ID, limit: Int = 6): [Product!]!

  coupons: [Coupon!]!
  availableCoupons(customerTier: String): [Coupon!]!

  inventoryStatus(itemId: ID!): InventoryStatus
  priceQuote(itemId: ID!, quantity: Int = 1, customerTier: String): PriceLine
  bulkPriceQuote(items: [QuoteItemInput!]!, customerTier: String, couponCode: String): BulkPriceQuote!

  currentCart: Cart!
  customerProfile(customerId: ID!): CustomerProfile
  loyaltySummary(customerId: ID!): LoyaltySummary
  wishlist(customerId: ID!): Wishlist!
  orderHistory(customerId: ID!, limit: Int = 5): [OrderSummary!]!
  orderById(orderId: ID!): Order

  productReviews(itemId: ID!, limit: Int = 5): [ProductReview!]!
  paymentMethods: [PaymentMethod!]!
  returnPolicy(categoryId: ID): ReturnPolicy!
  deliverySlots(postalCode: String!, prefecture: String!): DeliverySlotResult!
  shippingFeeQuote(postalCode: String!, prefecture: String!, shippingMethod: String = "standard", cartTotal: Int = 0): ShippingFeeQuote!
  validateShippingAddress(input: ShippingAddressInput!): AddressValidation!
}

type Mutation {
  addItemToCart(itemId: ID!, quantity: Int = 1): Cart!
  updateCartItemQuantity(itemId: ID!, quantity: Int!): Cart!
  removeItemFromCart(itemId: ID!): Cart!
  applyCouponToCart(couponCode: String!): Cart!
  addItemToWishlist(customerId: ID!, itemId: ID!): Wishlist!
}

type Employee {
  employeeId: ID!
  name: String!
  department: String!
  role: String
  email: String
}

type RecipeSearchResult {
  keyword: String!
  nextActionHint: String
  recipe: Recipe
}

type Recipe {
  recipeId: ID!
  recipeName: String!
  servings: Int!
  requiredIngredients: [RecipeIngredient!]!
}

type RecipeIngredient {
  ingredientName: String!
  requiredQty: String!
  itemId: ID!
}

type ItemBasic {
  itemId: ID!
  itemName: String!
  categoryId: ID
  brandId: ID
  unitPrice: Int!
  unit: String!
  stock: Int!
  tags: [String!]!
}

type Category {
  categoryId: ID!
  categoryName: String!
  description: String
  productCount: Int!
}

type Brand {
  brandId: ID!
  brandName: String!
  country: String
  productCount: Int!
}

type Product {
  itemId: ID!
  itemName: String!
  categoryId: ID!
  categoryName: String
  brandId: ID!
  brandName: String
  unitPrice: Int!
  unit: String!
  stock: Int!
  tags: [String!]!
  description: String
  featured: Boolean!
  averageRating: Float
  reviewCount: Int!
}

type Coupon {
  couponCode: String!
  description: String!
  discountType: String!
  discountValue: Int!
  minTotal: Int!
  eligibleTiers: [String!]!
}

type InventoryStatus {
  itemId: ID!
  stock: Int!
  availability: String!
  estimatedRestockDays: Int!
}

type PriceLine {
  itemId: ID!
  itemName: String!
  quantity: Int!
  unitPrice: Int!
  originalSubtotal: Int!
  discountAmount: Int!
  subtotal: Int!
  appliedDiscountRate: Float!
  unit: String!
}

input QuoteItemInput {
  itemId: ID!
  quantity: Int!
}

type BulkPriceQuote {
  customerTier: String!
  couponCode: String
  items: [PriceLine!]!
  summary: BulkPriceQuoteSummary!
}

type BulkPriceQuoteSummary {
  subtotal: Int!
  couponDiscount: Int!
  grandTotal: Int!
}

type Cart {
  cartId: ID!
  customerId: ID!
  couponCode: String
  items: [PriceLine!]!
  summary: CartSummary!
}

type CartSummary {
  originalSubtotal: Int!
  lineDiscountTotal: Int!
  couponDiscount: Int!
  subtotal: Int!
  shippingFee: Int!
  grandTotal: Int!
}

type Address {
  postalCode: String!
  prefecture: String!
  city: String!
  line1: String!
  line2: String!
}

type CustomerProfile {
  customerId: ID!
  fullName: String!
  email: String!
  tier: String!
  defaultAddress: Address!
  favoriteCategoryIds: [ID!]!
}

type LoyaltySummary {
  customerId: ID!
  tier: String!
  points: Int!
  nextTier: String
  nextTierRequiredPoints: Int
  perks: [String!]!
}

type Wishlist {
  customerId: ID!
  items: [Product!]!
}

type OrderSummary {
  orderId: ID!
  orderedAt: DateTime!
  status: String!
  total: Int!
  itemCount: Int!
}

type OrderItem {
  itemId: ID!
  quantity: Int!
  unitPrice: Int!
  itemName: String
}

type Order {
  orderId: ID!
  customerId: ID!
  orderedAt: DateTime!
  status: String!
  paymentMethod: String!
  shippingAddress: Address!
  items: [OrderItem!]!
  shippingFee: Int!
  discountTotal: Int!
  total: Int!
}

type ProductReview {
  reviewId: ID!
  rating: Int!
  title: String!
  comment: String!
  author: String!
  createdAt: String!
}

type PaymentMethod {
  code: String!
  label: String!
  fees: Int!
}

type ReturnPolicy {
  days: Int!
  openedPackageAllowed: Boolean!
  notes: [String!]!
}

type DeliverySlotResult {
  postalCode: String!
  prefecture: String!
  slots: [String!]!
}

type ShippingFeeQuote {
  postalCode: String!
  prefecture: String!
  shippingMethod: String!
  shippingFee: Int!
  freeShippingThreshold: Int!
  remoteSurcharge: Int!
}

input ShippingAddressInput {
  postalCode: String!
  prefecture: String!
  city: String!
  line1: String!
  line2: String
}

type AddressValidation {
  isValid: Boolean!
  normalizedAddress: Address!
  warnings: [String!]!
}
```

## 5. Query仕様

この節では、実装優先度が高いQueryから順に仕様を整理します。

### 5.1 employeeById

用途:
社員情報取得。既存のexternalモード想定APIです。

Query:

```graphql
query EmployeeById($employeeId: ID!) {
  employeeById(employeeId: $employeeId) {
    employeeId
    name
    department
    role
    email
  }
}
```

variables:

```json
{ "employeeId": "E001" }
```

期待レスポンス:

```json
{
  "data": {
    "employeeById": {
      "employeeId": "E001",
      "name": "山田太郎",
      "department": "システム開発部",
      "role": "ストア運営責任者",
      "email": "taro.yamada@example.com"
    }
  }
}
```

### 5.2 recipeByKeyword

用途:
レシピ取得。具材のitemIdを返し、商品照会へつなげます。

Query:

```graphql
query RecipeByKeyword($keyword: String!) {
  recipeByKeyword(keyword: $keyword) {
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

### 5.3 itemById

用途:
商品基本情報取得。既存のexternalモード想定APIです。

Query:

```graphql
query ItemById($itemId: ID!) {
  itemById(itemId: $itemId) {
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

### 5.4 products

用途:
商品検索。現在のsearch_products相当です。

Query:

```graphql
query Products($query: String, $categoryId: ID, $brandId: ID, $limit: Int) {
  products(query: $query, categoryId: $categoryId, brandId: $brandId, limit: $limit) {
    itemId
    itemName
    categoryId
    categoryName
    brandId
    brandName
    unitPrice
    unit
    stock
    tags
    description
    featured
    averageRating
    reviewCount
  }
}
```

フィルタ仕様:

- query, categoryId, brandId のいずれか1つ以上を指定可能
- queryは商品名、説明、タグ、カテゴリ名、ブランド名への部分一致
- limit未指定時は8

### 5.5 productById

用途:
商品詳細取得。現在のget_product_details相当です。

### 5.6 categories と categoryProducts

用途:
カテゴリ一覧、およびカテゴリ配下商品一覧。

### 5.7 brands と brandProducts

用途:
ブランド一覧、およびブランド配下商品一覧。

### 5.8 featuredProducts

用途:
トップ掲載用注目商品一覧。

### 5.9 recommendedProducts

用途:
顧客起点または商品起点のおすすめ商品。

制約:

- customerId または basedOnItemId のどちらかは必須

### 5.10 coupons と availableCoupons

用途:
クーポン一覧、および会員ランク別の利用可能クーポン一覧。

ルール:

- customerTierが未指定なら bronze 扱い
- eligibleTiers に customerTier を含むものだけ返す

### 5.11 currentCart

用途:
現在カート取得。現行デモでは単一カート前提です。

### 5.12 customerProfile と loyaltySummary

用途:
顧客プロフィール、および会員情報取得。

### 5.13 wishlist

用途:
お気に入り一覧取得。返却値はitemId配列ではなく、商品詳細配列で返すことを推奨します。

### 5.14 orderHistory と orderById

用途:
注文履歴と注文詳細。

ルール:

- itemCount は items.quantity の合計
- limit未指定時は5

### 5.15 productReviews

用途:
商品レビュー取得。

### 5.16 paymentMethods

用途:
利用可能決済手段取得。

### 5.17 returnPolicy

用途:
返品ポリシー取得。

ルール:

- categoryId指定時はカテゴリ別ルールを優先
- 該当カテゴリがなければdefaultを返す

### 5.18 deliverySlots

用途:
配送時間帯取得。

ルール:

- 北海道、沖縄県は remote スロットを返す
- それ以外は default スロットを返す

### 5.19 shippingFeeQuote

用途:
送料見積。

ルール:

- shippingMethodが未指定なら standard
- standard の送料無料閾値は 3500
- express の送料無料閾値は 6000
- 北海道、沖縄県は remoteSurcharge 420
- standardの基本送料は550、expressは880

### 5.20 validateShippingAddress

用途:
配送先形式チェック。

ルール:

- postalCode はハイフンなし7桁
- line1 が3文字未満なら warnings を返す
- 正規化結果は normalizedAddress にそのまま返す

## 6. Mutation仕様

### 6.1 addItemToCart

用途:
商品をカートへ追加し、更新後カートを返します。

Mutation:

```graphql
mutation AddItemToCart($itemId: ID!, $quantity: Int!) {
  addItemToCart(itemId: $itemId, quantity: $quantity) {
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

ルール:

- quantity未指定時は1
- 既存商品があれば加算
- 商品が存在しなければエラー

### 6.2 updateCartItemQuantity

用途:
カート内商品の数量更新。

ルール:

- quantity は 0 以上
- quantity が 0 の場合は削除扱い
- 対象商品がカートに存在しなければエラー

### 6.3 removeItemFromCart

用途:
カートから商品削除。

ルール:

- 対象商品がなくても結果は最新カートを返してよい

### 6.4 applyCouponToCart

用途:
クーポン適用。

ルール:

- couponCode が存在しない場合はエラー
- 更新後カートを返す

### 6.5 addItemToWishlist

用途:
お気に入り追加。

ルール:

- customerId と itemId は必須
- すでに存在するitemIdは重複追加しない
- 顧客または商品が存在しなければエラー
- 更新後Wishlistを返す

## 7. 業務ルール

mock実装に合わせるなら、次の業務ルールをGraphQLサーバ側で再現してください。

### 7.1 価格計算

- customerTier が gold なら 10% 引き
- customerTier が silver なら 5% 引き
- quantity が 5以上なら 5% 引き
- quantity が 10以上なら 8% 引き
- 会員割引と数量割引の合計は最大25%

### 7.2 カート送料

- 商品小計が3500以上なら送料無料
- それ未満は550

### 7.3 クーポン

- fixed は固定額値引き
- percentage は割合値引き
- subtotal が minTotal 未満なら適用しない

### 7.4 在庫状態

- stock > 50 なら in_stock
- stock > 10 なら limited
- それ以下は low_stock
- low_stock の estimatedRestockDays は 5

## 8. エラー方針

GraphQLサーバ側では、入力不備や未検出データを明示的に扱ってください。

推奨方針:

- 入力不備は GraphQL error を返す
- 対象未検出は nullable field を返すか、業務エラーとしてGraphQL errorにする
- 既存アプリ互換を優先するなら、not found は null より明示エラーの方が実装しやすい

最低限、次のケースはハンドリングが必要です。

- 商品未検出
- 顧客未検出
- 注文未検出
- クーポン未検出
- quantity不正
- 郵便番号形式不正

## 9. 実装優先順

最短でmock依存を減らすなら、次の順で実装します。

1. employeeById
2. recipeByKeyword
3. itemById
4. products
5. productById
6. categories
7. brands
8. customerProfile
9. loyaltySummary
10. currentCart
11. addItemToCart
12. updateCartItemQuantity
13. removeItemFromCart
14. applyCouponToCart
15. wishlist
16. addItemToWishlist
17. orderHistory
18. orderById
19. featuredProducts
20. recommendedProducts

## 10. 受け入れ条件

GraphQL API実装完了の判断基準は次の通りです。

1. localモードで employeeById、recipeByKeyword、itemById が動く
2. mockなしでも商品検索、商品詳細、カテゴリ、ブランド一覧が取得できる
3. currentCart と4つのcart mutationが動く
4. wishlist、orderHistory、orderById が動く
5. featuredProducts と recommendedProducts が動く
6. existing UI が mockモード以外でも致命的エラーなく表示できる

## 11. このドキュメントで実装対象としている範囲

このドキュメントは、今mockで提供している業務データを外出しするためのGraphQL API仕様です。

対象外:

- Expressの /api/chat 自体の仕様
- MCPプロトコル自体の仕様
- Gemini呼び出しの仕様

対象:

- external GraphQL APIのQueryとMutation
- モックと同じ業務ルール
- モックと同等の返却データ

## 12. 参照元コード

- mockデータ本体: src/gateways/mock/mockDatabase.js
- mockデータソース: src/gateways/mock/createMockDataSource.js
- 業務ロジック: src/gateways/createCommerceGateway.js
- ユースケース: src/usecases/commerceUseCases.js
- 既存external入口: src/gateways/external/createExternalDataSource.js
- GraphQL probe定義: src/server/config/externalApiTarget.js
