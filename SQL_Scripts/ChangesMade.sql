 1)alter table  order_items modify column total_item_price decimal(12,2) 
 as (quantity* unit_price) STORED;
 
 2) update orders o left join( 
 select order_id, COALESCE(SUM(total_item_price),0) as calc_total
 from order_items group by order_id
 ) t on o.order_id = t.order_id SET o.total_amount = t.calc_total;
 
 3)Alter table payments  add column gateway_ref varchar(127) default null,
 add column upi_vpa varchar(127) default null,
 add column response_payload text default null;
 
 4)alter table reviews modify review_text text not null;
 
 5)alter table products add column brand  varchar(30) not null;
 6) alter table products add column description text;
 7) alter table products add column description text  not null;
 
 8) alter table orders modify status enum('PENDING','PROCESSSING','SHIPPED','DELIVERED','CANCELLED','REFUNDED');
 9) alter table payments modify status enum('Pending','PAID','Failed','Refunded');
 
 10) alter table orders add column orderStatus enum('PENDING','PAID','PROCESSSING','SHIPPED','DELIVERED','CANCELLED','REFUNDED') not null;
 11) alter table orders drop column status;
 
 12)alter table orders add column payment_method enum('COD','UPI') not null;
 
 13) alter table products add column image_url varchar(255);
 
 14) drop trigger trg_update_total_amount;
 15) DELIMITER //
mysql> create trigger trg_update_total_amount
    -> after insert on order_items
    -> for each row
    -> BEGIN
    -> update orders
    -> SET total_amount = total_amount + NEW.total_item_price
    -> where order_id = NEW.order_id;
    -> END //
    
    16)INSERT INTO payments
(order_id, user_id, amount, currency, payment_method, status, gateway_ref, upi_vpa, response_payload)
VALUES
(1, 1, 499.00, 'INR', 'COD', 'Pending', NULL, NULL, 'Cash on Delivery not initiated'),
 
(2, 2, 15000.00, 'INR', 'UPI', 'PAID', 'TXNUPI987654321', 'ritu@upi', 'UPI Payment successful'),
 
(3, 4, 999.00, 'INR', 'COD', 'Pending', NULL, NULL, 'Cash on Delivery not initiated'),
 
(4, 5, 3500.00, 'INR', 'UPI', 'PAID', 'TXNUPI123456789', 'user5@upi', 'UPI Payment successful');

17)
INSERT INTO reviews 
(user_id, product_id, rating, review_text, status)
VALUES
(1, 3, 5, 'Excellent quality T-shirt, very comfortable and perfect fit!', 'VISIBLE'),
 
(2, 2, 4, 'Smartphone performance is great, battery life could be better.', 'VISIBLE'),
 
(4, 6, 5, 'Great football! Good grip and durable material.', 'VISIBLE'),
 
(5, 8, 4, 'The skincare kit is good and shows results in a week.', 'VISIBLE');


18)alter table orders add column razorpay_order_id varchar(100) null;
19) alter table orders modify orderStatus enum('PENDING','PAID','PROCESSSING','SHIPPED','DELIVERED','CANCELLED','REFUNDED','EXPIRED');

-------------------------------------------------------
 20)alter table user modify phone varchar(50)  unique;
 21)alter table cart_items add column quantity int not null default 1;
 22)alter table orders modify shipping_address varchar(255);
 -------------------4/12/25----------------------------------------------------
 23)INSERT INTO products 
(user_id, category_id, product_name, product_price, product_mrp, product_quantity, 
 product_avg_rating, product_reviews_count, brand, description, image_url)
VALUES
(1, 1, 'Camera', 2499.00, 2999.00, 4, 
 0, 0, 'Nikon', 
 'High-quality and high pixels, clafity with deep bass and long battery backup.',
 'https://example.com/images/camera.jpg');
 24)INSERT INTO user (name, email, password, phone, role, location_id)
VALUES (
    'Admin',
    'admin@gmail.com',
    '$2a$10$WqH14r2raXIiQunlslqY5OTtyEiZVikC6FzDaXYVCLxA4U6Q7vax.',
    '9999999999',
    'Admin',
    NULL
);
25)update user set password='$2a$10$OefW5akOIXJz.4L4CTjYpebw8aIs7E9LU5YycSUu6KU6buFjVo5iC' where email="admin@gmail.com";
26)Update products set image_url ='headphones.jpg' where product_name ='Headphones';