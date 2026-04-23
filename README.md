# Smart Campus Sensor & Room Management API

## Overview

This project implements a RESTful API for managing rooms, sensors, and sensor readings within a Smart Campus system.
It is built using **JAX-RS (Jersey)** and follows REST architectural principles.

The API allows:

* Managing rooms and their capacities
* Registering sensors within rooms
* Recording and retrieving sensor readings
* Filtering sensors by type
* Handling errors with appropriate HTTP status codes

---

## Technologies Used

* Java (JDK 17)
* JAX-RS (Jersey)
* Maven
* Grizzly HTTP Server
* In-memory data storage (HashMap, ArrayList)

---

## How to Run

1. Clone the repository:


git clone <your-repo-url>
cd smart-campus-api


2. Run the server:


mvn clean compile exec:java


3. The API will start at:


http://localhost:8080/api/v1


---

## API Endpoints

### Rooms

* GET /api/v1/rooms
* POST /api/v1/rooms
* GET /api/v1/rooms/{roomId}
* DELETE /api/v1/rooms/{roomId}

### Sensors

* GET /api/v1/sensors
* GET /api/v1/sensors?type=CO2
* POST /api/v1/sensors
* GET /api/v1/sensors/{sensorId}

### Sensor Readings

* GET /api/v1/sensors/{sensorId}/readings
* POST /api/v1/sensors/{sensorId}/readings

---

## Sample curl Commands

### 1. Get API discovery


curl http://localhost:8080/api/v1


### 2. Get all rooms


curl http://localhost:8080/api/v1/rooms


### 3. Create a room


curl -X POST http://localhost:8080/api/v1/rooms -H "Content-Type: application/json" -d "{\"id\":\"LAB-101\",\"name\":\"Computer Lab\",\"capacity\":40}"


### 4. Create a sensor


curl -X POST http://localhost:8080/api/v1/sensors -H "Content-Type: application/json" -d "{\"id\":\"CO2-001\",\"type\":\"CO2\",\"status\":\"ACTIVE\",\"currentValue\":415.5,\"roomId\":\"LAB-101\"}"


### 5. Filter sensors by type


curl "http://localhost:8080/api/v1/sensors?type=CO2"


### 6. Add a sensor reading


curl -X POST http://localhost:8080/api/v1/sensors/CO2-001/readings -H "Content-Type: application/json" -d "{\"value\":420.8}"


### 7. Get sensor readings


curl http://localhost:8080/api/v1/sensors/CO2-001/readings


---

## Error Handling

The API uses custom exception handling and returns meaningful HTTP status codes:

| Scenario                         | Status Code               |
| -------------------------------- | ------------------------- |
| Duplicate resource               | 409 Conflict              |
| Invalid linked resource          | 422 / 400                 |
| Resource not found               | 404 Not Found             |
| Sensor unavailable (maintenance) | 403 Forbidden             |
| Unexpected errors                | 500 Internal Server Error |

---

## Key Features

* RESTful API design using JAX-RS
* Sub-resource locator pattern for sensor readings
* Query parameter filtering (`?type=CO2`)
* In-memory data storage (no database used)
* Structured JSON responses
* Logging of requests and responses
* Proper HTTP status codes and error handling

---

## Design Decisions

* **Sub-resource locator** is used to manage sensor readings:

   * /sensors/{sensorId}/readings is handled by SensorReadingResource
* * *In-memory storage** is used as required by the coursework
* **Exception mappers** ensure clean API responses without exposing internal errors

---

## Notes

* No database is used (as per coursework requirements)
* No Spring Boot is used — only JAX-RS
* Designed for demonstration and coursework purposes

---

## Coursework Questions & Answers


---

### **Part 1 – Service Architecture & Setup**

**Q1: What is the lifecycle of a JAX-RS Resource class?**

