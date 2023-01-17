INSERT INTO company(vat_num, name, address) VALUES ('IT895623147', 'JDigital', '740 Madison Ave, New York, NY 10065, USA');
INSERT INTO company(vat_num, name, address) VALUES ('IT943761235', 'Danbo', 'Rue Jeanne D’Arc 63/F, Paris, France');
INSERT INTO company(vat_num, name, address) VALUES ('IT315284678', 'MemSystems S.p.a.', 'Largo Colombo 56/C, Milano, Italy');
INSERT INTO company(vat_num, name, address) VALUES ('IT534697215', 'PlaStic', 'Viale Europa 103/A, Torino, Italy');
INSERT INTO company(vat_num, name, address) VALUES ('IT788945231', 'Comma', 'Corso Milano 117/B, Padova, Italy');

INSERT INTO shipping_lot(shipping_code, product_quantity, shipping_date, customer_vat_num) VALUES ('0258463971', 100, '2023-01-03', 'IT895623147');
INSERT INTO shipping_lot(shipping_code, product_quantity, shipping_date, customer_vat_num) VALUES ('1346792468', 200, '2022-02-16', 'IT943761235');
INSERT INTO shipping_lot(shipping_code, product_quantity, shipping_date, customer_vat_num) VALUES ('1472583690', 60, '2022-10-24', 'IT315284678');
INSERT INTO shipping_lot(shipping_code, product_quantity, shipping_date, customer_vat_num) VALUES ('7619438246', 15, '2022-12-17', 'IT534697215');
INSERT INTO shipping_lot(shipping_code, product_quantity, shipping_date, customer_vat_num) VALUES ('7946138520', 30, '2022-04-07', 'IT788945231');
INSERT INTO shipping_lot(shipping_code, product_quantity, shipping_date, customer_vat_num) VALUES ('8462597310', 20, '2023-01-12', 'IT895623147');
INSERT INTO shipping_lot(shipping_code, product_quantity, shipping_date, customer_vat_num) VALUES ('9638527410', 150, '2022-05-19', 'IT943761235');

INSERT INTO non_compliance_type(code, name, description) VALUES (1, 'Missing', 'Missing ordered items.');
INSERT INTO non_compliance_type(code, name, description) VALUES (2, 'Damage', 'The ordered item is damaged or not-working.');
INSERT INTO non_compliance_type(code, name, description) VALUES (3, 'Inelegible', 'Items different than those ordered arrived.');
INSERT INTO non_compliance_type(code, name, description) VALUES (4, 'Surplus', 'More items arrived than were ordered.');
INSERT INTO non_compliance_type(code, name, description) VALUES (5, 'Optical Inspection Error', 'Detected imperfection or error during optical inspection.');
INSERT INTO non_compliance_type(code, name, description) VALUES (6, 'Solder Paste Imperfection', 'Solder paste applied on the wrong PCB pins.');
INSERT INTO non_compliance_type(code, name, description) VALUES (7, 'Damage Inspection Error', 'Damaged component found during the inspection.');
INSERT INTO non_compliance_type(code, name, description) VALUES (8, 'Pen Drive Not Working', 'Pen Drive doesnt work');
INSERT INTO non_compliance_type(code, name, description) VALUES (9, 'Wrong Pen Drive Position', 'The pen drive is placed in the wrong way');
INSERT INTO non_compliance_type(code, name, description) VALUES (10, 'Label Readability Error', 'The ID Label is unreadable or was badly printed.');
INSERT INTO non_compliance_type(code, name, description) VALUES (11, 'Wrong Label Code', 'The code printed on the label is wrong');
INSERT INTO non_compliance_type(code, name, description) VALUES (12, 'Damaged Pen Drive error', 'The assembled pen drive is not working.');
INSERT INTO non_compliance_type(code, name, description) VALUES (13, 'Storing Damage Error', 'The pen drive got damaged during the storing.');
INSERT INTO non_compliance_type(code, name, description) VALUES (14, 'Shipment Delay', 'Shipping delay caused by internal errors in the shipping process');
INSERT INTO non_compliance_type(code, name, description) VALUES (15, 'Customer Complaint', 'The customer received an incorrect or broken item.');

INSERT INTO users(fiscal_code, email, first_name, last_name, password, job, role, employer_vat_num)
VALUES ('AAAABBBBAAAABBBB', 'admin@jdigital.com', 'Admin', 'JDigital', '$2a$10$x7NkP.T/PQtVGDDsd.sP2.y/F3AFsXhdpHu.VZzyBet0waqxtKmUW', 'CEO', 'admin', 'IT895623147');