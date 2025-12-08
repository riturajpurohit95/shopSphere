-- MySQL dump 10.13  Distrib 8.0.38, for Win64 (x86_64)
--
-- Host: localhost    Database: shopsphere
-- ------------------------------------------------------
-- Server version	8.0.39

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` VALUES (21,1,1,'Headphones',1499.00,1999.00,59,0.00,0,'Boat','High-quality wireless headphones with deep bass and long battery backup.','headphones.jpg'),(22,8,1,'Smart LED',23999.00,28999.00,4,0.00,0,'Samsung','43-inch Full HD smart LED TV with powerful speakers.','tv.jpg'),(23,1,2,'T-Shirt',499.00,799.00,120,0.00,0,'H&M','Soft and breathable cotton t-shirt for everyday wear.','tshirt.jpg'),(24,8,2,'Shoes',1999.00,2499.00,60,0.00,0,'Nike','Lightweight and comfortable running shoes for daily workouts.','shoes.jpg'),(25,1,3,'Java Programming',899.00,1099.00,40,0.00,0,'Pearson','Beginner-friendly Java programming book with examples.','java_book.jpg'),(26,1,3,'Data Structures',699.00,999.00,30,0.00,0,'McGraw Hill','Complete guide to DSA with theory and coding practice.','dsa_book.jpg'),(27,1,4,'Frying Pan',899.00,1299.00,35,0.00,0,'Prestige','Durable non-stick fry pan suitable for gas and induction.','frypan.jpg'),(28,8,4,'Kettle',999.00,1499.00,45,0.00,0,'Philips','Fast boiling electric kettle with auto cut-off technology.','kettle.jpg'),(29,1,5,'Badminton Racket',1299.00,1599.00,50,0.00,0,'Yonex','Lightweight badminton racket set for beginners and intermediate players.','racket.jpg'),(30,1,5,'Football',799.00,999.00,75,0.00,0,'Nivia','Durable size-5 football suitable for turf and outdoor play.','football.jpg'),(31,1,1,'Camera',2499.00,2999.00,4,0.00,0,'Nikon','High-quality and high pixels, clafity with deep bass and long battery backup.','camera.jpg'),(32,1,1,'Dryer',3999.00,5000.00,4,0.00,0,'Philips','2 minute dryer','gps.jpg');
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-12-05 18:31:52
