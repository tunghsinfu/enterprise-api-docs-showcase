# 現代後端 API 自動化文件部署與排障實踐

## 核心思想

**淘汰手寫靜態文件（Word、Wiki），讓程式碼或測試成為唯一的真實來源（Single Source of Truth），透過自動化手段徹底解決文件與程式碼不同步的問題。**

---

## 系統架構

```
┌─────────────────────────────────────────────────────────────────────┐
│                       使用者 / 瀏覽器                                │
└──────────────────────────┬──────────────────────────────────────────┘
                           │ :80
                           ▼
┌──────────────────────────────────────────────────────────────────────┐
│                    Nginx API Gateway (demo-gateway)                   │
│  ┌─────────┐  ┌──────────┐  ┌───────────┐  ┌──────────────────┐    │
│  │ /api/   │  │ /swagger │  │  /docs/   │  │   /jenkins/      │    │
│  │ /v3/... │  │ -ui/     │  │           │  │                  │    │
│  └────┬────┘  └────┬─────┘  └─────┬─────┘  └────────┬─────────┘    │
└───────┼────────────┼──────────────┼──────────────────┼──────────────┘
        │            │              │                  │
        ▼            ▼              ▼                  ▼
┌───────────┐ ┌───────────┐ ┌──────────┐ ┌────────────────┐
│ Swagger   │ │  Swagger  │ │REST Docs │ │   Jenkins      │
│ API       │ │  UI       │ │ 靜態文件  │ │  CI/CD Server  │
│ :8080     │ │  (同上)   │ │ Nginx :80│ │  :8080          │
└───────────┘ └───────────┘ └──────────┘ └────────────────┘
│             │              │
│             │              └── 測試驅動文件 (Test-Driven)
│             │                  建構時自動產生，與程式碼 100% 一致
│             │
│             └────────────── 交互式沙盒 (Annotation-Driven)
│                             可直接發送請求測試 API
│
└─────────────────────────── 主要 API 路由
                            提供 User CRUD 功能
```

---

## 服務一覽

| 服務 | 容器名稱 | 技術 | 說明 |
|------|---------|------|------|
| `swagger-api` | `demo-swagger-api` | Spring Boot 3.x + springdoc-openapi | OpenAPI 3 規格服務，提供 `/api/users` API 及 `/v3/api-docs` JSON 規格 |
| `restdocs-api` | `demo-restdocs-api` | Spring Boot 3.x + Spring REST Docs | 同一套 API 的 REST Docs 版本，Controller 無框架註解侵入 |
| `restdocs-docs` | `demo-restdocs-docs` | Nginx (靜態文件伺服器) | 建構時自動測試並產生 REST Docs HTML 文件，獨立容器提供服務 |
| `gateway` | `demo-gateway` | Nginx (統一入口) | 路由轉發、統一路徑管理 |
| `jenkins` | `demo-jenkins` | Jenkins LTS + JCasC | 自動化 CI/CD，觸發建置、測試、文件部署 |

---

## 快速啟動

### 環境需求

- Docker Engine 24+
- Docker Compose v2

### 啟動指令

```bash
cd demo-enterprise
docker compose up --build
```

首次建置需下載 Maven 相依套件與 Jenkins 基礎映像，約 5–10 分鐘。

### 訪問入口

| 入口 | 網址 | 說明 |
|------|------|------|
| **首頁導覽** | http://localhost | 各服務入口總覽 |
| **Swagger UI** | http://localhost/swagger-ui/index.html | 互動式 API 沙盒（可 Try it out） |
| **OpenAPI 規格** | http://localhost/v3/api-docs | OpenAPI 3 JSON 原始規格 |
| **REST Docs 文件** | http://localhost/docs/user-api.html | 靜態 API 文件（源自測試） |
| **API 實例** | http://localhost/api/users | 實際 User CRUD 端點 |
| **Jenkins** | http://localhost/jenkins/ | CI/CD 主控臺（帳號 `admin` / 密碼 `admin`） |

---

## 企業級實踐原則

### 原則一：文件資產與業務邏輯分離

在生產環境中，**Spring Boot 服務不應直接承載靜態文件流量**。原因：

1. **安全性** — 減少攻擊面
2. **效能** — 避免文件請求消耗應用伺服器執行緒
3. **解耦** — 文件可獨立部署、擴展、CDN 加速

本架構的實踐方式：

| 專案 | 本地開發 | 企業部署 |
|------|---------|---------|
| Swagger UI | Spring Boot 直接提供 Swagger UI（`springdoc-openapi` 自動） | CI/CD 將 `openapi.json` 導出至獨立 Swagger UI 容器 |
| REST Docs | Spring Boot 透過 `WebMvcConfigurer` 提供 `/docs/` | 建構期產出 HTML → 獨立 Nginx 容器提供靜態服務 |

