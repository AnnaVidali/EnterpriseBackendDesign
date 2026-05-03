CREATE TABLE payments (
    id bigint primary key,
    invoice_id bigint not null,
    currency varchar(3) not null,
    amount decimal(19,2) not null,
    status varchar(255) not null,
    payment_date timestamp,
    constraint fk_payments_invoice foreign key (invoice_id) references invoices(id)
);

CREATE INDEX idx_payments_invoice_id ON payments(invoice_id);
