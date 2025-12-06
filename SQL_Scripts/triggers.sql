--------------------triggers------------------------

-----------------------------------PRODUCT-------------------------------------------
-----check stock before placing order if enough stock available---
DELIMITER //
create trigger trg_check_stock_availability
before insert on order_items
for each row
BEGIN
	DECLARE available_stock int DEFAULT 0;
	select product_quantity into available_stock
	from products
	where product_id= NEW.product_id;
	if available_Stock < NEW.quantity then
	SIGNAL SQLSTATE '45000'
	SET MESSAGE_TEXT = 'Not enough stock stock available for this product';
	END IF;
END //




---to reduce stock when order_items is added-----
Create trigger trg_reduce_stock
after insert on order_items
for each row
BEGIN
	update products
	SET product_quantity = product_quantity - NEW.quantity
	where product_id = NEW.product_id;
	
END //


----restore stock after order_items deleted----
create trigger trg_restore_stock_after_order_deletion
after delete on order_items
for each row
BEGIN
	update products
	SET product_quantity = product_quantity + OLD.quantity
	where product_id = OLD.product_id;
	
END //

-------------------------------ORDER_ITEMS----------------------------------------

----UPDATE  TOTAL ITEM PRICES AFTER ITEMS FOR ORDER INSERTED------------------------------

create trigger trg_update_total_amount
after insert on order_items
for each row
BEGIN
	update orders
	SET total_amount = total_amount + NEW.total_item_price
	where order_id = NEW.order_id;
END //
-----update order toal after ite, deleted------------

create trigger trg_update_order_total_after_delete
after delete on order_items
for each row
BEGIN
	update orders
	SET total_amount = total_amount  - OLD.total_item_price
	where order_id = OLD.order_id;
END //
------update order total after update------------------------------------------------
 -------------------
create trigger trg_update_order_total_after_update
after update on order_items
for each row
BEGIN
	IF NEW.order_id = OLD.order_id then
	update orders
	SET total_amount = (total_amount  - OLD.total_item_price) + NEW.total_item_price
	where order_id = NEW.order_id;
	
	else
	update orders
	SET total_amount = (total_amount  - OLD.total_item_price)
	where order_id = OLD.order_id;
	
	update orders
	SET total_amount = total_amount  + NEW.total_item_price
	where order_id = NEW.order_id;
	
	END IF;
	
END //
--------------order-----------------------------------------

--------restore stock if orfer is cancelled-----------------
create trigger trg_restore_stock_after_order_delete
after update on orders
for each row
BEGIN
	IF NEW.status ='CANCELLED' AND OLD.status <> 'CANCELLED' THEN
	update products p
	join order_items oi on oi.product_id =p.product_id
	SET p.product_quantity = p.product_quantity + oi.quantity
	where oi.order_id = NEW.order_id;
	END IF;
END //

-------------payment-------------------------------

--------update payment status after successful payment------------------------
create trigger trg_update_order_payment_status
after insert on payments
for each row
BEGIN
	IF NEW.status = 'COMPLETED' then 
	update orders
	SET status = 'PAID'
	where order_id = NEW.order_id;
	END IF;
END //

-- 1. prevent duplicate cart

DELIMITER //

CREATE TRIGGER trg_prevent_duplicate_cart
BEFORE INSERT ON carts
FOR EACH ROW
BEGIN
	IF EXISTS (SELECT 1 FROM carts WHERE user_id = NEW.user_id)
	THEN 
	SIGNAL SQLSTATE '45000'
	SET MESSAGE_TEXT = 'User already has a cart';
	END IF;
END ;
//

DELIMITER ; 

-- cart_items table

-- 1. prevent adding same product twice in the same cart  -- filter in addItem() of CartItemDaoImpl

DELIMITER //

CREATE TRIGGER trg_cart_item_unique
BEFORE INSERT ON cart_items
FOR EACH ROW
BEGIN
	IF EXISTS (
	SELECT 1 FROM cart_items
	WHERE cart_id = NEW.cart_id AND 
	product_id = NEW.product_id)
	THEN 
	SIGNAL SQLSTATE '45000'
	SET MESSAGE_TEXT = 'Product already exists in cart';
	END IF;
END ;
//

DELIMITER ; 


-- 2. prevent zero or negative quantity

DELIMITER //

CREATE TRIGGER trg_cart_item_quantity_check
BEFORE INSERT ON cart_items
FOR EACH ROW
BEGIN
	IF NEW.quantity <= 0
	THEN 
	SIGNAL SQLSTATE '45000'
	SET MESSAGE_TEXT = 'Quantity must be greater than 0';
	END IF;
