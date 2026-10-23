# E-Commerce Order & Payment Backend

## Overview

This is a beginner-friendly Java Spring Boot backend for a simple e-commerce system. It lets users browse products, manage a shopping cart, place orders, reduce product stock, simulate payments, and track order status.

The project is intentionally monolithic and easy to explain in a fresher interview. It does not use authentication, microservices, Kafka, Redis, Docker, cloud services, or real payment gateway integration.

## Features

- User CRUD APIs with email uniqueness validation
- Category CRUD APIs
- Product CRUD APIs, product search, and category filter
- User shopping cart with item subtotal and cart total calculation
- Order creation from cart
- Product stock validation and stock reduction
- Order cancellation with stock restoration
- Payment simulation with SUCCESS and FAILED outcomes
- DTO-based API responses
- Global exception handling
- Jakarta Bean Validation
- MySQL database with JPA/Hibernate
- Sample data seeding for users, categories, products, and carts

## Technology Stack

- Java 17
- Spring Boot 3.x
- Spring Web
- Spring Data JPA
- Hibernate
- MySQL
- Maven
- Jakarta Bean Validation
- Lombok
- Embedded Apache Tomcat
- Postman

## Architecture

The project follows a simple layered structure:

```text
Client/Postman
-> Embedded Apache Tomcat
-> Spring MVC Controller
-> Service
-> Repository
-> Hibernate/JPA
-> MySQL
## Database

Main tables:

- `users`
- `categories`
- `products`
- `carts`
- `cart_items`
- `orders`
- `order_items`
- `payments`

Main relationships:

- One user has one cart
- One category has many products
- One cart has many cart items
- One product can appear in many cart items
- One user can have many orders
- One order has many order items
- One product can appear in many order items
- One order has one payment

`OrderItem.priceAtPurchase` stores the product price at order time. This is important because product prices may change later, but old orders should keep the original price.

## Important Endpoints

### Users

- `POST /api/users`
- `GET /api/users`
- `GET /api/users/{id}`
- `PUT /api/users/{id}`
- `DELETE /api/users/{id}`

### Categories

- `POST /api/categories`
- `GET /api/categories`
- `GET /api/categories/{id}`
- `PUT /api/categories/{id}`
- `DELETE /api/categories/{id}`

### Products

- `POST /api/products`
- `GET /api/products`
- `GET /api/products/{id}`
- `PUT /api/products/{id}`
- `DELETE /api/products/{id}`
- `GET /api/products/search?name=phone`
- `GET /api/products/category/{categoryId}`

### Cart

- `GET /api/carts/user/{userId}`
- `POST /api/carts/{cartId}/items`
- `PUT /api/carts/{cartId}/items/{productId}`
- `DELETE /api/carts/{cartId}/items/{productId}`
- `DELETE /api/carts/{cartId}/clear`

### Orders

- `POST /api/orders/user/{userId}`
- `GET /api/orders/{orderId}`
- `GET /api/orders/user/{userId}`
- `GET /api/orders`
- `PUT /api/orders/{orderId}/cancel`
- `PUT /api/orders/{orderId}/status`

### Payments
- `POST /api/payments/order/{orderId}`
- `GET /api/payments/{paymentId}`
- `GET /api/payments/order/{orderId}`

## Setup

1. Install Java 17 or later.
2. Install MySQL.
3. Create a database:

```sql
CREATE DATABASE ecommerce_order_payment_db;
```

4. Configure credentials using environment variables, or edit local values:

```text
DB_URL=jdbc:mysql://localhost:3306/ecommerce_order_payment_db?createDatabaseIfNotExist=true
DB_USERNAME=root
DB_PASSWORD=your_mysql_password
```

5. Run the project:

```bash
mvn spring-boot:run
```

6. Open Postman and test:

```text
http://localhost:8080
```

## Project Flow

```text
User -> Product -> Cart -> Order -> Payment -> Order Status
```

1. Create categories and products.
2. Create a user. A cart is created for the user automatically.
3. Add products to the cart.
4. Create an order from the cart.
5. The service checks stock and reduces it.
6. Payment is simulated.
7. If payment succeeds, the order becomes `CONFIRMED` and the cart is cleared.
8. If payment fails, the order becomes `CANCELLED`, stock is restored, and the cart remains available.

## Why `@Transactional` Is Used

Order creation changes multiple tables: products, orders, order items, payments, and cart items. These changes must succeed together. If stock reduction succeeds but payment or order saving fails, the database would become inconsistent. `@Transactional` makes the full operation atomic, so Spring rolls back database changes when an unexpected failure occurs.

## Apache Tomcat

Spring Boot's `spring-boot-starter-web` includes embedded Apache Tomcat. You do not need to install Tomcat separately for local development. When a Postman request is sent, Tomcat receives the HTTP request and passes it to Spring MVC, which calls the correct controller method.
```

Controllers receive HTTP requests and return responses. Services contain business logic such as stock checks, order creation, payment simulation, and cancellation. Repositories talk to the database through Spring Data JPA. Hibernate converts Java entities into database rows.
