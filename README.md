# External API Mock GraphQL Service

Spring Boot 3 + Java 21 + Spring for GraphQL で構成した、MCP 向け外部 API のモック実装です。実DBは使わず、メモリ上のモックデータを返します。

## 提供 Query

- employeeById(employeeId: ID!): Employee
- recipeByKeyword(keyword: String!): RecipeSearchResult
- itemById(itemId: ID!): Item

## ローカル起動

1. PowerShell でプロジェクト直下へ移動
2. 次を実行

```powershell
.\mvnw.cmd spring-boot:run
```

GraphiQL:

- http://localhost:8081/graphiql

GraphQL endpoint:

- http://localhost:8081/graphql

## テスト実行

```powershell
.\mvnw.cmd test
```

## サンプル Query

```graphql
query EmployeeById {
  employeeById(employeeId: "E001") {
    employeeId
    name
    department
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
query ItemById {
  itemById(itemId: "G001") {
    itemId
    itemName
    unitPrice
    unit
    stock
  }
}
```

## モック挙動

- 正常系
  - employeeId: E001, E002, E003
  - itemId: G001 から G006
  - keyword: カレー, 親子丼, たまねぎ など
- 未存在
  - employeeId: E998
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
