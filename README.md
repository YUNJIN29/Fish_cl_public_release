# Aquatic Disease Knowledge Platform

这是一个用于学习、科研训练和课程/竞赛原型搭建的水产病害智能诊断与知识图谱 Web 项目。项目聚合病害知识库、AI 辅助诊断、RAG 知识检索、知识图谱、科普内容、社区交流和用品展示等模块，用于演示从“症状记录”到“资料检索、辅助判断、知识关联展示”的完整流程。

> 本公开版本已移除个人域名、服务器地址、备案号、邮箱、部署脚本、私有交接文档、构建产物和运行数据。请在自己的环境中配置数据库、密钥和部署信息。

## 功能概览

- 门户首页：项目入口与模块导航。
- 病害知识库：查询常见水产及观赏鱼病害，查看症状、原因、防治建议等信息。
- AI 智能诊断：支持症状描述与图片上传，结合鱼种、水温、水质等信息生成辅助诊断结果。
- RAG 知识检索：基于本地资料片段进行问答召回，为诊断和学习提供可追溯资料基础。
- 知识图谱：展示病害、症状、病原、药物、环境因素之间的关联，图谱分类、节点和关系存储在数据库中。
- 科普学习与交流社区：整理科普文章、帖子、评论、收藏和点赞等内容。
- 用品展示：展示水质调节、鱼药治理、器材、饲料等用品信息。
- 个人中心：包含个人资料、诊断记录、订单记录、打印入口和小游戏模块。
- 管理后台：管理商品、分类、订单、内容审核、用户和基础统计。

## 技术栈

| 模块 | 技术 |
| --- | --- |
| 后端 | Java 17, Spring Boot 3.3, Spring Security, Spring Data JPA |
| 数据库 | MySQL 8.0+ |
| 前端 | Vue 3, Vite, TypeScript, Element Plus, Pinia |
| 可视化 | ECharts |
| 接口文档 | SpringDoc OpenAPI / Swagger UI |

## 目录结构

```text
.
├─ backend/                 # Spring Boot 后端
│  ├─ src/main/java/         # 业务代码
│  ├─ src/main/resources/    # 配置、SQL
│  └─ pom.xml
├─ frontend/                # Vue 3 前端
│  ├─ src/views/            # 页面
│  ├─ src/router/           # 前端路由
│  ├─ src/stores/           # Pinia 状态
│  └─ package.json
├─ RAG_DATASETS.md          # RAG 数据源建议
└─ README.md
```

## 本地运行

### 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 16+，推荐 Node.js 20+
- MySQL 8.0+

### 后端

1. 创建数据库：

```sql
CREATE DATABASE fishmart DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 配置环境变量：

```powershell
$env:MYSQL_URL="jdbc:mysql://localhost:3306/fishmart?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true"
$env:MYSQL_USER="fishmart_user"
$env:MYSQL_PASSWORD="your_mysql_password"
$env:JWT_SECRET="replace_with_a_long_random_secret"
$env:ADMIN_USERNAME="admin"
$env:ADMIN_PASSWORD="replace_with_a_strong_admin_password"
```

如果要启用 AI 诊断接口，请额外配置兼容 OpenAI Chat Completions 格式的服务：

```powershell
$env:AI_BASE_URL="https://your-ai-provider.example.com"
$env:AI_API_KEY="your_api_key"
$env:AI_MODEL="your_model_name"
```

3. 启动后端：

```powershell
cd backend
mvn spring-boot:run
```

后端默认地址：`http://localhost:8080`

Swagger：`http://localhost:8080/swagger-ui.html`

### 前端

```powershell
cd frontend
npm install
npm run dev
```

前端默认地址：`http://localhost:5173`

## 构建

```powershell
cd backend
mvn -DskipTests package

cd ../frontend
npm install
npm run build
```

## 数据说明

- 基础演示数据由后端初始化器写入数据库。
- 知识图谱数据存储在 `knowledge_graph_categories`、`knowledge_graph_nodes`、`knowledge_graph_links` 三张表中，服务启动时会根据 `diseases` 表同步生成。
- 上传文件目录、打印文件目录和诊断图片目录由 `application.yml` 中的配置项控制。

## 安全提示

- 不要提交真实数据库密码、JWT 密钥、AI API Key、服务器 IP、域名备案信息或个人联系方式。
- 公开部署前请修改默认管理员账号和密码。
- AI 诊断结果仅适合作为学习和辅助分析参考，不能替代现场检测或专业诊断。
