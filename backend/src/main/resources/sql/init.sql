-- FishMart 数据库初始化脚本（可直接在 MySQL 执行）
-- 建库：CREATE DATABASE fishmart DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
-- 使用：USE fishmart;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS favorites;
DROP TABLE IF EXISTS likes;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS posts;
DROP TABLE IF EXISTS article_categories;
DROP TABLE IF EXISTS articles;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS product_images;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  nickname VARCHAR(64) NOT NULL,
  avatar_url VARCHAR(512),
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE roles (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(32) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_roles (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE categories (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE products (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  category_id BIGINT NOT NULL,
  name VARCHAR(128) NOT NULL,
  description TEXT,
  price_cents INT NOT NULL,
  stock INT NOT NULL DEFAULT 0,
  cover_image_url VARCHAR(512),
  status VARCHAR(16) NOT NULL DEFAULT 'ON',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE product_images (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  image_url VARCHAR(512) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  CONSTRAINT fk_product_images_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE orders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no VARCHAR(64) NOT NULL UNIQUE,
  user_id BIGINT NOT NULL,
  total_cents INT NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE order_items (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  unit_price_cents INT NOT NULL,
  CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id),
  CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE articles (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(200) NOT NULL,
  cover_image_url VARCHAR(512),
  content TEXT NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'PUBLISHED',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE article_categories (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE posts (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  title VARCHAR(200) NOT NULL,
  content TEXT NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'APPROVED',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_posts_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE comments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  target_type VARCHAR(16) NOT NULL,
  target_id BIGINT NOT NULL,
  content VARCHAR(1000) NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'APPROVED',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users(id),
  INDEX idx_comments_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE likes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  target_type VARCHAR(16) NOT NULL,
  target_id BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_likes_user FOREIGN KEY (user_id) REFERENCES users(id),
  UNIQUE KEY uk_likes (user_id, target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE favorites (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  target_type VARCHAR(16) NOT NULL,
  target_id BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_favorites_user FOREIGN KEY (user_id) REFERENCES users(id),
  UNIQUE KEY uk_favorites (user_id, target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===== 种子数据（演示用）=====
INSERT INTO roles(name) VALUES ('USER'), ('ADMIN');

-- 管理员账号由后端启动器按 ADMIN_USERNAME / ADMIN_PASSWORD 环境变量创建；这里不写死账号口令

INSERT INTO categories(name, sort_order) VALUES
('孔雀鱼', 10),
('锦鲤', 9),
('神仙鱼', 8),
('水草/造景', 1);

INSERT INTO products(category_id, name, description, price_cents, stock, cover_image_url, status) VALUES
(1, '蓝草尾孔雀鱼（公）', '色彩鲜艳，适合新手饲养。', 1990, 50, NULL, 'ON'),
(2, '红白锦鲤（15cm）', '经典红白，适合池塘/大缸。', 8990, 20, NULL, 'ON'),
(3, '埃及神仙鱼（对）', '观赏性强，注意水质稳定。', 12900, 10, NULL, 'ON');

INSERT INTO articles(title, cover_image_url, content, status) VALUES
('新手入门：观赏鱼开缸三件事', NULL, '1) 先养水 2) 控制密度 3) 规律换水...', 'PUBLISHED'),
('孔雀鱼常见病与预防', NULL, '白点、烂尾、肠炎的识别与处理...', 'PUBLISHED');

-- posts/comments/likes/favorites 在实现业务后由接口产生，这里先留空

SET FOREIGN_KEY_CHECKS = 1;

