const INITIAL_MOCK_DATABASE = {
    employees: {
        E001: { employeeId: "E001", name: "山田太郎", department: "システム開発部", role: "ストア運営責任者", email: "taro.yamada@example.com" },
        E002: { employeeId: "E002", name: "佐藤花子", department: "インフラ推進部", role: "SRE", email: "hanako.sato@example.com" },
        E003: { employeeId: "E003", name: "鈴木一郎", department: "EC商品企画部", role: "MD", email: "ichiro.suzuki@example.com" }
    },
    categories: [
        { categoryId: "CAT-FRESH", categoryName: "生鮮食品", description: "野菜・肉・乳製品" },
        { categoryId: "CAT-PANTRY", categoryName: "加工食品", description: "レトルト・調味料・乾物" },
        { categoryId: "CAT-DAILY", categoryName: "日用品", description: "洗剤・キッチン用品" },
        { categoryId: "CAT-BEAUTY", categoryName: "ビューティー", description: "スキンケア・ヘアケア" },
        { categoryId: "CAT-BEVERAGE", categoryName: "飲料", description: "水・お茶・コーヒー" }
    ],
    brands: [
        { brandId: "BR-LOCAL", brandName: "Local Farm", country: "日本" },
        { brandId: "BR-AURA", brandName: "Aura Select", country: "日本" },
        { brandId: "BR-CLEAN", brandName: "Clean Home", country: "日本" },
        { brandId: "BR-GLOW", brandName: "Glow Lab", country: "日本" },
        { brandId: "BR-BREW", brandName: "Morning Brew", country: "日本" }
    ],
    products: [
        { itemId: "G001", itemName: "国産 玉ねぎ", categoryId: "CAT-FRESH", brandId: "BR-LOCAL", unitPrice: 120, unit: "個", stock: 82, tags: ["野菜", "カレー", "定番"], description: "甘みの強い国産玉ねぎ。", featured: true },
        { itemId: "G002", itemName: "北海道 じゃがいも", categoryId: "CAT-FRESH", brandId: "BR-LOCAL", unitPrice: 80, unit: "個", stock: 105, tags: ["野菜", "カレー", "煮込み"], description: "煮崩れしにくい男爵系。", featured: true },
        { itemId: "G003", itemName: "にんじん", categoryId: "CAT-FRESH", brandId: "BR-LOCAL", unitPrice: 90, unit: "本", stock: 66, tags: ["野菜", "カレー", "サラダ"], description: "みずみずしい国産にんじん。", featured: false },
        { itemId: "G004", itemName: "牛こま切れ肉", categoryId: "CAT-FRESH", brandId: "BR-LOCAL", unitPrice: 450, unit: "100g", stock: 40, tags: ["肉", "カレー", "炒め物"], description: "旨みのある国産牛こま。", featured: true },
        { itemId: "G005", itemName: "カレールー 中辛", categoryId: "CAT-PANTRY", brandId: "BR-AURA", unitPrice: 260, unit: "箱", stock: 120, tags: ["カレー", "ルー", "定番"], description: "コクと香りのバランスが良い中辛。", featured: true },
        { itemId: "G006", itemName: "国産 福神漬け", categoryId: "CAT-PANTRY", brandId: "BR-AURA", unitPrice: 150, unit: "袋", stock: 90, tags: ["カレー", "付け合わせ"], description: "甘さ控えめの福神漬け。", featured: false },
        { itemId: "G007", itemName: "濃縮だしつゆ", categoryId: "CAT-PANTRY", brandId: "BR-AURA", unitPrice: 380, unit: "本", stock: 54, tags: ["調味料", "和食"], description: "煮物や麺つゆに使える濃縮タイプ。", featured: false },
        { itemId: "G008", itemName: "台所用洗剤 シトラス", categoryId: "CAT-DAILY", brandId: "BR-CLEAN", unitPrice: 298, unit: "本", stock: 73, tags: ["洗剤", "キッチン"], description: "油汚れに強い台所用洗剤。", featured: true },
        { itemId: "G009", itemName: "高保湿フェイスマスク 7枚入", categoryId: "CAT-BEAUTY", brandId: "BR-GLOW", unitPrice: 680, unit: "袋", stock: 31, tags: ["美容", "保湿", "スキンケア"], description: "夜の集中ケア向けフェイスマスク。", featured: false },
        { itemId: "G010", itemName: "ドリップコーヒー 10袋", categoryId: "CAT-BEVERAGE", brandId: "BR-BREW", unitPrice: 540, unit: "箱", stock: 88, tags: ["コーヒー", "朝食"], description: "すっきりした後味のブレンド。", featured: true }
    ],
    recipes: [
        {
            id: "RECIPE001",
            name: "カレー",
            keywords: ["カレー", "カレーライス", "カレーの材料"],
            servings: 4,
            ingredients: [
                { ingredientName: "玉ねぎ", requiredQty: "2個", itemId: "G001" },
                { ingredientName: "じゃがいも", requiredQty: "3個", itemId: "G002" },
                { ingredientName: "にんじん", requiredQty: "1本", itemId: "G003" },
                { ingredientName: "牛こま切れ肉", requiredQty: "300g", itemId: "G004" },
                { ingredientName: "カレールー", requiredQty: "1箱", itemId: "G005" },
                { ingredientName: "福神漬け", requiredQty: "1袋", itemId: "G006" }
            ]
        },
        {
            id: "RECIPE002",
            name: "肉じゃが",
            keywords: ["肉じゃが", "煮物"],
            servings: 3,
            ingredients: [
                { ingredientName: "玉ねぎ", requiredQty: "1個", itemId: "G001" },
                { ingredientName: "じゃがいも", requiredQty: "4個", itemId: "G002" },
                { ingredientName: "牛こま切れ肉", requiredQty: "250g", itemId: "G004" },
                { ingredientName: "濃縮だしつゆ", requiredQty: "50ml", itemId: "G007" }
            ]
        }
    ],
    featuredProductIds: ["G001", "G002", "G004", "G005", "G008", "G010"],
    recommendationMap: {
        G005: ["G001", "G002", "G003", "G004", "G006"],
        G010: ["G009", "G008"],
        default: ["G005", "G010", "G008", "G001"]
    },
    coupons: [
        { couponCode: "WELCOME10", description: "初回購入10%オフ", discountType: "percentage", discountValue: 10, minTotal: 1500, eligibleTiers: ["bronze", "silver", "gold"] },
        { couponCode: "PANTRY200", description: "加工食品カテゴリ200円オフ", discountType: "fixed", discountValue: 200, minTotal: 1200, eligibleTiers: ["silver", "gold"] },
        { couponCode: "GOLD15", description: "ゴールド会員15%オフ", discountType: "percentage", discountValue: 15, minTotal: 3000, eligibleTiers: ["gold"] }
    ],
    customerProfiles: {
        C001: {
            customerId: "C001",
            fullName: "橋本和樹",
            email: "kazuki.hashimoto@example.com",
            tier: "gold",
            defaultAddress: {
                postalCode: "1500001",
                prefecture: "東京都",
                city: "渋谷区",
                line1: "神宮前1-2-3",
                line2: "Apt 502"
            },
            favoriteCategoryIds: ["CAT-FRESH", "CAT-BEVERAGE"]
        },
        C002: {
            customerId: "C002",
            fullName: "中村悠",
            email: "yu.nakamura@example.com",
            tier: "silver",
            defaultAddress: {
                postalCode: "5300001",
                prefecture: "大阪府",
                city: "大阪市北区",
                line1: "梅田2-4-9",
                line2: ""
            },
            favoriteCategoryIds: ["CAT-PANTRY", "CAT-DAILY"]
        }
    },
    loyalty: {
        C001: { customerId: "C001", tier: "gold", points: 4820, nextTier: null, nextTierRequiredPoints: null, perks: ["送料無料", "先行セール", "誕生月クーポン"] },
        C002: { customerId: "C002", tier: "silver", points: 1840, nextTier: "gold", nextTierRequiredPoints: 3000, perks: ["限定クーポン", "レビュー特典"] }
    },
    wishlist: {
        C001: ["G009", "G010"],
        C002: ["G008"]
    },
    orders: [
        {
            orderId: "O1001",
            customerId: "C001",
            orderedAt: "2026-03-28T10:30:00+09:00",
            status: "shipped",
            paymentMethod: "credit_card",
            shippingAddress: { postalCode: "1500001", prefecture: "東京都", city: "渋谷区", line1: "神宮前1-2-3", line2: "Apt 502" },
            items: [
                { itemId: "G005", quantity: 2, unitPrice: 260 },
                { itemId: "G006", quantity: 1, unitPrice: 150 },
                { itemId: "G010", quantity: 1, unitPrice: 540 }
            ],
            shippingFee: 0,
            discountTotal: 104,
            total: 1106
        },
        {
            orderId: "O1002",
            customerId: "C002",
            orderedAt: "2026-03-20T18:20:00+09:00",
            status: "delivered",
            paymentMethod: "cod",
            shippingAddress: { postalCode: "5300001", prefecture: "大阪府", city: "大阪市北区", line1: "梅田2-4-9", line2: "" },
            items: [
                { itemId: "G008", quantity: 2, unitPrice: 298 },
                { itemId: "G007", quantity: 1, unitPrice: 380 }
            ],
            shippingFee: 480,
            discountTotal: 0,
            total: 1456
        }
    ],
    reviews: {
        G005: [
            { reviewId: "R001", rating: 5, title: "コクが深い", comment: "野菜の甘みとよく合います。", author: "M.T", createdAt: "2026-03-14" },
            { reviewId: "R002", rating: 4, title: "家族向け", comment: "辛すぎず食べやすいです。", author: "K.S", createdAt: "2026-02-26" }
        ],
        G010: [
            { reviewId: "R003", rating: 5, title: "朝にちょうど良い", comment: "香りが良く毎朝飲んでいます。", author: "Y.N", createdAt: "2026-03-01" }
        ],
        G008: [
            { reviewId: "R004", rating: 4, title: "油落ちが良い", comment: "少量で十分に落ちます。", author: "H.K", createdAt: "2026-01-18" }
        ]
    },
    paymentMethods: [
        { code: "credit_card", label: "クレジットカード", fees: 0 },
        { code: "apple_pay", label: "Apple Pay", fees: 0 },
        { code: "cod", label: "代金引換", fees: 330 },
        { code: "bank_transfer", label: "銀行振込", fees: 0 }
    ],
    returnPolicy: {
        default: {
            days: 14,
            openedPackageAllowed: false,
            notes: ["到着後14日以内に申請してください。", "生鮮食品は品質不良時を除き返品不可です。"]
        },
        byCategory: {
            "CAT-FRESH": { days: 2, openedPackageAllowed: false, notes: ["生鮮食品は到着翌日までにご連絡ください。"] },
            "CAT-BEAUTY": { days: 30, openedPackageAllowed: false, notes: ["未開封品のみ返品可能です。"] }
        }
    },
    deliverySlots: {
        default: ["08:00-12:00", "12:00-14:00", "14:00-16:00", "16:00-18:00", "18:00-20:00"],
        remote: ["12:00-14:00", "16:00-18:00"]
    },
    cart: {
        cartId: "CART-001",
        customerId: "C001",
        couponCode: null,
        items: []
    }
};

export function createMockDatabase() {
    return JSON.parse(JSON.stringify(INITIAL_MOCK_DATABASE));
}