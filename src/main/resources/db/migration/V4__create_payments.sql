CREATE TABLE payments (
    id int primary key,
    invoice_id int not null,
    amount decimal(10,2) not null,
    currency varchar(3) not null,
    status varchar(255) not null,
    payment_date timestamp not null,
    constraint fk_payments_invoice foreign key (invoice_id) references invoices(id)
);

CREATE INDEX idx_payments_invoice_id ON payments(invoice_id);
