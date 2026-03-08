# CafeShop - Mikroservisna aplikacija

Spring Boot mikroservisna aplikacija za kafić/shop sa Eureka, API Gateway, Feign, Config Server i Docker orkestracijom.

---

## Sadržaj

1. [Pregled arhitekture](#pregled-arhitekture)
2. [Eureka Server](#eureka-server)
3. [API Gateway](#api-gateway)
4. [Feign – međuservisna komunikacija](#feign--međuservisna-komunikacija)
5. [Docker](#docker)
6. [Kako projekat funkcioniše](#kako-projekat-funkcioniše)
7. [Pokretanje](#pokretanje)

---

## Pregled arhitekture

```
┌─────────────┐     ┌─────────────┐     ┌──────────────────────────────────────┐
│   web-app   │────▶│ API Gateway │────▶│ user-service │ order-service │ ...    │
│  (port 9000)│     │ (port 8765) │     └──────────────────────────────────────┘
└─────────────┘     └──────┬──────┘                        │
                           │                               │
                           ▼                               ▼
                    ┌─────────────┐                 ┌─────────────┐
                    │   Eureka    │◀────────────────│  Servisi se │
                    │  (8761)     │   registruju    │  registruju │
                    └─────────────┘                 └─────────────┘
```

**Servisi:**
- **config-server** (8888) – centralna konfiguracija
- **eureka-server** (8761) – service discovery
- **api-gateway** (8765) – jedinstvena ulazna tačka
- **user-service** (8003) – korisnici
- **order-service** (8001) – porudžbine
- **payment-service** (8002) – plaćanja
- **catalog-service** (8004) – katalog proizvoda
- **web-app** (9000) – frontend aplikacija

---

## Eureka Server

### Šta je Eureka?

**Eureka** je Netflix-ov service discovery server. Drži registar svih mikroservisa – svaki servis se pri startu registruje, a drugi servisi mogu pronaći adrese preko imena umesto hardkodovanih URL-ova.

### Zašto se koristi?

- Dinamičko pronalaženje servisa (bez fiksne liste IP:port)
- Load balancing između instanci
- Otpornost – kada servis nestane, Eureka ga uklanja iz registra

### Podešavanja u kodu

#### 1. Maven dependency (`pom.xml`)

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```

#### 2. Glavna aplikacija – Eureka Server

```java
@EnableEurekaServer
@SpringBootApplication
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

#### 3. Konfiguracija – Eureka Server (`application.properties`)

```properties
spring.application.name=eureka-server
server.port=8761

# Server se NE registruje u sebe i NE povlači listu servisa
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
```

#### 4. Konfiguracija – Eureka Client (npr. user-service)

Svaki mikroservis koji želi da se registruje mora imati:

```properties
eureka.client.service-url.defaultZone=http://eureka-server:8761/eureka/
eureka.instance.prefer-ip-address=true
eureka.instance.hostname=user-service
```

- `defaultZone` – adresa Eureka servera (u Dockeru: `eureka-server`, lokalno: `localhost`)
- `prefer-ip-address=true` – koristi IP umesto hostname (važno u Dockeru)
- `hostname` – ime pod kojim se servis vidi u Eureka Dashboard-u

**Dashboard:** `http://localhost:8761` – pregled registrovanih servisa.

---

## API Gateway

### Šta je API Gateway?

**API Gateway** je jedinstvena ulazna tačka za sve klijente. Prosleđuje zahteve odgovarajućim mikroservisima na osnovu putanje (path). Omogućava centralizovani log, autentifikaciju, rate limiting itd.

### Zašto se koristi?

- Klijent komunicira samo sa jednim URL-om (gateway)
- Gateway rutira na osnovu `/service-id/path`
- Skriva interne adrese servisa

### Podešavanja u kodu

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

#### 2. Konfiguracija (`application.properties`)

```properties
spring.application.name=api-gateway
server.port=8765

# Eureka – gateway mora biti klijent da bi znao gde su servisi
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.instance.prefer-ip-address=true
eureka.instance.hostname=localhost

# Dinamičko rutiranje preko Eureka
spring.cloud.gateway.discovery.locator.enabled=true
spring.cloud.gateway.discovery.locator.lower-case-service-id=true

# Uklanja prvi segment putanje pri prosleđivanju
# npr. /user-service/api/users → prosleđuje /api/users
spring.cloud.gateway.discovery.locator.filters[0].name=StripPrefix
spring.cloud.gateway.discovery.locator.filters[0].args.parts=1
```

#### 3. Format URL-a za poziv servisa

```
http://localhost:8765/{service-id}/{path}
```

**Primeri:**
- `http://localhost:8765/user-service/api/users` → user-service
- `http://localhost:8765/order-service/api/orders` → order-service
- `http://localhost:8765/payment-service/api/...` → payment-service

Gateway koristi Eureka da pronađe instancu servisa sa datim imenom i prosledi zahtev.

---

## Feign – međuservisna komunikacija

### Šta je Feign?

**Feign** je deklarativni HTTP klijent. Napraviš interface sa HTTP metodama (npr. `@GetMapping`), a Spring kreira implementaciju koja šalje HTTP zahteve. Idealno za pozive između mikroservisa.

### Zašto se koristi?

- Umesto `RestTemplate` ili `WebClient` pišeš samo interface
- Manje koda, čitljivije
- Lako radi sa Eureka (može koristiti service-id umesto URL-a)

### Podešavanja u kodu

#### 1. Maven dependency (`pom.xml`)

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

#### 2. Uključivanje Feign klijenata u glavnoj aplikaciji

```java
@SpringBootApplication
@EnableFeignClients
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
```

#### 3. Definicija Feign klijenta (interface)

**UserServiceClient** (u order-service):

```java
@FeignClient(name = "user-service", url = "${user-service.url:http://localhost:8003}")
public interface UserServiceClient {

    @GetMapping("/api/users/{id}")
    UserResponse getUserById(@PathVariable Long id);

    record UserResponse(Long id, String name) {}
}
```

**OrderServiceClient** (u payment-service):

```java
@FeignClient(name = "order-service", url = "${order-service.url:http://localhost:8001}")
public interface OrderServiceClient {

    @GetMapping("/api/orders/{id}")
    OrderResponse getOrderById(@PathVariable Long id);

    record OrderResponse(Long id, Long userId) {}
}
```

- `name` – identifikator servisa
- `url` – adresa (iz konfiguracije; fallback na `http://localhost:port`)
- Metode – standardne Spring MVC anotacije (`@GetMapping`, `@PostMapping`, itd.)

#### 4. Konfiguracija URL-a u Dockeru (`config-repo`)

U Dockeru servisi se pozivaju po imenu kontejnera:

**config-repo/order-service.properties:**
```properties
user-service.url=http://user-service:8003
```

**config-repo/payment-service.properties:**
```properties
order-service.url=http://order-service:8001
```

#### 5. Korišćenje u servisu

```java
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserServiceClient userServiceClient;

    public Order create(CreateOrderRequest request) {
        try {
            userServiceClient.getUserById(request.getUserId());  // validacija
        } catch (FeignException.NotFound e) {
            throw new IllegalArgumentException("Korisnik ne postoji.");
        } catch (FeignException e) {
            throw new IllegalStateException("User-service nije dostupan.");
        }
        // ... kreiranje porudžbine
    }
}
```

**Pozivni lanac u projektu:**
- **order-service** → **user-service** (provera korisnika pre kreiranja porudžbine)
- **payment-service** → **order-service** (provera porudžbine pre plaćanja)

---

## Docker

### Šta koristi projekat?

- **Docker Compose** – orkestracija svih servisa u jednoj mreži
- **Dockerfile** – multi-stage build: Maven build → JRE slika
- ** bridge mreža** – svi kontejneri komuniciraju preko imena servisa (npr. `user-service`, `mysql`)

### Redosled pokretanja (depends_on)

1. **mysql** – baza; ostali čekaju healthcheck
2. **config-server** – konfiguracija; servisi uzimaju DB i Eureka URL
3. **eureka-server** – registar; ostali se registruju
4. **api-gateway** – čeka Eureka
5. **user-service, order-service, payment-service, catalog-service** – čekaju mysql, config-server, eureka
6. **web-app** – čeka api-gateway i ostale servise

### Podešavanja po servisu

**Mikroservisi (npr. user-service):**
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

**Web-app** (poziva servise preko gateway-a):
```yaml
environment:
  GATEWAY_URL: http://api-gateway:8765
  USER_SERVICE_URL: http://api-gateway:8765/user-service
  ORDER_SERVICE_URL: http://api-gateway:8765/order-service
  # ...
```

### Dockerfile (tipičan)

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

- **Stage 1:** Maven build u obrazcu sa JDK 21
- **Stage 2:** Samo JRE + curl (za healthcheck) + JAR

---

## Kako projekat funkcioniše

### Tok zahteva

1. **Klijent (web-app)** šalje zahtev na `http://api-gateway:8765/user-service/api/users`
2. **API Gateway** primajući `/user-service/...` traži `user-service` u Eureka i prosleđuje zahtev na `/api/users`
3. **User-service** obrađuje zahtev i vraća odgovor gateway-u, gateway prosleđuje klijentu

### Međuservisni pozivi (Feign)

- **Order-service** kreće porudžbinu → poziva **user-service** da proveri korisnika
- **Payment-service** plaća porudžbinu → poziva **order-service** da proveri porudžbinu

Ovi pozivi idu **direktno** (user-service:8003, order-service:8001), jer su svi u istoj Docker mreži. Web-app koristi **gateway**, jer pristupa izvana preko jedne tačke.

### Config Server

- Konfiguracija je u `config-repo/` (per-servis fajlovi: `user-service.properties`, `order-service.properties`, itd.)
- Servisi sa `SPRING_PROFILES_ACTIVE=docker` uzimaju konfiguraciju sa Config Servera
- U konfiguraciji su npr. DB URL, Eureka URL, Feign URL-i (za Docker imena kontejnera)

### Baza podataka

- Jedan MySQL (`cafeShop`) – deljen od strane user-service, order-service, payment-service, catalog-service
- `init-db/init.sql` – inicijalni podaci (tabele, seed podaci)
- Svaki servis ima svoje tabele; JPA `ddl-auto=update` ažurira šemu

---

## Pokretanje

### Lokalno (bez Docker-a)

1. Pokreni **MySQL** (port 3306, baza `cafeShop`, root/123)
2. Pokreni **config-server** (8888)
3. Pokreni **eureka-server** (8761)
4. Pokreni **api-gateway** (8765)
5. Pokreni mikroservise: **user-service**, **order-service**, **payment-service**, **catalog-service**
6. Pokreni **web-app** (9000)

U `application.properties` za lokalno korišćenje koriste se `localhost` za Eureka i Config Server.

### Docker Compose

```bash
docker-compose up -d
```

Svi servisi se pokreću u definisanom redosledu. Aplikacija je dostupna na:

- **Web aplikacija:** http://localhost:9000
- **Eureka Dashboard:** http://localhost:8761
- **API Gateway:** http://localhost:8765

**Build i ponovno pokretanje:**
```bash
docker-compose build --no-cache
docker-compose up -d
```
