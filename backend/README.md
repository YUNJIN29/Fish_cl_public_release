# 水产病害智能诊断与知识图谱平台后端

## 项目简介

这是公开脱敏版本的 Spring Boot 后端，面向水产病害知识库、AI 辅助诊断、RAG 检索、知识图谱、社区内容、养殖待办和水质日志等模块。

它主要解决用户在养鱼过程中遇到的“不会判断病害、查不到可靠信息、不知道怎么处理、相关用品购买不方便”等问题。项目核心是把 **AI智能诊断、RAG知识检索、病害知识库、知识图谱和水产健康用品服务** 结合起来，形成从识病到处理的完整闭环。

### 核心功能

1. **AI智能诊断**：支持文字描述与图片上传，结合水质环境信息生成专业诊断报告。
2. **病害知识库**：涵盖常见水产与观赏鱼病害，提供致病原因、症状、治疗与预防措施。
3. **知识图谱可视化**：直观展示病害、症状、药物、环境因素之间的关联。
4. **用品展示**：基于诊断结果展示相关用品信息。
5. **科普社区与记录**：科学养殖案例复盘，用户历史诊断记录追溯。

## 运行环境

- JDK 17
- MySQL 8.0+
- Node.js 16+

## 后端启动（Spring Boot）

1. 创建数据库 `fishmart`（或按需修改 application.yml 中的库名）
2. 通过环境变量配置数据库、JWT 和管理员密码：
```bash
MYSQL_USER=fishmart_user
MYSQL_PASSWORD=your_mysql_password
JWT_SECRET=replace_with_a_long_random_secret
ADMIN_USERNAME=admin
ADMIN_PASSWORD=replace_with_a_strong_admin_password
```
3. 启动项目：
```bash
cd backend
mvn spring-boot:run
```
- Swagger 接口文档：`http://localhost:8080/swagger-ui.html`
- 管理员账号只会在设置 `ADMIN_PASSWORD` 后自动创建。

## 前端启动（Vue3 + Vite）

```bash
cd frontend
npm install
npm run dev
```
访问地址：`http://localhost:5173`