### 原則二：CI/CD 自動化文件產出

Jenkins 預設配置 Pipeline `demo-pipeline`，包含以下階段：

```
Checkout → Build Swagger-UI → Build REST-Docs + Generate Docs → Verify Validation
```

Pipeline 每 30 分鐘自動觸發（可調整），亦可手動執行。

### 原則三：統一的輸入驗證與錯誤回應

兩個 Spring Boot 專案皆實作：

1. **DTO 層** — Jakarta Validation 註解（`@NotBlank`, `@Email`, `@Size`, `@Past`）
2. **Global Exception Handler** — `@RestControllerAdvice` 攔截驗證失敗，回傳統一 JSON
3. **SLF4J 日誌** — Controller 入口記錄請求參數，排障時比對日誌與文件即可定位問題

---

## 技術方案對決：OpenAPI 3 vs Spring REST Docs

| 比較維度 | OpenAPI 3 (Springdoc) | Spring REST Docs |
| --- | --- | --- |
| **核心模式** | **代碼即文件 (Annotation-Driven)** | **測試驅動文件 (Test-Driven)** |
| **生產機制** | Controller/DTO 加上規格註解，運行時動態掃描生成 | 撰寫 MockMvc 測試，測試通過後攔截真實請求/回應生成 |
| **程式碼污染** | 較高（Controller 層塞入非業務註解） | **無（Controller 保持絕對乾淨）** |
| **準確度保障** | 中高（修改程式但忘記更新註解，可能產生偏差） | **極高（100% 準確，測試失敗則無法生成文件）** |
| **前端體驗** | **動態互動式（Swagger UI）**，提供沙盒環境可直接發送請求 | **靜態閱讀式**，生成 HTML 說明網頁 |
| **對排障幫助** | **極高**。直接在 Swagger UI 模擬髒資料輸入 | **高**。確保規格絕對正確，減少資訊不對稱 |

---

## 目錄結構

```
demo-enterprise/
├── docker-compose.yml          # 主編排檔，整合所有服務
├── .env                        # 環境變數
├── README.md
│
├── gateway/                    # 統一 API Gateway
│   ├── Dockerfile
│   ├── nginx.conf              # 路由規則配置
│   └── landing/
│       └── index.html          # 首頁導覽
│
├── docs/                       # REST Docs 靜態文件伺服器
│   └── Dockerfile.docs         # 建構時執行測試、產出 HTML
│
├── jenkins/                    # CI/CD 自動化
│   ├── Dockerfile
│   ├── plugins.txt             # Jenkins 外掛清單
│   ├── casc.yaml               # Configuration as Code
│   └── jobs/
│       └── demo-pipeline/
│           └── config.xml      # 預設 Pipeline 工作
│
├── Demo-Swagger-UI/            # Spring Boot + springdoc-openapi（個別啟動 port 38080）
│   └── docker-compose.yml      # 獨立 docker-compose 供本機開發
│
└── Demo-Spring-REST-Docs/      # Spring Boot + Spring REST Docs（個別啟動 port 8080）
    └── docker-compose.yml      # 獨立 docker-compose 供本機開發
```

---

## 排障驗證

啟動後，可透過以下情境驗證防禦機制：

### 情境：傳入髒資料

```bash
curl -X POST http://localhost/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"","email":"invalid","birthday":"2099-12-31"}'
```

**預期回應（400）：**
```json
{
  "timestamp": "2026-06-30T12:00:00",
  "status": 400,
  "error": "請求參數驗證失敗",
  "fieldErrors": {
    "username": "使用者名稱不可為空",
    "email": "請提供正確的 Email 格式",
    "birthday": "生日必須是過去的日期"
  }
}
```

### 情境：比對文件與異常日誌

1. 在 Swagger UI（http://localhost/swagger-ui/index.html）查看 API 規格
2. 或閱讀 REST Docs 文件（http://localhost/docs/user-api.html）
3. 檢視 Spring Boot 容器日誌中的輸入參數記錄
4. 比對實際請求、錯誤回應與規格文件，判定是「前端傳錯欄位」還是「後端資料異常」

---

## 子專案本地開發

各子專案保留獨立的 `docker-compose.yml`，供開發階段快速驗證：

```bash
# Swagger UI 版單獨啟動
cd demo-enterprise/Demo-Swagger-UI
docker compose up --build
# 訪問 http://localhost:38080/swagger-ui/index.html

# REST Docs 版單獨啟動
cd demo-enterprise/Demo-Spring-REST-Docs
docker compose up --build
# 訪問 http://localhost:8080/swagger-ui/index.html
```

---

## 停止與清理

```bash
# 停止所有服務
docker compose down

# 停止並清除資料（含 Jenkins 工作區與建置歷史）
docker compose down -v
```
