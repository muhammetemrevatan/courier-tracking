# 🚚 Courier Tracking System

A Spring Boot application that ingests **streaming courier locations**

- Logs a courier *entrance* when they come within **100 meters** of a Migros store  
  (re-entries to the **same** store within **1 minute** are ignored),
- Tracks each courier’s **total travel distance**, and Exposes REST endpoints to push locations and query distance/logs.

---

## 🗂 Database Tables

- **COURIERS** → Stores the courier’s name and total distance traveled.
- **COURIER_DETAILS** → Stores the courier’s last known location details.
- **COURIER_LOGS** → Stores logs of the courier’s store entrances.
- **STORES** → Stores store information and geolocation.

---

## 🏗 Architecture & Design

**Hexagonal Architecture (Ports & Adapters)**

- **Domain**: core business objects & behavior `Courier`, `Location`, `Store`, `ProximityPolicy`, `DistanceStrategy (Haversine)`

- **Application (Use-Cases)**: orchestration & domain coordination `CourierService`, ports (`…Port` interfaces), observers

- **Infrastructure**: implementation details JPA repositories, Redis cache adapter, Flyway migrations, AOP logging, i18n

**Patterns Used**

- **Observer**: location events notify multiple observers (e.g., `CourierService` + demo notification/log observer)
- **Strategy**: pluggable distance calculation (default **Haversine**)
- **Optimistic Locking**: handles updates on courier aggregates
- **AOP**: controller start/finish logging (cross-cutting concern)

---

## ⚙️ Requirements

- **Java 21**
- **Spring Boot 3.4.x**
- **Maven 3.9+**
- **Docker** (Docker Desktop, Colima, or compatible)
- **Redis 7+** (for caching last location & per-store last-entry state)
- **H2** (in-memory database for demo)

---

## ▶️ Running the Project

### Option A - Docker Compose (recommended)

```bash
1- cd path/to/project - # navigate to project root
2- cd .compose - # navigate to compose directory 
3- docker-compose up --build # build & start services 
```

### Option B - Local (no containers)

```bash
1- Start Redis locally (or via docker run redis:7 -p 6379:6379)
2- Run the app:
    ./mvnw spring-boot:run
    # or
    mvn spring-boot:run
```

### Test with Swagger UI & H2 Console will be available at:
* http://localhost:8080/h2-console/
* http://localhost:8080/swagger-ui/index.html/ -- API docs & testing
* Sample coordinates:
  * (outside the store geofence): (41.001331, 29.1244229)
  * (outside the store geofence): (40.9923307, 29.126223)
  * (inside the store geofence): (40.992781, 29.1244229)

## ⚙️ Resources

- https://www.geeksforgeeks.org/dsa/haversine-formula-to-find-distance-between-two-points-on-a-sphere/
- https://stackoverflow.com/questions/27928/calculate-distance-between-two-latitude-longitude-points-haversine-formula
- https://www.baeldung.com/java-find-distance-between-points
- https://www.coordinatesfinder.com/
- https://www.baeldung.com/spring-retry