**Answer:**
By default, JAX-RS resource classes are instantiated per request, meaning a new instance is created for each incoming HTTP request. This approach avoids shared state between requests, reducing concurrency issues. However, since this project uses in-memory data structures such as `HashMap` and `ArrayList`, these structures must be shared and managed carefully to avoid race conditions. Using thread-safe collections like `ConcurrentHashMap` ensures safe access when handling multiple requests.

---

**Q2: Why is hypermedia (HATEOAS) important in RESTful APIs?**

**Answer:**
Hypermedia (HATEOAS) allows API responses to include links to related resources, enabling clients to dynamically navigate the API without hardcoding URLs. This improves flexibility, reduces coupling between client and server, and makes APIs easier to maintain. Compared to static documentation, hypermedia provides real-time discoverability and adaptability to changes in the API structure.

---

### **Part 2 – Room Management**

**Q3: What are the implications of returning only IDs vs full objects when listing rooms?**

**Answer:**
Returning only IDs reduces network bandwidth usage and improves performance, especially when dealing with large datasets. However, it requires clients to make additional requests to retrieve full details. Returning full objects provides complete information in a single response, reducing the number of requests but increasing payload size. The choice depends on the trade-off between efficiency and convenience.

---

**Q4: Is the DELETE operation idempotent in your implementation?**

**Answer:**
Yes, DELETE is idempotent. If a room is successfully deleted, repeating the same DELETE request will not change the system state further. Instead, subsequent requests will typically return a `404 Not Found` because the resource no longer exists. If deletion is blocked (e.g., due to existing sensors), repeated requests will consistently return the same error response (`409 Conflict`), maintaining idempotency.

---

### **Part 3 – Sensor Operations**

**Q5: What happens if a client sends data in a format other than JSON?**

**Answer:**
The API explicitly uses `@Consumes(MediaType.APPLICATION_JSON)`. If a client sends data in a different format such as `text/plain` or `application/xml`, JAX-RS will reject the request and return a `415 Unsupported Media Type` error. This ensures that the server only processes valid and expected input formats.

---

**Q6: Why are query parameters preferred over path parameters for filtering?**

**Answer:**
Query parameters are better suited for filtering because they represent optional criteria applied to a collection resource. For example, `/sensors?type=CO2` clearly indicates filtering. Using path parameters like `/sensors/type/CO2` can make the API less flexible and harder to extend when multiple filters are needed. Query parameters allow easier combination of multiple filters and align better with REST design principles.

---

### **Part 4 – Sub-Resources**

**Q7: What are the benefits of the Sub-Resource Locator pattern?**

**Answer:**
The Sub-Resource Locator pattern improves modularity by delegating nested resource handling to separate classes. This keeps the main resource class simpler and easier to maintain. It also improves scalability, as complex APIs can be broken into smaller, focused components. Compared to handling all endpoints in a single class, this approach reduces code duplication and improves readability.

---

### **Part 5 – Error Handling & Logging**

**Q8: Why is HTTP 422 more appropriate than 404 for missing linked resources?**

**Answer:**
HTTP 422 (Unprocessable Entity) is more appropriate because the request itself is valid, but contains semantic errors, such as referencing a non-existent `roomId`. A 404 would imply that the endpoint itself is missing, which is not the case. Therefore, 422 provides a more accurate representation of the problem.

---

**Q9: Why is exposing Java stack traces a security risk?**

**Answer:**
Exposing stack traces can reveal sensitive internal details such as class names, package structures, and implementation logic. Attackers can use this information to identify vulnerabilities and exploit the system. Therefore, APIs should return generic error messages to clients while logging detailed errors internally for debugging purposes.

---

**Q10: Why use JAX-RS filters for logging instead of manual logging?**

**Answer:**
JAX-RS filters allow centralized handling of cross-cutting concerns like logging. This avoids duplicating logging code in every resource method, making the code cleaner and easier to maintain. Filters ensure consistent logging across all endpoints and simplify future modifications, such as changing log formats or adding additional logging details.

---

