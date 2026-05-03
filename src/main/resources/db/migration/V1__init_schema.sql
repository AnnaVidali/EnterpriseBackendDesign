CREATE TABLE customers (
    id int primary key,
    name varchar(255) not null,
    email varchar(255) not null unique
);

CREATE TABLE products (
    id int primary key,
    name varchar(255) not null,
    price_currency varchar(3) not null,
    price_amount decimal(10,2) not null
);
