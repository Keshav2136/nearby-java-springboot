# Nearby — Social Network API

> A production-ready social network backend built with **Spring Boot**, **PostgreSQL**, and **Neo4j**. Demonstrates **polyglot persistence** — using the right database for the right job.

---

## What is this?

This is a portfolio project that simulates the backend of a social network like Twitter or Instagram. It handles:

- **User profiles** (stored in PostgreSQL)
- **Follow / Unfollow relationships** (stored in Neo4j graph database)
- **Friend-of-friend suggestions** (powered by graph traversal)

The key insight: SQL databases slow down on "who follows who" queries as the network grows. A **graph database** (Neo4j) handles these relationships 1000x faster because it is built for connected data.

---

## Architecture

```
┌─────────────────┐
│   Your Browser  │
│   or Terminal   │
└────────┬────────┘
         │ HTTP/REST
┌────────▼────────┐
│  Spring Boot    │  ← Java backend (this code)
│  ├─ REST API    │     (Controllers)
│  ├─ Services    │     (Business logic)
│  ├─ JPA (SQL)   │     (User profiles)
│  └─ Neo4jClient │     (Follow graph)
└────────┬────────┘
    ┌────┴────┐
    ▼         ▼
┌────────┐  ┌──────────┐
│PostgreSQL│  │  Neo4j   │
│(Alwaysdata)│  │(AuraDB)  │
│Users,   │  │Follows,   │
│Profiles │  │Suggestions│
└────────┘  └──────────┘
```

---

## Technology Stack

| Technology | Purpose | Why we use it |
|---|---|---|
| **Java 25** | Programming language | Modern, fast, industry standard |
| **Spring Boot 4.1** | Backend framework | Auto-configuration, REST APIs, database connections |
| **Spring Data JPA** | PostgreSQL ORM | Maps Java objects to SQL tables automatically |
| **PostgreSQL 16** | Relational database | Stores structured data: users, profiles, posts |
| **Neo4j 5** | Graph database | Stores relationships: follows, friend-of-friend paths |
| **Neo4jClient** | Graph query runner | Direct Cypher queries for complex graph logic |
| **Lombok** | Boilerplate reducer | Auto-generates getters/setters/constructors |
| **Gradle** | Build tool | Compiles, tests, and runs the project |

---

## Prerequisites

Before you start, you need:

1. **Java 25** installed
   ```bash
   java -version
   # Should show: openjdk version "25" or higher
   ```

2. **Gradle** (comes with the project via wrapper)