END ;
//

CREATE TRIGGER trg_cart_item_quantity_check_update
BEFORE UPDATE ON cart_items
FOR EACH ROW
BEGIN
	IF NEW.quantity <= 0
	THEN 
	SIGNAL SQLSTATE '45000'
	SET MESSAGE_TEXT = 'Quantity must be greater than 0';
	END IF;
END ;
//

DELIMITER ;


-- categories table

-- 1. prevent duplicate category names

DELIMITER //

CREATE TRIGGER trg_category_unique_name
BEFORE INSERT ON categories
FOR EACH ROW
BEGIN
	IF EXISTS (
	SELECT 1 FROM categories
	WHERE category_name = NEW.category_name)
	THEN 
	SIGNAL SQLSTATE '45000'
	SET MESSAGE_TEXT = 'Category name already exists';
	END IF;
END ;
//

CREATE TRIGGER trg_category_unique_name_update
BEFORE UPDATE ON categories
FOR EACH ROW
BEGIN
	IF EXISTS (
	SELECT 1 FROM categories
	WHERE category_name = NEW.category_name 
	AND category_id != NEW.category_id)
	THEN 
	SIGNAL SQLSTATE '45000'
	SET MESSAGE_TEXT = 'Category name already exists';
	END IF;
END ;
//

DELIMITER ;

-- 2. prevent deletion of category if products exist

DELIMITER //

CREATE TRIGGER trg_category_delete_check
BEFORE DELETE ON categories
FOR EACH ROW
BEGIN
	IF EXISTS (
	SELECT 1 FROM products
	WHERE category_id = OLD.category_id)
	THEN 
	SIGNAL SQLSTATE '45000'
	SET MESSAGE_TEXT = 'Cannot delete category with existing products';
	END IF;
END ;
//

DELIMITER ;

---------------------------------------------
DELIMITER $$

DROP TRIGGER IF EXISTS trg_reviews_after_delete $$
CREATE TRIGGER trg_reviews_after_delete
AFTER DELETE ON reviews
FOR EACH ROW
BEGIN
  /* Recompute aggregates for the affected product after a review is deleted.
     We consider only reviews with status = 'VISIBLE'. */
  UPDATE products p
  LEFT JOIN (
    SELECT
      r.product_id,
      COUNT(*) AS cnt,
      AVG(r.rating) AS avg_rating
    FROM reviews r
    WHERE r.product_id = OLD.product_id
      AND r.status = 'VISIBLE'
  ) agg ON agg.product_id = p.product_id
  SET p.product_reviews_count = COALESCE(agg.cnt, 0),
      p.product_avg_rating    = ROUND(COALESCE(agg.avg_rating, 0), 2)
  WHERE p.product_id = OLD.product_id;
END $$

DELIMITER ;
--------------------------------------------------------
DELIMITER $$

DROP TRIGGER IF EXISTS trg_reviews_after_insert $$
CREATE TRIGGER trg_reviews_after_insert
AFTER INSERT ON reviews
FOR EACH ROW
BEGIN
  /* Only recompute if this review is meant to be visible */
  IF NEW.status = 'VISIBLE' THEN
    UPDATE products p
    JOIN (
      SELECT
        r.product_id,
        COUNT(*) AS cnt,
        AVG(r.rating) AS avg_rating
      FROM reviews r
      WHERE r.product_id = NEW.product_id
        AND r.status = 'VISIBLE'
    ) agg ON agg.product_id = p.product_id
    SET p.product_reviews_count = agg.cnt,
        p.product_avg_rating    = ROUND(agg.avg_rating, 2);
  END IF;
END $$

DELIMITER ;

--------------------------------------------------------
DELIMITER $$

DROP TRIGGER IF EXISTS trg_wishlists_after_delete $$
CREATE TRIGGER trg_wishlists_after_delete
AFTER DELETE ON wishlists
FOR EACH ROW
BEGIN
    DELETE FROM wishlist_items
    WHERE wishlist_id = OLD.wishlist_id;
END $$

DELIMITER ;

------------------------------------------------------
DELIMITER $$

DROP TRIGGER IF EXISTS trg_user_delete_cascade_products $$
CREATE TRIGGER trg_user_delete_cascade_products
AFTER DELETE ON `user`
FOR EACH ROW
BEGIN
    -- Only cascade delete products if the deleted user was a Seller
    IF OLD.role = 'Seller' THEN
        DELETE FROM products
        WHERE user_id = OLD.user_id;
    END IF;
END $$

DELIMITER ;

