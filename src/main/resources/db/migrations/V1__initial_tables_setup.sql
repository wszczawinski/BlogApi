CREATE TABLE users
(
    id         BINARY(16) PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL COMMENT 'Hashed password using bcrypt',
    name       VARCHAR(255) NOT NULL,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,


    CONSTRAINT chk_users_email_format CHECK (email REGEXP '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT chk_users_name_length CHECK (CHAR_LENGTH(name) >= 2)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


CREATE TABLE categories
(
    id   BINARY(16) PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,


    CONSTRAINT chk_categories_name_length CHECK (CHAR_LENGTH(name) >= 2)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


CREATE TABLE tags
(
    id   BINARY(16) PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,


    CONSTRAINT chk_tags_name_length CHECK (CHAR_LENGTH(name) >= 2)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


CREATE TABLE posts
(
    id           BINARY(16) PRIMARY KEY,
    title        VARCHAR(255) NOT NULL UNIQUE,
    slug         VARCHAR(255) NOT NULL UNIQUE,
    content      MEDIUMTEXT   NOT NULL,
    status       VARCHAR(50)  NOT NULL,
    reading_time INTEGER      NOT NULL,
    author_id    BINARY(16)   NOT NULL,
    category_id  BINARY(16)   NOT NULL,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,


    CONSTRAINT fk_posts_author FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_posts_category FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE RESTRICT,


    CONSTRAINT chk_posts_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
    CONSTRAINT chk_posts_reading_time_positive CHECK (reading_time > 0),
    CONSTRAINT chk_posts_title_length CHECK (CHAR_LENGTH(title) >= 5),
    CONSTRAINT chk_posts_slug_format CHECK (slug REGEXP '^[a-z0-9]+(-[a-z0-9]+)*$')
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


CREATE TABLE post_tags
(
    post_id    BINARY(16) NOT NULL,
    tag_id     BINARY(16) NOT NULL,
    created_at DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (post_id, tag_id),
    CONSTRAINT fk_post_tags_post FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
    CONSTRAINT fk_post_tags_tag FOREIGN KEY (tag_id) REFERENCES tags (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


CREATE INDEX idx_posts_author_id ON posts (author_id);
CREATE INDEX idx_posts_category_id ON posts (category_id);
CREATE INDEX idx_posts_status ON posts (status);
CREATE INDEX idx_posts_created_at ON posts (created_at DESC);
CREATE INDEX idx_posts_slug ON posts (slug);

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_categories_name ON categories (name);
CREATE INDEX idx_tags_name ON tags (name);
CREATE INDEX idx_post_tags_created_at ON post_tags (created_at);
