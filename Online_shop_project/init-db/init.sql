CREATE DATABASE IF NOT EXISTS cafeShop;
USE cafeShop;


DROP TABLE IF EXISTS payment_items;
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS cards;
DROP TABLE IF EXISTS order_item_extras;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS product_allergens;
DROP TABLE IF EXISTS extra_allergens;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS extras;
DROP TABLE IF EXISTS allergens;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS global_notifications;
DROP TABLE IF EXISTS loyalty_accounts;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS loyalty_tiers;

-- ============================================================
-- USER SERVICE
-- ============================================================

CREATE TABLE loyalty_tiers (
    id               BIGINT        AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(50)   NOT NULL UNIQUE,
    discount_percent DECIMAL(5,2)  NOT NULL DEFAULT 0.00,
    min_points       INT           NOT NULL DEFAULT 0
);

INSERT INTO loyalty_tiers (name, discount_percent, min_points) VALUES
('BRONZE', 0.00,  0),
('SILVER', 5.00,  100),
('GOLD',   10.00, 500);

CREATE TABLE users (
    id       BIGINT       AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(100) NOT NULL,
    email    VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role     VARCHAR(20)  NOT NULL DEFAULT 'KUPAC'
);

-- Passwords are BCrypt hashes of '123' (strength 10)
INSERT INTO users (name, email, password, role) VALUES
('Ana Jovanovic',    'ana.jovanovic@example.com',    '$2b$10$CWxqHrDCIMLArvs3LUp1cOUlgAs1TMsOUU6185aEozQKhzeOAOxGS', 'KUPAC'),
('Marko Petrovic',   'marko.petrovic@example.com',   '$2b$10$CWxqHrDCIMLArvs3LUp1cOUlgAs1TMsOUU6185aEozQKhzeOAOxGS', 'KUPAC'),
('Jelena Nikolic',   'jelena.nikolic@example.com',   '$2b$10$CWxqHrDCIMLArvs3LUp1cOUlgAs1TMsOUU6185aEozQKhzeOAOxGS', 'KUPAC'),
('Admin Korisnik',   'admin@example.com',             '$2b$10$CWxqHrDCIMLArvs3LUp1cOUlgAs1TMsOUU6185aEozQKhzeOAOxGS', 'ADMIN');

CREATE TABLE loyalty_accounts (
    user_id          BIGINT  PRIMARY KEY,
    points           INT     NOT NULL DEFAULT 0,
    loyalty_tier_id  BIGINT  NOT NULL,
    CONSTRAINT fk_loyalty_user  FOREIGN KEY (user_id)         REFERENCES users(id)         ON DELETE CASCADE,
    CONSTRAINT fk_loyalty_tier  FOREIGN KEY (loyalty_tier_id) REFERENCES loyalty_tiers(id)
);

INSERT INTO loyalty_accounts (user_id, points, loyalty_tier_id)
SELECT id, 0, 1 FROM users WHERE role = 'KUPAC';

UPDATE loyalty_accounts SET points = 120, loyalty_tier_id = 2 WHERE user_id = (SELECT id FROM users WHERE email = 'marko.petrovic@example.com');
UPDATE loyalty_accounts SET points = 520, loyalty_tier_id = 3 WHERE user_id = (SELECT id FROM users WHERE email = 'jelena.nikolic@example.com');

CREATE TABLE notifications (
    id          BIGINT        AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT        NOT NULL,
    message     VARCHAR(500)  NOT NULL,
    type        VARCHAR(50)   NOT NULL,
    is_read     TINYINT(1)    NOT NULL DEFAULT 0,
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE global_notifications (
    id          BIGINT        AUTO_INCREMENT PRIMARY KEY,
    message     VARCHAR(500)  NOT NULL,
    type        VARCHAR(50)   NOT NULL,
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- CATALOG SERVICE
-- ============================================================

CREATE TABLE allergens (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500)
);

INSERT INTO allergens (name, description) VALUES
('Gluten',   'Sadrzi gluten (psenica, jecam, raz)'),
('Laktoza',  'Sadrzi laktozu (mleko i mlecni proizvodi)'),
('Jaja',     'Sadrzi jaja'),
('Orasi',    'Sadrzi orahe i ostale ljusture'),
('Soja',     'Sadrzi soju');

CREATE TABLE products (
    id          BIGINT         AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100)   NOT NULL,
    description VARCHAR(500),
    price       DECIMAL(10,2)  NOT NULL,
    size        VARCHAR(50),
    calories    INT
);

INSERT INTO products (name, description, price, size, calories) VALUES
('Espresso',       'Jak crni espresso',                    180.00, 'S', 5),
('Cappuccino',     'Espresso sa mlecnom penom',            250.00, 'M', 120),
('Latte',          'Espresso sa dosta mleka',              280.00, 'L', 150),
('Croissant',      'Masleni kroasan',                      150.00, NULL, 280),
('Cokoladna torta','Domaca cokoladna torta',               300.00, NULL, 450);

CREATE TABLE extras (
    id          BIGINT         AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100)   NOT NULL,
    description VARCHAR(500),
    price       DECIMAL(10,2)  NOT NULL,
    calories    INT
);

INSERT INTO extras (name, description, price, calories) VALUES
('Sirupe od vanile',  'Dodatak sirup od vanile',    50.00, 30),
('Bademovo mleko',    'Zamena za kravlje mleko',    70.00, 15),
('Slag krem',         'Slag krem na vrh',           50.00, 50),
('Ekstra espresso',   'Dodatni shot espress',       80.00, 5);

