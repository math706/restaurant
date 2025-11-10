# 🍽️ Best Matched Restaurants API

A **Spring Boot 3.5.6 (Java 21)** REST API that helps users find the **best-matched restaurants** based on search criteria such as **name**, **customer rating**, **distance**, **price**, and **cuisine**.  
It reads restaurant and cuisine data from CSV files and loads them into an in-memory **H2** database on startup.

Includes:
- Clean layered architecture (Controller → Service → Repository)
- JPA entities (`Restaurant`, `Cuisine`)
- In-memory authentication (`admin` / `admin`)
- Swagger UI for easy testing
- Unit & integration tests
- Gradle + Lombok + Java 21 toolchain

---

## 🧩 Tech Stack

| Component | Version | Purpose |
|------------|----------|----------|
| Java | 21 | Language |
| Spring Boot | 3.5.6 | Application framework |
| Spring Data JPA | — | ORM / persistence |
| Spring Security | — | Basic Auth protection |
| H2 Database | — | In-memory DB for dev/test |
| Lombok | — | Reduce boilerplate |
| Springdoc OpenAPI | 2.1.0 | Swagger UI / API docs |
| Apache Commons CSV | 1.10.0 | Parse CSV input data |
| JUnit 5 + Mockito | — | Unit & integration testing |

---

## 🚀 Getting Started

### Prerequisites
- **Java 21+**
- **Gradle 8+**
- (Optional) IDE with Lombok support (IntelliJ / VS Code)

### ▶️ How to Run the Application

1. Clone the repository or extract the project folder.
2. Open a terminal in the project root.
3. Run the application using **Gradle Wrapper**:

   ```bash
   ./gradlew bootRun

Once started, the app loads the CSV data automatically from:

The application is protected with **HTTP Basic Authentication**.

| Username | Password | Roles |
|-----------|-------|--------|
| admin | admin | USER, ADMIN |

---

### 🧭 Accessing the API (Swagger UI)

After the app starts, open your browser and go to:

👉 [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

Click the “Authorize” button on the top-right.  
Enter username and password (e.g. `admin` / `admin`).  
Test the `/api/restaurants/search` endpoint directly from Swagger.

**Example parameters:**     
customerRating = 3  
price = 15

Swagger automatically shows request parameters and example responses.

---

### 🧪 Testing Without Swagger (via cURL or Postman)

If you prefer to test manually, use the Basic Auth credentials with `curl` or Postman.

**Example cURL request:**

```bash
curl -u admin:adminpass "http://localhost:8080/api/restaurants/search?customerRating=3&price=15"
```


### 🧪 Example Postman Setup

**Method:** GET  
**URL:** `http://localhost:8080/api/restaurants/search`  
**Auth type:** Basic Auth  
**Username:** admin  
**Password:** admin  

**Query params:**   
customerRating: 3   
price: 15

lick **Send** to see the JSON results.

---

### 💾 In-Memory Database (H2)

You can inspect the loaded data in the **H2 console**:

| Setting | Value |
|----------|--------|
| URL | [http://localhost:8080/h2-console](http://localhost:8080/h2-console) |
| JDBC URL | jdbc:h2:mem:restaurantsdb |
| Username | sa |
| Password | (leave empty) |

---

### ✅ Run Automated Tests

To run all unit and integration tests:

```bash
./gradlew test
```

Test reports are generated at:  
build/reports/tests/test/index.html 

### 🧰 Useful URLs   
| Description | URL |
|----------|--------|
| Swagger UI | [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) |
| H2 Console | [http://localhost:8080/h2-console](http://localhost:8080/h2-console) |
| OpenAPI JSON | [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs) |
