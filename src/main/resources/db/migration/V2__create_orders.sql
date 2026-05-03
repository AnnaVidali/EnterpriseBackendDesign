CREATE TABLE orders (
    id bigint primary key,
    customer_id bigint not null,
    status varchar(255) not null,
    currency varchar(3) not null,
    total_amount decimal(19,2) not null,
    constraint fk_orders_customer foreign key (customer_id) references customers(id)
);

CREATE TABLE order_lines (
    id bigint primary key,
    order_id bigint not null,
    product_id bigint not null,
    quantity int not null,
    price_amount decimal(19,2) not null,
    price_currency varchar(3) not null,
    constraint fk_order_lines_order foreign key (order_id) references orders(id),
    constraint fk_order_lines_product foreign key (product_id) references products(id)
);

CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_order_lines_order_id ON order_lines(order_id);
CREATE INDEX idx_order_lines_product_id ON order_lines(product_id);
