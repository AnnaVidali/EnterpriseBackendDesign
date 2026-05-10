CREATE TABLE customers (
    id int auto_increment primary key,
    name varchar(255) not null,
    last_name varchar(255) not null,
    email varchar(255) not null unique,
    created_date datetime not null,
    last_modified_date datetime not null,
    version bigint default 0
);

CREATE TABLE products (
    id int primary key,
    name varchar(255) not null,
    sku varchar(255) not null unique,
    price_currency varchar(3) not null,
    price_amount decimal(10,2) not null,
    created_date datetime not null,
    last_modified_date datetime not null,
    version bigint default 0
);
