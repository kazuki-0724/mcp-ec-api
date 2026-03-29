このプロジェクト対応の外部API要件定義（GCP前提 / Java GraphQL）

目的
- 既存MCPツール `get_employee_info` / `get_recipe_by_keyword` / `get_item_info_by_id` が参照する外部APIを提供する
- JavaベースのGraphQL APIとして実装し、GCP上で本番運用できる構成にする
- この要件をそのまま生成AIに渡すだけで、実装着手できる粒度にする

スコープ
1. 提供API
- 社員情報取得
- レシピ検索（キーワード）
- 商品詳細取得（ID）

2. 非スコープ
- 管理画面
- バッチETL
- 商品更新系（Mutation）は初期リリース対象外

業務要件
1. 社員情報
- 入力: 社員ID（例: E001）
- 出力: 氏名、部署

2. レシピ検索
- 入力: キーワード（例: カレー）
- 出力: レシピID、レシピ名、人数、必要材料（ingredientName, requiredQty, itemId）

3. 商品詳細
- 入力: 商品ID（例: G001）
- 出力: 商品ID、商品名、単価、単位、在庫

4. 互換性制約
- 既存MCP側のレスポンス整形に合わせ、フィールド意味を変更しない
- itemId は `requiredIngredients[].itemId` で返す

機能要件（GraphQL）
1. Query API
- `employeeById(employeeId: ID!): Employee`
- `recipeByKeyword(keyword: String!): RecipeSearchResult`
- `itemById(itemId: ID!): Item`

2. エラー方針
- 入力不正は GraphQL エラー `BAD_USER_INPUT`
- 未検出は null + 拡張コード `NOT_FOUND`
- 外部障害は `INTERNAL_ERROR` または `UPSTREAM_TIMEOUT`

3. バリデーション
- employeeId は `^E[0-9]{3}$`
- itemId は `^G[0-9]{3}$`
- keyword は trim 後 1文字以上

GraphQLスキーマ要件（SDL）

```graphql
schema {
  query: Query
}

type Query {
  employeeById(employeeId: ID!): Employee
  recipeByKeyword(keyword: String!): RecipeSearchResult
  itemById(itemId: ID!): Item
}

type Employee {
  employeeId: ID!
  name: String!
  department: String!
}

type RecipeSearchResult {
  keyword: String!
  recipe: Recipe
  nextActionHint: String!
}

type Recipe {
  recipeId: ID!
  recipeName: String!
  servings: Int!
  requiredIngredients: [RequiredIngredient!]!
}

type RequiredIngredient {
  ingredientName: String!
  requiredQty: String!
  itemId: ID!
}

type Item {
  itemId: ID!
  itemName: String!
  unitPrice: Int!
  unit: String!
  stock: Int!
}
```

非機能要件
1. 可用性
- 目標SLO: 月間 99.9%

2. 性能
- p95 応答時間: 300ms 以下（キャッシュヒット時）
- p95 応答時間: 800ms 以下（通常時）
- タイムアウト: 2.5秒（上流待ち）

3. スケール
- Cloud Run autoscaling
- 同時実行数は負荷試験で調整（初期値 40）

4. 監査
- 相関ID、ユーザー識別子、operationName をログ出力
- 機密値はログマスク

GCPアーキテクチャ要件
1. 実行基盤
- Cloud Run（Java 21）
- リージョン: asia-northeast1（推奨）

2. API公開
- API Gateway または HTTPS Load Balancer 配下
- TLS必須

3. 認証
- フロント/BFF向け公開は Firebase Auth ID Token 検証
- サービス間は Workload Identity または Service Account

4. シークレット
- Secret Manager で管理
- DB接続情報、上流APIトークン、署名鍵を格納

5. 監視
- Cloud Logging
- Cloud Monitoring（SLI, SLO, Alert）
- Error Reporting

データストア要件
1. 初期構成（推奨）
- Cloud SQL for PostgreSQL
- テーブル: employees, recipes, recipe_ingredients, items

2. 代替
- Firestore（低運用）
- ただし複雑Joinが増える場合はCloud SQL優先

