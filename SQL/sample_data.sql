insert into locations (city,hub_value)
values("Chandigarh",20),
("Jaipur",40),
("Pune",60),
("Banglore",80);

INSERT INTO categories (category_name) VALUES
('Electronics'),
('Clothing'),
('Books'),
('Home & Kitchen'),
('Sports & Outdoors');

-- then create 2 users seller1, seller2


INSERT INTO products 
(user_id, category_id, product_name, product_price, product_mrp, product_quantity, 
 product_avg_rating, product_reviews_count, brand, description, image_url)
VALUES
(2, 1, 'Headphones', 1499.00, 1999.00, 50, 
 0, 0, 'Boat', 
 'High-quality wireless headphones with deep bass and long battery backup.',
 'https://example.com/images/headphones.jpg'),

(2, 1, 'Smart LED', 23999.00, 28999.00, 20, 
 0, 0, 'Samsung',
 '43-inch Full HD smart LED TV with powerful speakers.',
 'https://example.com/images/tv.jpg'),

(3, 2, 'T-Shirt', 499.00, 799.00, 120, 
 0, 0, 'H&M',
 'Soft and breathable cotton t-shirt for everyday wear.',
 'https://example.com/images/tshirt.jpg'),

(3, 2, 'Shoes', 1999.00, 2499.00, 60, 
 0, 0, 'Nike',
 'Lightweight and comfortable running shoes for daily workouts.',
 'https://example.com/images/shoes.jpg'),

(2, 3, 'Java Programming', 899.00, 1099.00, 40, 
 0, 0, 'Pearson',
 'Beginner-friendly Java programming book with examples.',
 'https://example.com/images/java_book.jpg'),

(2, 3, 'Data Structures', 699.00, 999.00, 30, 
 0, 0, 'McGraw Hill',
 'Complete guide to DSA with theory and coding practice.',
 'https://example.com/images/dsa_book.jpg'),

(2, 4, 'Frying Pan', 899.00, 1299.00, 35, 
 0, 0, 'Prestige',
 'Durable non-stick fry pan suitable for gas and induction.',
 'https://example.com/images/frypan.jpg'),

(2, 4, 'Kettle', 999.00, 1499.00, 45, 
 0, 0, 'Philips',
 'Fast boiling electric kettle with auto cut-off technology.',
 'https://example.com/images/kettle.jpg'),

(2, 5, 'Badminton Racket', 1299.00, 1599.00, 50, 
 0, 0, 'Yonex',
 'Lightweight badminton racket set for beginners and intermediate players.',
 'https://example.com/images/racket.jpg'),

(2, 5, 'Football', 799.00, 999.00, 70, 
 0, 0, 'Nivia',
 'Durable size-5 football suitable for turf and outdoor play.',
 'https://example.com/images/football.jpg');
