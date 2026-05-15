CREATE TABLE customers (
    id bigint primary key,
    name varchar(255) not null,
    last_name varchar(255) not null,
    email varchar(255) not null unique,
    created_date timestamp not null,
    last_modified_date timestamp not null,
    version bigint default 0
);

CREATE TABLE products (
    id bigint primary key,
    name varchar(255) not null,
    sku varchar(255) not null unique,
    price_currency varchar(3) not null,
    price_amount decimal(19,2) not null,
    created_date timestamp not null,
    last_modified_date timestamp not null,
    version bigint default 0
);