セキュリティ要件
1. GraphQL防御
- Query depth 制限
- Query complexity 制限
- Introspection は本番で制御

2. 通信防御
- CORS allowlist
- Cloud Armor（レート制限、Bot対策）

3. 依存性
- SCAをCIで実行（Critical/HighでFail）

4. 権限
- 原則最小権限IAM
- Cloud Run実行SAをBFF用/MCP用で分離

API契約（既存MCPとのマッピング）
1. employeeById
- GraphQL応答 `employeeById.name` -> MCP `name`
- GraphQL応答 `employeeById.department` -> MCP `department`

2. recipeByKeyword
- GraphQL応答 `recipeByKeyword.recipe.requiredIngredients` -> MCP `recipe.requiredIngredients`
- GraphQL応答 `recipeByKeyword.nextActionHint` -> MCP `nextActionHint`

3. itemById
- GraphQL応答 `itemById.itemId` -> MCP `itemId`
- GraphQL応答 `itemById.itemName` -> MCP `itemName`

実装要件（Java）
1. 技術スタック
- Spring Boot 3.x
- Spring for GraphQL
- Spring Data JPA
- PostgreSQL Driver
- Micrometer + OpenTelemetry

2. パッケージ構成
- `com.example.externalapi.app`
- `com.example.externalapi.graphql`
- `com.example.externalapi.usecase`
- `com.example.externalapi.domain`
- `com.example.externalapi.infrastructure`
- `com.example.externalapi.config`

3. 例外設計
- `AppException(code, message, status)` を統一利用
- GraphQL error extension に `code` を必ず付与

4. テスト
- 単体テスト: UseCase中心
- 結合テスト: GraphQL query 実行
- 契約テスト: MCP期待レスポンスとの一致確認

受け入れ基準（Definition of Done）
1. 3つのQueryが仕様どおり応答する
2. 無効ID/未存在ID/タイムアウト時のエラーコードが規定どおり
3. Cloud Runへデプロイし疎通確認済み
4. p95、エラー率、タイムアウト率が監視ダッシュボードで確認可能
5. MCP側 `USE_EXTERNAL_APIS=true` で本APIに切替できる

生成AIに渡す実装指示テンプレート（このまま利用可）

```text
あなたはJava/Spring Boot/GraphQLのシニアエンジニアです。
以下仕様を満たすGCP向けプロダクション品質のプロジェクトを生成してください。

【目的】
- 既存MCPツール get_employee_info / get_recipe_by_keyword / get_item_info_by_id が利用する外部APIを提供する。
- Java 21 + Spring Boot 3 + Spring for GraphQL で実装。
- Cloud Runへデプロイ可能な構成。

【必須要件】
1) GraphQL Query
- employeeById(employeeId: ID!): Employee
- recipeByKeyword(keyword: String!): RecipeSearchResult
- itemById(itemId: ID!): Item

2) バリデーション
- employeeId: ^E[0-9]{3}$
- itemId: ^G[0-9]{3}$
- keyword: trim後1文字以上

3) エラー
- BAD_USER_INPUT / NOT_FOUND / UPSTREAM_TIMEOUT / INTERNAL_ERROR
- GraphQL extensions.code に必ず設定

4) DB
- PostgreSQL前提、初期DDLとseedデータを作成

5) 運用
- Micrometerメトリクス、構造化ログ、相関ID
- Dockerfile、Cloud Runデプロイ手順
- 環境変数: DB_*、PORT、ALLOWED_ORIGINS

6) テスト
- UseCase単体テスト
- GraphQL統合テスト
- 主要異常系テスト

【出力してほしいもの】
- 完全なプロジェクト構成
- build.gradle または pom.xml
- GraphQL schema.graphqls
- 主要Javaクラス実装
- application.yml / application-prod.yml
- Dockerfile
- cloudbuild.yaml（任意）
- デプロイコマンド例（gcloud）

【品質条件】
- すべてコンパイル可能
- コードコメントは最小限
- 例外処理と入力検証を必ず実装
```