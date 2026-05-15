INSERT INTO customers (id, name, last_name, email, created_date, last_modified_date, version)
VALUES
    (100, 'Alice', 'Johnson', 'alice@example.com', NOW(), NOW(), 0),
    (101, 'Bob', 'Williams', 'bob@example.com', NOW(), NOW(), 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO products (id, name, sku, price_currency, price_amount, created_date, last_modified_date, version)
VALUES
    (100, 'Office Chair', 'OFC-100', 'USD', 299.99, NOW(), NOW(), 0),
    (101, 'Desk Lamp', 'DSK-101', 'USD', 49.99, NOW(), NOW(), 0),
    (102, 'Notebook Set', 'NTB-102', 'USD', 12.99, NOW(), NOW(), 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO orders (id, customer_id, status, currency, total_amount, created_date, last_modified_date, version)
VALUES
    (100, 100, 'CREATED', 'USD', 299.99, NOW(), NOW(), 0),
    (101, 100, 'CONFIRMED', 'USD', 362.97, NOW(), NOW(), 0),
    (102, 101, 'CANCELLED', 'USD', 49.99, NOW(), NOW(), 0),
    (103, 101, 'CONFIRMED', 'USD', 12.99, NOW(), NOW(), 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO order_lines (id, order_id, product_id, quantity, price_amount, price_currency)
VALUES
    (100, 100, 100, 1, 299.99, 'USD'),
    (101, 101, 100, 1, 299.99, 'USD'),
    (102, 101, 101, 1, 49.99, 'USD'),
    (103, 101, 102, 1, 12.99, 'USD'),
    (104, 102, 101, 1, 49.99, 'USD'),
    (105, 103, 102, 1, 12.99, 'USD')
ON CONFLICT (id) DO NOTHING;

INSERT INTO invoices (id, customer_id, order_id, currency, amount, status, invoice_date, created_date, last_modified_date, version)
VALUES
    (100, 100, 101, 'USD', 362.97, 'ISSUED', NOW(), NOW(), NOW(), 0),
    (101, 101, 103, 'USD', 12.99, 'PAID', NOW(), NOW(), NOW(), 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO payments (id, invoice_id, order_id, customer_id, currency, amount, status, payment_date, created_date, last_modified_date, version)
VALUES
    (100, 101, 103, 101, 'USD', 12.99, 'COMPLETED', NOW(), NOW(), NOW(), 0)
ON CONFLICT (id) DO NOTHING;
