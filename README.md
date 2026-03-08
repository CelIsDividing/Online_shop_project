# CafeShop - Microservices application

A Spring Boot microservices application for a café/shop with Eureka, API Gateway, Feign, Config Server, and Docker orchestration.

---

## Contents

1. [Architecture overview](#architecture-overview)
2. [Eureka Server](#eureka-server)
3. [API Gateway](#api-gateway)
4. [Feign – inter-service communication](#feign--inter-service-communication)
5. [Docker](#docker)
6. [How the project works](#how-the-project-works)
7. [Running the project](#running-the-project)

---

## Architecture overview

```
┌─────────────┐     ┌─────────────┐     ┌──────────────────────────────────────┐
│   web-app   │───▶│ API Gateway │───▶│ user-service │ order-service │ ...  │
│  (port 9000)│     │ (port 8765) │     └──────────────────────────────────────┘
└─────────────┘     └──────┬──────┘                        │
                           │                               │
                           ▼                               ▼
                    ┌─────────────┐                 ┌─────────────┐
                    │   Eureka    │◀────────────────│  Services   │
                    │  (8761)     │   register      │  register   │
                    └─────────────┘                 └─────────────┘
```

**Services:**
- **config-server** (8888) – centralized configuration
- **eureka-server** (8761) – service discovery
- **api-gateway** (8765) – single entry point
- **user-service** (8003) – users
- **order-service** (8001) – orders
- **payment-service** (8002) – payments
- **catalog-service** (8004) – product catalog
- **web-app** (9000) – frontend application

---

## Eureka Server

### What is Eureka?

**Eureka** is Netflix’s service discovery server. It keeps a registry of all microservices—each service registers itself on startup, and other services can discover addresses by name instead of using hardcoded URLs.

### Why is it used?

- Dynamic service discovery (no fixed list of IP:port)
- Load balancing between instances
- Resilience – when a service goes down, Eureka removes it from the registry

### Code configuration

#### 1. Maven dependency (`pom.xml`)

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```

#### 2. Main application – Eureka Server

```java
@EnableEurekaServer
@SpringBootApplication
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

#### 3. Configuration – Eureka Server (`application.properties`)

```properties
spring.application.name=eureka-server
server.port=8761

# The server does NOT register with itself and does NOT fetch the service registry
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
```

#### 4. Configuration – Eureka Client (e.g. user-service)

Each microservice that wants to register must have:

```properties
eureka.client.service-url.defaultZone=http://eureka-server:8761/eureka/
eureka.instance.prefer-ip-address=true
eureka.instance.hostname=user-service
```

- `defaultZone` – Eureka server address (in Docker: `eureka-server`, locally: `localhost`)
- `prefer-ip-address=true` – use IP instead of hostname (important in Docker)
- `hostname` – the name under which the service appears in the Eureka Dashboard

**Dashboard:** `http://localhost:8761` – view registered services.

---

## API Gateway

### What is an API Gateway?

**API Gateway** is a single entry point for all clients. It forwards requests to the appropriate microservices based on the path. It enables centralized logging, authentication, rate limiting, etc.

### Why is it used?

- The client communicates with only one URL (the gateway)
- The gateway routes based on `/service-id/path`
- It hides internal service addresses

### Code configuration

#### 1. Maven dependency (`pom.xml`)

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

#### 2. Configuration (`application.properties`)

```properties
spring.application.name=api-gateway
server.port=8765

# Eureka – the gateway must be a client so it knows where the services are
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.instance.prefer-ip-address=true
eureka.instance.hostname=localhost

# Dynamic routing via Eureka
spring.cloud.gateway.discovery.locator.enabled=true
spring.cloud.gateway.discovery.locator.lower-case-service-id=true

# Removes the first path segment when forwarding
# e.g. /user-service/api/users → forwards /api/users
spring.cloud.gateway.discovery.locator.filters[0].name=StripPrefix
spring.cloud.gateway.discovery.locator.filters[0].args.parts=1
```

#### 3. URL format to call services

```
http://localhost:8765/{service-id}/{path}
```

**Examples:**
- `http://localhost:8765/user-service/api/users` → user-service
- `http://localhost:8765/order-service/api/orders` → order-service
- `http://localhost:8765/payment-service/api/...` → payment-service

The gateway uses Eureka to find an instance of the service with the given name and forwards the request.

---

## Feign – inter-service communication

### What is Feign?

**Feign** is a declarative HTTP client. You create an interface with HTTP methods (e.g. `@GetMapping`), and Spring generates an implementation that sends HTTP requests. It’s ideal for calls between microservices.

### Why is it used?

- Instead of writing `RestTemplate` or `WebClient`, you only write an interface
- Less code, more readable
- Works easily with Eureka (can use service-id instead of a URL)

### Code configuration

#### 1. Maven dependency (`pom.xml`)

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

#### 2. Enabling Feign clients in the main application

```java
@SpringBootApplication
@EnableFeignClients
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
```

#### 3. Feign client definition (interface)

**UserServiceClient** (in order-service):

```java
@FeignClient(name = "user-service", url = "${user-service.url:http://localhost:8003}")
public interface UserServiceClient {

    @GetMapping("/api/users/{id}")
    UserResponse getUserById(@PathVariable Long id);

    record UserResponse(Long id, String name) {}
}
```

**OrderServiceClient** (in payment-service):

```java
@FeignClient(name = "order-service", url = "${order-service.url:http://localhost:8001}")
public interface OrderServiceClient {

    @GetMapping("/api/orders/{id}")
    OrderResponse getOrderById(@PathVariable Long id);

    record OrderResponse(Long id, Long userId) {}
}
```

- `name` – service identifier
- `url` – address (from configuration; fallback to `http://localhost:port`)
- Methods – standard Spring MVC annotations (`@GetMapping`, `@PostMapping`, etc.)

#### 4. URL configuration in Docker (`config-repo`)

In Docker, services call each other by container name:

**config-repo/order-service.properties:**
```properties
user-service.url=http://user-service:8003
```

**config-repo/payment-service.properties:**
```properties
order-service.url=http://order-service:8001
```

#### 5. Using it in a service

```java
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserServiceClient userServiceClient;

    public Order create(CreateOrderRequest request) {
        try {
            userServiceClient.getUserById(request.getUserId());  // validation
        } catch (FeignException.NotFound e) {
            throw new IllegalArgumentException("User does not exist.");
        } catch (FeignException e) {
            throw new IllegalStateException("User-service is not available.");
        }
        // ... create order
    }
}
```

**Call chain in the project:**
- **order-service** → **user-service** (verify the user before creating an order)
- **payment-service** → **order-service** (verify the order before payment)

---

## Docker

### What does the project use?

- **Docker Compose** – orchestrates all services in a single network
- **Dockerfile** – multi-stage build: Maven build → JRE image
- **bridge network** – all containers communicate via service names (e.g. `user-service`, `mysql`)

### Startup order (depends_on)

1. **mysql** – database; others wait for the healthcheck
2. **config-server** – configuration; services fetch DB and Eureka URLs
3. **eureka-server** – registry; others register
4. **api-gateway** – waits for Eureka
5. **user-service, order-service, payment-service, catalog-service** – wait for mysql, config-server, eureka
6. **web-app** – waits for api-gateway and other services

### Per-service settings

**Microservices (e.g. user-service):**
```yaml
environment:
  SPRING_PROFILES_ACTIVE: docker
  CONFIG_SERVER_URL: http://config-server:8888
depends_on:
  mysql:
    condition: service_healthy
  config-server:
    condition: service_healthy
  eureka-server:
    condition: service_healthy
```

**API Gateway:**
```yaml
environment:
  EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE: http://eureka-server:8761/eureka/
  EUREKA_INSTANCE_PREFER_IP_ADDRESS: "true"
```

**Web-app** (calls services through the gateway):
```yaml
environment:
  GATEWAY_URL: http://api-gateway:8765
  USER_SERVICE_URL: http://api-gateway:8765/user-service
  ORDER_SERVICE_URL: http://api-gateway:8765/order-service
  # ...
```

### Dockerfile (typical)

```dockerfile
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

FROM eclipse-temurin:21-jre
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

- **Stage 1:** Maven build using an image with JDK 21
- **Stage 2:** JRE only + curl (for healthcheck) + JAR

---

## How the project works

### Request flow

1. **Client (web-app)** sends a request to `http://api-gateway:8765/user-service/api/users`
2. **API Gateway**, receiving `/user-service/...`, looks up `user-service` in Eureka and forwards the request to `/api/users`
3. **User-service** processes the request and returns the response to the gateway; the gateway forwards it to the client

### Inter-service calls (Feign)

- **Order-service** starts an order → calls **user-service** to validate the user
- **Payment-service** pays for an order → calls **order-service** to validate the order

These calls go **directly** (user-service:8003, order-service:8001) because they are all on the same Docker network. The web-app uses the **gateway** because it accesses the system from outside through a single point.

### Config Server

- Configuration is in `config-repo/` (per-service files: `user-service.properties`, `order-service.properties`, etc.)
- Services with `SPRING_PROFILES_ACTIVE=docker` fetch configuration from the Config Server
- The configuration contains e.g. DB URL, Eureka URL, Feign URLs (Docker container names)

### Database

- One MySQL (`cafeShop`) – shared by user-service, order-service, payment-service, catalog-service
- `init-db/init.sql` – initial data (tables, seed data)
- Each service has its own tables; JPA `ddl-auto=update` updates the schema

---

## Running the project

### Local (without Docker)

1. Start **MySQL** (port 3306, DB `cafeShop`, root/123)
2. Start **config-server** (8888)
3. Start **eureka-server** (8761)
4. Start **api-gateway** (8765)
5. Start microservices: **user-service**, **order-service**, **payment-service**, **catalog-service**
6. Start **web-app** (9000)

In `application.properties` for local usage, `localhost` is used for Eureka and Config Server.

### Docker Compose

```bash
docker-compose up -d
```

All services start in the defined order. The application is available at:

- **Web application:** http://localhost:9000
- **Eureka Dashboard:** http://localhost:8761
- **API Gateway:** http://localhost:8765

**Build and restart:**
```bash
docker-compose build --no-cache
docker-compose up -d
```