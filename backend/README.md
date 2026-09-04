# Backend

Spring Boot 后端服务，提供认证、病害知识库、AI 辅助诊断、RAG 检索、知识图谱、内容、商品、订单、上传和访问统计等接口。

## 启动

```powershell
$env:MYSQL_URL="jdbc:mysql://localhost:3306/fishmart?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true"
$env:MYSQL_USER="root"
$env:MYSQL_PASSWORD="your_mysql_password"
$env:JWT_SECRET="replace_with_a_long_random_secret"
$env:ADMIN_USERNAME="admin"
$env:ADMIN_PASSWORD="replace_with_a_strong_admin_password"

mvn spring-boot:run
```

Swagger：`http://localhost:8080/swagger-ui.html`

## 说明

- 数据库表由 Spring Data JPA 自动维护。
- 演示数据由各初始化器写入。
- 知识图谱数据存储在数据库表中，不依赖静态 JSON 文件。
- 不要把真实密钥、账号密码或生产环境配置提交到仓库。
