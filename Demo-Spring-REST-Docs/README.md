# Demo-Spring-REST-Docs

Spring Boot 3.x 簡易 Demo 專案，以「使用者管理」為案例，使用 **Spring REST Docs** 實踐**測試即文件**（Test-as-Documentation），在測試階段自動產生 API 文件。

與 `Demo-Swagger-UI`（springdoc-openapi / Swagger UI）不同，Spring REST Docs 的文件源自**測試案例**，保證文件永遠與程式碼行為一致。

---

## 技術棧

| 技術 | 用途 |
|------|------|
| Java 17+ | 語言 |
| Spring Boot 3.x | 框架 |
| Maven | 建置工具 |
| Spring Web | REST API |
| Jakarta Validation | 輸入資料驗證 |
| Spring REST Docs | 在測試中產生 API 文件片段（snippets） |
| Asciidoctor Maven Plugin | 將 snippets 編譯成靜態 HTML |
| Docker / Docker Compose | 容器化部署 |

---

## 與 Swagger UI 版本的關鍵差異

| 層面 | springdoc-openapi (Demo-Swagger-UI) | Spring REST Docs (本專案) |
|------|--------------------------------------|--------------------------|
| 文件產生時機 | **運行期** — 啟動後即時解析 `@Schema` 等註解 | **測試期** — `mvn package` 時跑測試產生 snippets |
| DTO 額外標註 | 需加 `@Schema`、`@Operation` | 只需 Jakarta Validation，無框架侵入 |
| 正確性保證 | 註解可能與實作脫節 | 文件**源自測試**，測試通過才產出，保證一致 |
| 互動性 | Swagger UI 可 Try it out | 純靜態 HTML |

---

## 目錄結構

```
Demo-Spring-REST-Docs/
├── Dockerfile
├── docker-compose.yml
├── .dockerignore
├── pom.xml
├── README.md
└── src/
    ├── docs/asciidocs/
    │   └── user-api.adoc          # AsciiDoc 主文件，嵌入 snippets
    ├── main/
    │   ├── java/com/example/demorestdocs/
    │   │   ├── DemoRestDocsApplication.java
    │   │   ├── controller/UserController.java
    │   │   ├── dto/UserCreateRequest.java
    │   │   ├── dto/UserResponse.java
    │   │   └── exception/GlobalExceptionHandler.java
    │   └── resources/application.yml
    └── test/java/com/example/demorestdocs/
        └── controller/
            └── UserControllerDocumentationTest.java
```

---

## 快速啟動

### 方式一：本機執行（需 JDK 17+ 與 Maven）

```bash
mvn clean verify
```

- 測試執行 → 產生 snippets 至 `target/generated-snippets/`
- Asciidoctor 編譯 → 輸出 `target/generated-docs/user-api.html`

啟動 API 伺服器：

```bash
mvn spring-boot:run
```

### 方式二：Docker Compose（建議）

```bash
docker compose up --build
```

啟動後：

| 用途 | 網址 |
|------|------|
| REST API | http://localhost:8080/api/users |
| API 文件 | http://localhost:8080/docs/user-api.html |

文件可在 **http://localhost:8080/docs/user-api.html** 查看，由 Spring Boot 直接提供靜態資源服務，不需要額外容器。

---

## 文件產出流程

```
mvn package
  │
  ├── 1. 執行測試 (surefire)
  │     └── UserControllerDocumentationTest.java
  │           ├── createUser()              → snippets/user/create/
  │           ├── createUserWithValidationErrors() → snippets/user/create-validation-error/
  │           ├── getUser()                 → snippets/user/get/
  │           └── getUserNotFound()         → snippets/user/get-not-found/
  │
  └── 2. Asciidoctor 編譯 (prepare-package)
        └── user-api.adoc
              └── include::{snippets}/user/.../ → target/generated-docs/user-api.html
```

---

## 測試案例說明

| 測試方法 | 模擬情境 | 產生的 snippets |
|----------|----------|----------------|
| `createUser` | 傳入正確資料建立使用者 | `http-request.adoc`, `http-response.adoc`, `request-fields.adoc`, `response-fields.adoc`, `curl-request.adoc` |
| `createUserWithValidationErrors` | 傳入空 username、錯誤 email、未來生日 | 同上 + `response-fields.adoc` 顯示驗證錯誤結構 |
| `getUser` | 查詢已建立的使用者 | `http-request.adoc`, `http-response.adoc`, `path-parameters.adoc`, `response-fields.adoc` |
| `getUserNotFound` | 查詢不存在的使用者 ID | `http-response.adoc`（404） |

---

## API 測試情境

| 方法 | 路徑 | 請求 | 預期結果 |
|------|------|------|----------|
| POST | `/api/users` | `{"username":"", "email":"bad", "birthday":"2099-12-31"}` | **400** + `fieldErrors` 指出各欄位錯誤 |
| POST | `/api/users` | `{"username":"zhangsan","email":"z@example.com","birthday":"1990-01-01"}` | **201** + 回傳 UserResponse |
| GET | `/api/users/{id}` | 查詢已建立的使用者 | **200** + UserResponse |
| GET | `/api/users/{id}` | 查詢不存在的使用者 | **404** |