CREATE TABLE product_allergens (
    product_id  BIGINT NOT NULL,
    allergen_id BIGINT NOT NULL,
    PRIMARY KEY (product_id, allergen_id),
    CONSTRAINT fk_pa_product  FOREIGN KEY (product_id)  REFERENCES products(id)  ON DELETE CASCADE,
    CONSTRAINT fk_pa_allergen  FOREIGN KEY (allergen_id) REFERENCES allergens(id) ON DELETE CASCADE
);

CREATE TABLE extra_allergens (
    extra_id    BIGINT NOT NULL,
    allergen_id BIGINT NOT NULL,
    PRIMARY KEY (extra_id, allergen_id),
    CONSTRAINT fk_ea_extra    FOREIGN KEY (extra_id)    REFERENCES extras(id)    ON DELETE CASCADE,
    CONSTRAINT fk_ea_allergen  FOREIGN KEY (allergen_id) REFERENCES allergens(id) ON DELETE CASCADE
);

INSERT INTO product_allergens (product_id, allergen_id)
SELECT p.id, a.id FROM products p, allergens a
WHERE (p.name = 'Cappuccino'       AND a.name = 'Laktoza')
   OR (p.name = 'Latte'            AND a.name = 'Laktoza')
   OR (p.name = 'Croissant'        AND a.name IN ('Gluten','Jaja','Laktoza'))
   OR (p.name = 'Cokoladna torta'  AND a.name IN ('Gluten','Jaja','Laktoza'));

INSERT INTO extra_allergens (extra_id, allergen_id)
SELECT e.id, a.id FROM extras e, allergens a
WHERE (e.name = 'Bademovo mleko' AND a.name = 'Orasi')
   OR (e.name = 'Slag krem'      AND a.name = 'Laktoza');

-- ============================================================
-- ORDER SERVICE
-- ============================================================

CREATE TABLE orders (
    id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    status     VARCHAR(20)  NOT NULL DEFAULT 'PAID',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_items (
    id           BIGINT         AUTO_INCREMENT PRIMARY KEY,
    order_id     BIGINT         NOT NULL,
    product_id   BIGINT         NOT NULL,
    product_name VARCHAR(255)   NOT NULL,
    price        DECIMAL(10,2)  NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

CREATE TABLE order_item_extras (
    id            BIGINT         AUTO_INCREMENT PRIMARY KEY,
    order_item_id BIGINT         NOT NULL,
    extra_id      BIGINT         NOT NULL,
    extra_name    VARCHAR(255)   NOT NULL,
    price         DECIMAL(10,2)  NOT NULL,
    CONSTRAINT fk_extras_item FOREIGN KEY (order_item_id) REFERENCES order_items(id) ON DELETE CASCADE
);

INSERT INTO orders (user_id, status) VALUES (1, 'PAID');
INSERT INTO order_items (order_id, product_id, product_name, price) VALUES (1, 1, 'Espresso', 200.00);
INSERT INTO order_item_extras (order_item_id, extra_id, extra_name, price) VALUES (1, 1, 'Sirupe od vanile', 50.00);
INSERT INTO order_items (order_id, product_id, product_name, price) VALUES (1, 4, 'Croissant', 150.00);

INSERT INTO orders (user_id, status) VALUES (1, 'PAID');
INSERT INTO order_items (order_id, product_id, product_name, price) VALUES (2, 2, 'Cappuccino', 280.00);

INSERT INTO orders (user_id, status) VALUES (2, 'PAID');
INSERT INTO order_items (order_id, product_id, product_name, price) VALUES (3, 3, 'Latte', 280.00);

-- ============================================================
-- PAYMENT SERVICE
-- ============================================================

CREATE TABLE cards (
    id               BIGINT        AUTO_INCREMENT PRIMARY KEY,
    user_id          BIGINT        NOT NULL,
    card_holder      VARCHAR(255)  NOT NULL,
    last_four_digits VARCHAR(4)    NOT NULL,
    expiry_month     INT           NOT NULL,
    expiry_year      INT           NOT NULL
);

CREATE TABLE transactions (
    id         BIGINT         AUTO_INCREMENT PRIMARY KEY,
    order_id   BIGINT         NOT NULL,
    user_id    BIGINT         NOT NULL,
    card_id    BIGINT         NULL,
    amount     DECIMAL(10,2)  NOT NULL,
    paid_at    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transaction_card FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE SET NULL
);

CREATE TABLE payment_items (
    id             BIGINT         AUTO_INCREMENT PRIMARY KEY,
    transaction_id BIGINT         NOT NULL,
    order_item_id  BIGINT         NOT NULL,
    description    VARCHAR(500)   NOT NULL,
    amount         DECIMAL(10,2)  NOT NULL,
    CONSTRAINT fk_payment_item_transaction FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE CASCADE
);

INSERT INTO cards (user_id, card_holder, last_four_digits, expiry_month, expiry_year) VALUES
(1, 'Ana Jovanovic',   '1234', 12, 2027),
(1, 'Ana Jovanovic',   '5678', 6,  2026),
(2, 'Marko Petrovic',  '9012', 3,  2028),
(3, 'Jelena Nikolic',  '3456', 9,  2025);

INSERT INTO transactions (order_id, user_id, card_id, amount, paid_at) VALUES
(1, 1, 1, 400.00, NOW() - INTERVAL 5 DAY),
(2, 1, 1, 280.00, NOW() - INTERVAL 3 DAY),
(3, 2, 3, 280.00, NOW() - INTERVAL 1 DAY);

INSERT INTO payment_items (transaction_id, order_item_id, description, amount) VALUES
(1, 1, 'Espresso',          200.00),
(1, 2, 'Sirupe od vanile',   50.00),
(1, 3, 'Croissant',         150.00),
(2, 4, 'Cappuccino',        280.00),
(3, 5, 'Latte',             280.00);