3. **A PostgreSQL database** — You can use:
   - [Alwaysdata](https://www.alwaysdata.com) (free, cloud-hosted) ← **Recommended**
   - Local PostgreSQL
   - Any other PostgreSQL provider

4. **A Neo4j AuraDB account** — Free forever tier:
   - Go to [https://neo4j.com/cloud/aura/](https://neo4j.com/cloud/aura/)
   - Sign up and create an instance
   - Save your **Connection URI** and **Password**

---

## Setup (5 minutes)

### Step 1: Clone the repo

```bash
git clone https://github.com/Keshav2136/nearby-java-springboot.git
cd nearby-java-springboot
```

### Step 2: Configure your databases

```bash
# Copy the example config
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Open `src/main/resources/application.properties` and fill in your real credentials:

```properties
server.port=8081

# PostgreSQL (Alwaysdata or your provider)
spring.datasource.url=jdbc:postgresql://YOUR_HOST:5432/YOUR_DATABASE?sslmode=require
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Neo4j AuraDB
spring.neo4j.uri=neo4j+s://YOUR_INSTANCE.databases.neo4j.io
spring.neo4j.authentication.username=YOUR_USERNAME
spring.neo4j.authentication.password=YOUR_PASSWORD
```

> **Note:** The `application.properties` file is in `.gitignore` so your passwords will never be committed.

### Step 3: Build and run

```bash
# Compile and start the server
./gradlew bootRun
```

Wait for this line in the terminal:
```
Started NearbyApplication in X.XX seconds
```

Your API is live at: **http://localhost:8081**

---

## API Endpoints

### Home
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/` | Check if API is running |

### Users (PostgreSQL)
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/social/users` | Create a new user |
| `GET` | `/users` | List all users |
| `GET` | `/users/{id}` | Get user by ID |

### Social Graph (Neo4j)
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/social/follow?followerId={a}&followingId={b}` | User A follows User B |
| `POST` | `/social/unfollow?followerId={a}&followingId={b}` | User A unfollows User B |
| `GET` | `/social/{userId}/following` | Who does this user follow? |
| `GET` | `/social/{userId}/followers` | Who follows this user? |
| `GET` | `/social/{userId}/suggestions` | Friend-of-friend suggestions |

---

## Testing the API

Open a **new terminal** while the server is running:

### 1. Create users

```bash
curl -X POST http://localhost:8081/social/users \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","email":"alice@test.com"}'

# Note the "id" in the response (e.g., 1)

curl -X POST http://localhost:8081/social/users \
  -H "Content-Type: application/json" \
  -d '{"username":"bob","email":"bob@test.com"}'

# Note the "id" (e.g., 2)

curl -X POST http://localhost:8081/social/users \
  -H "Content-Type: application/json" \
  -d '{"username":"charlie","email":"charlie@test.com"}'

# Note the "id" (e.g., 3)
```

### 2. Build follow relationships

Replace `1`, `2`, `3` with the actual IDs you got above:

```bash
# Alice follows Bob
curl -X POST "http://localhost:8081/social/follow?followerId=1&followingId=2"

# Bob follows Charlie
curl -X POST "http://localhost:8081/social/follow?followerId=2&followingId=3"

# Alice follows Charlie
curl -X POST "http://localhost:8081/social/follow?followerId=1&followingId=3"
```

### 3. Query the graph

```bash
# Who does Alice follow?
curl http://localhost:8081/social/1/following

# Who follows Bob?
curl http://localhost:8081/social/2/followers

# Suggestions for Alice
# (Returns people that Bob follows, but Alice doesn't)
curl http://localhost:8081/social/1/suggestions
```

---

## Project Structure

```
nearby-java-springboot/
├── src/main/java/com/keshav/nearby/
│   ├── NearbyApplication.java          # Entry point
│   ├── controller/
│   │   ├── UserController.java         # User CRUD endpoints
│   │   └── SocialController.java       # Follow/suggestion endpoints
│   ├── entity/
│   │   └── User.java                   # PostgreSQL entity (JPA)
│   ├── model/
│   │   └── UserNode.java               # Neo4j node (Graph)
│   ├── repository/
│   │   └── UserRepository.java         # JPA repository (SQL)
│   └── service/
│       └── SocialService.java          # Business logic + dual writes
├── src/main/resources/
│   ├── application.properties.example  # Template (safe for Git)
│   └── application.properties          # Real config (ignored by Git)
├── build.gradle                        # Dependencies
└── README.md                           # This file
```

---

## How it works under the hood

### Creating a user (Dual Write)

When you call `POST /social/users`:

1. **PostgreSQL**: User row is inserted into the `users` table
2. **Neo4j**: A `(:User)` node is created with the same ID and username
3. Both operations are wrapped in `@Transactional` — if one fails, both roll back

### Following a user (Graph Operation)

When you call `POST /social/follow`:

1. **Neo4j** runs this Cypher query:
   ```cypher
   MATCH (a:User {userId: 1}), (b:User {userId: 2})
   CREATE (a)-[:FOLLOWS]->(b)
   ```
2. A relationship is created instantly — no slow SQL joins needed

### Friend-of-friend suggestions

When you call `GET /social/1/suggestions`:

1. **Neo4j** traverses the graph:
   ```cypher
   MATCH (u:User {userId: 1})-[:FOLLOWS]->(friend)-[:FOLLOWS]->(fof)
   WHERE NOT (u)-[:FOLLOWS]->(fof)
   RETURN fof
   ```
2. Returns people your friends follow, but you don't — the classic "People You May Know" algorithm

---

## Troubleshooting

### Port 8081 already in use

```bash
# Find and kill the process, or change the port in application.properties:
server.port=8082
```

### "Failed to start bean 'webServerStartStop'"

Another app is using the port. Either stop it or change `server.port`.

### Database connection errors

- Check your `application.properties` credentials
- Verify PostgreSQL allows remote connections (Alwaysdata does by default)
- Verify Neo4j AuraDB instance is **Active** (not paused)

### Neo4j NullPointerException

This project now uses `Neo4jClient` (raw Cypher) instead of Spring Data Neo4j repositories to avoid version compatibility issues.

---

## What I Learned

1. **Polyglot Persistence**: Using PostgreSQL for structured data and Neo4j for graph data
2. **Spring Data JPA**: Entity mapping, repositories, and database transactions
3. **Cypher Query Language**: How to traverse graphs efficiently
4. **Neo4jClient**: Direct Cypher execution when repositories are too abstract
5. **Dual-Write Transactions**: Keeping two databases in sync with `@Transactional`
6. **Cloud Database Hosting**: Using managed services (Alwaysdata + AuraDB) for zero-infrastructure development

---

## License

[Portfolio Source License 1.0](LICENSE) — Source available for learning and non-commercial use. Commercial use requires written permission.

---

## Contact

Built by [Keshav](https://github.com/Keshav2136) as a portfolio project to demonstrate backend architecture and polyglot database design.

> *"The right database for the right problem."*
