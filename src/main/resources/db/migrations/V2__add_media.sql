CREATE TABLE media
(
    id         INT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    short      VARCHAR(255) NOT NULL,
    short_slug VARCHAR(120) NOT NULL,
    folder     VARCHAR(255) NOT NULL,
    type       TINYINT      NOT NULL,
    status     TINYINT      NOT NULL,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_media_status (status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE media_file
(
    id       INT AUTO_INCREMENT PRIMARY KEY,
    media_id INT          NOT NULL,
    file     VARCHAR(255) NOT NULL,
    short    VARCHAR(255) NOT NULL,
    size     INT          NOT NULL,
    position INT          NOT NULL,

    INDEX idx_media_file_media_id (media_id),

    CONSTRAINT fk_media_file_media_id
        FOREIGN KEY (media_id) REFERENCES media (id)
            ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE post_media
(
    post_id  BINARY(16) NOT NULL,
    media_id INT        NOT NULL,

    PRIMARY KEY (post_id, media_id),
    INDEX idx_post_media_post_id (post_id),

    CONSTRAINT fk_post_media_post_id
        FOREIGN KEY (post_id) REFERENCES posts (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_post_media_media_id
        FOREIGN KEY (media_id) REFERENCES media (id)
            ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;