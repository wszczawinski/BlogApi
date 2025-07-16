# 📝 Personal Blog API

This is a backend for a blog that no one asked for — but that’s not the point. It’s an overengineered playground to
break stuff, fix it, and pretend you're doing real work. Built with **Spring Boot**, **Java**, **Docker** and
**MySQL**, this project is my sandbox for diving deep into Spring Boot app setup, managing dependencies,
configuring Spring Security, and mastering Docker Compose — all under the noble banner of 'learning by shipping,'

```
   (╯°□°）╯︵ ┻━┻
Overengineered? Absolutely.
Useful? Debatable.
Fun? Always.
```

---

## 🚀 Features

- User authentication
- CRUD operations for blog posts
- Tag and category management
- Secure API with Spring Security
- DTO mapping with MapStruct
- Validation with Hibernate Validator
- Database versioning and migration with Flyway

---

## 🧰 Tech Stack

- **Spring Boot** (Web, JPA, Security, Validation)
- **Java 21**
- **MySQL** (runtime), **H2** (for testing)
- **MapStruct** for DTO mapping
- **Lombok** for boilerplate reduction
- **Flyway** for database migrations
- **Maven** as the build tool
- **Docker Compose** for local environment setup
- **MySQL Connector**
- **Adminer** (for quick DB inspection)

---

## 🧠 Domain Model

```
+---------------------+       +----------------------+         +---------------------+
|       User          |       |         Post         |         |        Media        |
+---------------------+       +----------------------+         +---------------------+
| id: UUID            |<----->| id: UUID             |<------->| id: Long            |
| email: String       |       | title: String        |         | name: String        |
| name: String        |       | slug: String         |         | short: String       |
| password: String    |       | content: String      |         | shortSlug: String   |
| createdAt: DateTime |       | status: PostStatus   |         | folder: String      |
+---------------------+       | readingTime: Int     |         | type: Int           |
                              | createdAt: DateTime  |         | status: Int         |
                              | updatedAt: DateTime  |         | updatedAt: Date     |
                              +----------+-----------+         +---------------------+
                                         |                               |
                    +--------------------+----+                          |
                    |                         |                          v
           +---------------+         +-----------------+       +--------------------+
           |     Tag       |         |   Category      |       |    MediaFile       |
           +---------------+         +-----------------+       +--------------------+
           | id: UUID      |         | id: UUID        |       | id: Long           |
           | name: String  |         | name: String    |       | mediaId: Long      |
           +---------------+         +-----------------+       | file: String       |
                                                               | short: String      |
                                                               | size: Int          |
                                                               | position: Int      |
                                                               +--------------------+
```

**Relationships:**
- A `User` can author multiple `Posts` (1:N)
- A `Post` can have multiple `Tags` (M:N via post_tags)
- A `Post` belongs to one `Category` (N:1)
- A `Post` can be associated with multiple `Media` galleries (M:N via post_media)
- A `Media` gallery can contain multiple `MediaFile` entries (1:N)
- A `Media` gallery can be associated with multiple `Posts` (M:N via post_media)

---

## 🗺️ Roadmap

### Media & Gallery Features
- [x] **Media Domain Model** - Added media tables to support image galleries
- [x] **Database Migration** - Created V2 migration for media, media_file, and post_media tables
- [ ] **Gallery API** - Create endpoints for managing media galleries attached to posts
- [ ] **Post Thumbnails** - Enable adding thumbnail pictures for blog posts
- [ ] **Inline Images** - Support for adding pictures inside post content/text

---



## ⚙️ Getting Started

### Prerequisites

- **Java 21**
- **Maven 3.x** (or use the included mvnw)
- **Docker** (or MySQL 8+)

### Running the Application

1. **Start the database using Docker Compose**

   Use the provided [`docker-compose.yml`](./docker-compose.yml) file to start a local MySQL container and Adminer UI
   for development:

   ```bash
   docker-compose up
   ```

2. **Application Configuration**

   The project uses **Spring profiles** to manage environment-specific configuration.

    - The default profile is set to `dev` in [`application.properties`](./src/main/resources/application.properties):

      ```properties
      spring.profiles.active=dev
      ```

    - The `application-dev.properties` file is included in the repository and is preconfigured to match the settings
      used in the Docker Compose setup (e.g., database connection values).

    - To support other environments (e.g., `staging`, `production`), create the corresponding
      `application-<profile>.properties` file with appropriate values. Then either:
        - Update the `spring.profiles.active` property in `application.properties`, or
        - Override it at runtime using the `--spring.profiles.active=<profile>` flag.

3. **Build and Run the App**

   ```bash
   mvn spring-boot:run
   ```

   Flyway will automatically apply any pending migrations on startup.

4. **Running Tests**

   ```bash
   mvn test
   ```
