create database shopSphere;

use shopSphere;


create table  locations(
location_id int auto_increment primary key,
city varchar(30)not null,
hub_value int not null
);



create table user(
user_id int auto_increment primary key,
name varchar(30) not null,
email varchar(30) not null,
password varchar(255) not null,
phone varchar(15),
role ENUM('Admin','Seller','Buyer') default 'Buyer',
location_id int ,
constraint fk_users_location foreign key(location_id) references locations(location_id) on delete set null
);

create table categories(
category_id int auto_increment primary key,
category_name varchar(30) not null
);

create table products(
product_id int auto_increment primary key,
user_id int not null,
category_id int,
product_name varchar(30) not null,
product_price decimal(12,2) default null,
product_mrp decimal(12,2) default null,
product_quantity int,
product_avg_rating decimal(3,2) default null,
product_reviews_count int default 0,
constraint fk_product_seller foreign key(user_id) references user(user_id) on delete cascade,
constraint fk_product_cateogry foreign key(category_id) references categories(category_id) on delete set null

);


create table reviews(
review_id int auto_increment primary key,
user_id int not null,
product_id int not null,
rating int,
review_text text not null,
status ENUM('VISIBLE','HIDDEN','REPORTED') default 'VISIBLE',
created_at timestamp default current_timestamp,
constraint fk_review_user foreign key(user_id) references user(user_id) on delete cascade,
constraint fk_review_product foreign key(product_id) references products(product_id) on delete cascade

);

create table carts(
cart_id int auto_increment primary key,
user_id int not null,
constraint fk_cart_user foreign key(user_id) references user(user_id) on delete cascade
);

create table cart_items(
cart_items_id int auto_increment primary key,
cart_id int not null,
product_id int not null,
constraint fk_cart_items_cart foreign key(cart_id) references carts(cart_id) on delete cascade,
constraint fk_cart_items_product foreign key(product_id) references products(product_id) on delete cascade

);

create table wishlists(
wishlist_id int auto_increment primary key,
user_id int not null,
constraint fk_wishlist_user foreign key(user_id) references user(user_id) on delete cascade

);
create table wishlist_items(
wishlist_items_id int auto_increment primary key,
wishlist_id int not null,
product_id int not null,
constraint fk_wishlist_items_cart foreign key(wishlist_id) references wishlists(wishlist_id) on delete cascade,
constraint fk_wishlist_items_product foreign key(product_id) references products(product_id) on delete cascade

);

create table orders(
order_id int auto_increment primary key,
user_id int not null,
total_amount decimal(12,2) not null,
shipping_address varchar(30) not null,
status ENUM('PENDING','PAID','PROCESSSING','SHIPPED','DELIVERED','CANCELLED','REFUNDED') default 'PENDING',

placed_at timestamp default current_timestamp,
constraint fk_orders_user foreign key(user_id) references user(user_id) on delete cascade

);
create table order_items(
order_items_id int auto_increment primary key,
order_id int not null,
product_id int ,
product_name varchar(30),
quantity int default 1,
unit_price decimal(12,2) ,
total_item_price decimal(12,2),
constraint fk_order_items_cart foreign key(order_id) references orders(order_id) on delete cascade,
constraint fk_order_items_product foreign key(product_id) references products(product_id) on delete set null
);

create table payments(
payment_id int  auto_increment primary key,
order_id int,
user_id int,
amount decimal(12,2) not null,
currency varchar(30) default 'INR',
payment_method ENUM('COD','UPI'),
created_at timestamp default current_timestamp,
status ENUM('Pending','Initiated','Completed','Failed','Refunded') default 'Initiated',
constraint fk_payment_order foreign key(order_id) references orders(order_id) on delete cascade,
constraint fk_payment_user foreign key(user_id) references user(user_id) on delete cascade
);
