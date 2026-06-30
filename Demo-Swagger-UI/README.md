# Demo-Swagger-UI

Spring Boot 3.x 簡易 Demo 專案，以「使用者管理」為案例，實踐**規格先行 / 程式碼即文件**，並解決**前端傳入髒資料導致排障困難**的痛點。

---

## 技術棧

| 技術 | 用途 |
|------|------|
| Java 17+ | 語言 |
| Spring Boot 3.x | 框架 |
| Maven | 建置工具 |
| Spring Web | REST API |
| springdoc-openapi (Swagger UI) | 自動生成 OpenAPI 3 規格文件 |
| Jakarta Validation | 輸入資料驗證 |
| Docker / Docker Compose | 容器化部署 |

---

## 快速啟動

### 方式一：本機執行（需 JDK 17+ 與 Maven）

```bash
mvn spring-boot:run
```

### 方式二：Docker Compose（建議）

```bash
docker compose up --build
```

啟動後訪問 **http://localhost:8080/swagger-ui/index.html** 即可看到 OpenAPI / Swagger UI 規格頁面，所有 API 的欄位型態、格式、必填宣告一目瞭然。

---

## 專案目錄結構

```
Demo-Swagger-UI/
├── Dockerfile                  # 多階段建置：先 Maven 編譯，再 JRE 執行
├── docker-compose.yml          # 容器化編排
├── .dockerignore
├── pom.xml
└── src/main/
    ├── java/com/example/demoswaggerui/
    │   ├── DemoSwaggerUiApplication.java
    │   ├── controller/UserController.java
    │   ├── dto/UserCreateRequest.java
    │   ├── dto/UserResponse.java
    │   └── exception/GlobalExceptionHandler.java
    └── resources/application.yml
```

---

## 核心設計說明

### 1. 規格先行（Code as Documentation）

`UserCreateRequest` 使用 **Jakarta Validation** 註解（`@NotBlank`, `@Email`, `@Size`, `@Past`）定義輸入約束，同時使用 `@Schema` 註解描述欄位格式、範例值、長度限制。Swagger UI 會自動渲染這些資訊，前端可直接看到：

- `username`：必填，長度 2~50
- `email`：必填，必須符合 Email 格式
- `birthday`：非必填，但若提供必須是過去日期

```java
@NotBlank(message = "使用者名稱不可為空")
@Size(min = 2, max = 50)
private String username;

@NotBlank(message = "電子郵件不可為空")
@Email(message = "請提供正確的 Email 格式")
private String email;

@Past(message = "生日必須是過去的日期")
private LocalDate birthday;
```

### 2. 驗證失敗統一回應（排障優化）

`GlobalExceptionHandler`（`src/main/java/.../exception/GlobalExceptionHandler.java`）攔截所有 `MethodArgumentNotValidException`，回傳統一格式：

```json
{
  "timestamp": "2026-06-30T12:00:00",
  "status": 400,
  "error": "請求參數驗證失敗",
  "fieldErrors": {
    "email": "請提供正確的 Email 格式",
    "username": "使用者名稱長度需介於 2 到 50 之間"
  }
}
```

- **欄位級別錯誤回報**：`fieldErrors` 物件明確指出哪個欄位、什麼原因，前端可直接顯示，後端排障一目瞭然。
- **後端日誌輸出**：同時以 `log.warn` 印出完整錯誤明細，無需重現即可定位問題。

### 3. Controller 日誌記錄

```java
log.info("收到建立使用者請求 - username: {}, email: {}, birthday: {}",
        request.getUsername(), request.getEmail(), request.getBirthday());
```

進入每個 API 時以 `log.info` 印出請求參數，異常時以 `log.warn` 印出具體驗證失敗細節，讓排障時可直接從日誌定位問題。

---

## API 測試情境

| 方法 | 路徑 | 請求 / 情境 | 預期結果 |
|------|------|-------------|----------|
| POST | `/api/users` | `{"username":"", "email":"bad"}` | **400** + `fieldErrors` 指出 username 不可為空、email 格式錯誤 |
| POST | `/api/users` | `{"username":"a", "email":"bad"}` | **400** + `fieldErrors` 指出 username 長度不足、email 格式錯誤 |
| POST | `/api/users` | `{"username":"zhangsan", "email":"z@example.com"}` | **201** + 回傳 UserResponse JSON |
| GET | `/api/users/1` | 查詢已建立的使用者 | **200** + UserResponse |
| GET | `/api/users/999` | 查詢不存在的使用者 | **404** |doc
