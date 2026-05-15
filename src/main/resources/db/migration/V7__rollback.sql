-- Rollback V6: Remove seed data
DELETE FROM payments WHERE id IN (100);
DELETE FROM invoices WHERE id IN (100, 101);
DELETE FROM order_lines WHERE id IN (100, 101, 102, 103, 104, 105);
DELETE FROM orders WHERE id IN (100, 101, 102, 103);
DELETE FROM products WHERE id IN (100, 101, 102);
DELETE FROM customers WHERE id IN (100, 101);

-- Rollback V5: Remove audit and version columns
ALTER TABLE payments DROP COLUMN IF EXISTS version;
ALTER TABLE payments DROP COLUMN IF EXISTS created_date;
ALTER TABLE payments DROP COLUMN IF EXISTS last_modified_date;

ALTER TABLE invoices DROP COLUMN IF EXISTS version;
ALTER TABLE invoices DROP COLUMN IF EXISTS created_date;
ALTER TABLE invoices DROP COLUMN IF EXISTS last_modified_date;

ALTER TABLE orders DROP COLUMN IF EXISTS version;
ALTER TABLE orders DROP COLUMN IF EXISTS created_date;
ALTER TABLE orders DROP COLUMN IF EXISTS last_modified_date;

-- Rollback V4: Drop payments table
DROP TABLE IF EXISTS payments CASCADE;

-- Rollback V3: Drop invoices table
DROP TABLE IF EXISTS invoices CASCADE;

-- Rollback V2: Drop orders and order_lines tables
DROP TABLE IF EXISTS order_lines CASCADE;
DROP TABLE IF EXISTS orders CASCADE;

-- Rollback V1: Drop customers and products tables
DROP TABLE IF EXISTS products CASCADE;
DROP TABLE IF EXISTS customers CASCADE;
