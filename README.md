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

---

## GitHub Actions - Build & Security

### Overview

Automated CI/CD pipeline that runs **build validation** and **security scanning** on push and pull requests, ensuring
code quality and security before deployment.

### Pipeline Jobs

#### 🔨 Build Check

- **Java 21** with Maven dependency caching
- **Clean package build** (tests skipped for speed)
- **Validates compilation** before deployment

#### 🔐 Security Check

- **Snyk CLI** for dependency and code scanning
- **Medium+ severity threshold**
- **Continuous monitoring** on develop branch
- **Non-blocking** - won't fail builds on findings

### Setup

1. Add `SNYK_TOKEN` secret to repository settings
2. Pipeline runs automatically on:
    - Push to `develop`
    - Pull requests to `master`

---

## Deployment

### Volume Management

This application uses persistent volumes for logs and media files. The volumes are managed using bind mounts to ensure
easy access and backup on the host system.

#### Volume Locations

- **Logs**: `/app/logs` (container) → `/var/dokploy/blog-api/logs` (host)
- **Resources**: `/app/resources` (container) → `/var/dokploy/blog-api/resources` (host)

#### Setting Up Volumes on VPS

Before deploying, create the necessary directories on your VPS:

```bash
# Create directories
sudo mkdir -p /var/dokploy/blog-api/logs
sudo mkdir -p /var/dokploy/blog-api/resources

# Set proper permissions for www-data user (UID 33)
sudo chown -R 33:33 /var/dokploy/blog-api/resources
sudo chmod -R 755 /var/dokploy/blog-api/resources

# Logs directory permissions
sudo chmod -R 755 /var/dokploy/blog-api/logs

# Set proper permissions (if needed)
chmod -R 755 ./resources
```

#### Configuring Mounts in Dokploy

In Dokploy UI, navigate to your application's **Volumes / Mounts** section and add:

**Mount #1 - Logs:**

- Mount Type: `Bind Mount`
- Host Path: `/var/dokploy/blog-api/logs`
- Mount Path (container): `/app/logs`

**Mount #2 - Resources:**

- Mount Type: `Bind Mount`
- Host Path: `/var/dokploy/blog-api/resources`
- Mount Path (container): `/app/resources`

#### Backup and Restore

**Create a backup:**

```bash
# Full backup
tar -czf blog-backup-$(date +%Y%m%d).tar.gz /var/dokploy/blog-api/

# Resources only
tar -czf blog-resources-$(date +%Y%m%d).tar.gz /var/dokploy/blog-api/resources/

# Logs only
tar -czf blog-logs-$(date +%Y%m%d).tar.gz /var/dokploy/blog-api/logs/
```

**Restore from backup:**

```bash
# Stop the container first (via Dokploy UI)

# Restore files
tar -xzf blog-backup-20251023.tar.gz -C /

# Restart the container (via Dokploy UI)
```

#### Accessing Files

Since bind mounts are used, you can directly access files on the VPS:

```bash
# List resource files
ls -la /var/dokploy/blog-api/resources/

# View recent logs
tail -f /var/dokploy/blog-api/logs/app.log

# Check disk usage
du -sh /var/dokploy/blog-api/*
```

### Dokploy Build Configuration

**Build Type:** Dockerfile  
**Docker File:** `./Dockerfile`  
**Docker Context Path:** `.` (default)  
**Docker Build Stage:** (empty - builds last stage)

### Environment Variables

Create a `.env` file based on `.env.example` and configure all required environment variables before deployment.

### Notes

- The Dockerfile creates mount point directories (`/app/logs` and `/app/resources`) but does **not** declare them as
  Docker-managed volumes
- Volume management is handled at deployment time via Dokploy's bind mount configuration
- This approach ensures data is stored in accessible locations on the host for easy backup and management

---

## 🔀 Reverse Proxy Setup

The application uses **nginx** as a reverse proxy and **supervisor** for process management.

### Architecture

- **nginx** (port 80) → serves static media files & proxies API requests
- **Spring Boot** (port 8080) → handles application logic
- **supervisor** → manages both processes in a single container

### Configuration Files

- [`nginx.conf`](./nginx.conf) - Nginx reverse proxy configuration
- [`supervisord.conf`](./supervisord.conf) - Process supervisor configuration

This setup allows efficient static file serving while keeping the application containerized.

---

## 🗄️ Database

### Migrations

Database schema is managed by **Flyway**. Migrations are located in `src/main/resources/db/migrations/`.

**Naming convention:** `V{version}__{description}.sql`

Example:

- `V1__initial_schema.sql`
- `V2__add_media_tables.sql`

**Running migrations:**
Flyway runs automatically on application startup. To run manually:

```bash
mvn flyway:migrate
```
