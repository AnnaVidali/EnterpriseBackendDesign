CREATE TABLE invoices (
    id int primary key,
    customer_id int not null,
    order_id int not null,
    currency varchar(3) not null,
    amount decimal(10,2) not null,
    status varchar(255) not null,
    invoice_date date,
    constraint fk_invoices_customer foreign key (customer_id) references customers(id),
    constraint fk_invoices_order foreign key (order_id) references orders(id)
);

CREATE INDEX idx_invoices_customer_id ON invoices(customer_id);
CREATE INDEX idx_invoices_order_id ON invoices(order_id);