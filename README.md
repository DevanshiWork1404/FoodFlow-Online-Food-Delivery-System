# 🍕 FoodFlow – Online Food Delivery System

FoodFlow is an online food delivery system built as an **RDBMS project**. Customers can create an account, browse restaurants and menus, fill a cart, choose a payment method and track their orders. Restaurant owners manage their menu and see incoming orders, and admins get an overview of users and orders.

All data lives in a **PostgreSQL** database and is accessed from **Java using JDBC**. A small **Javalin API server** connects a website made with **HTML, CSS and JavaScript** to the same database. The project also includes the original console version.

## ✨ Features

- **Log in and Create account** with a role: Customer or Restaurant Owner
- **Customer:** browse restaurants, view menus, add food to a cart, enter a delivery address, pay by UPI, Card or Cash on Delivery, view order history
- **Restaurant Owner:** add and remove food items, view orders received by their restaurant
- **Admin:** dashboard with total users, restaurants, orders and revenue, plus lists of all users and orders
- **Server-side totals:** order totals are calculated from the prices stored in the database, not from the browser
- **Console version** (`main.java`) with the same ordering flow

## 🛠️ Tech Stack

| Area | Technology |
|---|---|
| Language | Java 17 |
| Build tool | Maven |
| Database | PostgreSQL (managed with pgAdmin) |
| Database access | JDBC with `PreparedStatement` (PostgreSQL driver 42.7.7) |
| API server | Javalin 6.3.0 on Jetty |
| JSON | Jackson 2.17.2 |
| Frontend | HTML5, CSS3, JavaScript (fetch API) |

## 🏗️ Architecture

```
Browser (HTML / CSS / JavaScript)
        │  HTTP + JSON
        ▼
API Server (Javalin – ApiServer.java)
        │  method calls
        ▼
Repository classes (Java + JDBC)
        │  SQL
        ▼
PostgreSQL (FoodFlow database)
```

The browser never talks to the database directly, so the database password stays on the server.

## 🗄️ Database

The `FoodFlow` database has six tables:

| Table | Purpose |
|---|---|
| `users` | Every user with email, phone, password and role |
| `addresses` | Delivery addresses saved by users |
| `restaurants` | Restaurants, each linked to an owner (`owner_id`) |
| `food_items` | Menu items of each restaurant |
| `orders` | One row per order, with customer, restaurant, address, total, status and payment details |
| `order_items` | The food items and quantities inside each order |

All relationships are one-to-many. `order_items` links orders and food items, which are many-to-many.

<details>
<summary>Reference schema</summary>

```sql
CREATE TABLE users (
    user_id INTEGER PRIMARY KEY,
    name VARCHAR(100), email VARCHAR(100), phone VARCHAR(15),
    password VARCHAR(255), role VARCHAR(30)
);
CREATE TABLE addresses (
    address_id INTEGER PRIMARY KEY, user_id INTEGER,
    address VARCHAR(255), city VARCHAR(100), pincode VARCHAR(10)
);
CREATE TABLE restaurants (
    restaurant_id INTEGER PRIMARY KEY, restaurant_name VARCHAR(100),
    restaurant_address VARCHAR(255), owner_id INTEGER
);
CREATE TABLE food_items (
    food_id INTEGER PRIMARY KEY, restaurant_id INTEGER, food_name VARCHAR(100),
    description VARCHAR(255), price NUMERIC(10,2), category VARCHAR(50)
);
CREATE TABLE orders (
    order_id INTEGER PRIMARY KEY, customer_id INTEGER, restaurant_id INTEGER,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, total_amount NUMERIC(10,2),
    status VARCHAR(30), address_id INTEGER,
    payment_method VARCHAR(30), payment_status VARCHAR(30)
);
CREATE TABLE order_items (
    order_item_id INTEGER PRIMARY KEY, order_id INTEGER,
    food_id INTEGER, quantity INTEGER
);
```

</details>

## 📁 Project Structure

```
FoodFlow
├── pom.xml
└── src/main
    ├── java/com/muj/foodflow/model
    │   ├── User.java, Customer.java, RestaurantOwner.java, Admin.java
    │   ├── DatabaseConnection.java
    │   ├── UserRepository.java, AddressRepository.java
    │   ├── RestaurantRepository.java, FoodItemRepository.java
    │   ├── OrderRepository.java, OrderItemRepository.java
    │   ├── main.java          (console application)
    │   └── ApiServer.java     (web / API server)
    └── resources/public
        ├── index.html         (website)
        └── bg.png             (background image)
```

## 🧱 OOP Concepts Used

- **Encapsulation:** private fields in `User` with getters and setters
- **Inheritance:** `Customer`, `RestaurantOwner` and `Admin` extend `User`
- **Polymorphism:** a `User` reference can hold any role, and `displayUserDetails()` is overridden
- **Abstraction:** repository classes hide the SQL and JDBC details

## 🚀 Getting Started

### Prerequisites

- Java 17
- Maven
- PostgreSQL and pgAdmin

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/<your-username>/FoodFlow.git
   cd FoodFlow
   ```
2. **Create the database** named `FoodFlow` in pgAdmin and create the six tables (see the reference schema above).
3. **Configure the connection** in `DatabaseConnection.java`: set the URL, user name and password for your PostgreSQL. This project uses port `3000`; PostgreSQL's default port is `5432`.
4. **Install dependencies** with Maven:
   ```bash
   mvn clean compile
   ```
5. **Run the web version:** run the `ApiServer` class, then open **http://localhost:7070**.
6. **Or run the console version:** run `main.java` and follow the menu.

> Always open the site through `http://localhost:7070`. Opening `index.html` directly will not work, because the page needs the API server.

## 🔌 API Endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/login` | Log in with email and password |
| POST | `/api/signup` | Create a Customer or Restaurant Owner account |
| GET | `/api/restaurants` | List all restaurants |
| GET | `/api/restaurants/{id}/menu` | Menu of one restaurant |
| POST | `/api/orders` | Place an order |
| GET | `/api/users/{id}/orders` | Order history of a customer |
| GET | `/api/owner/{uid}/menu` | Owner's menu |
| POST | `/api/owner/{uid}/food` | Add a food item |
| DELETE | `/api/owner/{uid}/food/{fid}` | Remove a food item |
| GET | `/api/owner/{uid}/orders` | Orders received by the owner's restaurant |
| GET | `/api/admin/{uid}/summary` | Admin totals (also `/users` and `/orders`) |

## ⚠️ Known Limitations

- Passwords are stored as plain text (a real system should hash them, for example with BCrypt).
- New ids are created with `MAX(id) + 1` instead of database sequences.
- Saving an order is not wrapped in a single database transaction.
- The Delivery Partner role exists in the data but has no page yet.

## 🔮 Future Scope

- Password hashing and secure login sessions
- Foreign keys, `SERIAL` ids and transactions
- Order status updates (Preparing, Out for delivery, Delivered)
- Delivery partner module and an online payment gateway
- Search, ratings and reviews

## 👩‍💻 Author

**Rana Devanshi Chitrang**, Manipal University Jaipur
**Ayushee Singh**, Manipal University Jaipur
