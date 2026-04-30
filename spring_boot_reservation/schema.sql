-- MariaDB Database Schema Creation Script
-- Generated based on DATABASE_SCHEMA.md

-- 1. Create Database (Optional)
-- CREATE DATABASE IF NOT EXISTS reservation_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- USE reservation_db;

-- 2. users table
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL,
    `email` VARCHAR(100) NOT NULL,
    `provider` VARCHAR(50) DEFAULT NULL,         -- 소셜 로그인 제공자 (google 등)
    `provider_id` VARCHAR(255) DEFAULT NULL,     -- 소셜 로그인 고유 ID
    `role` ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER', -- 사용자 권한
    `status` ENUM('active', 'inactive', 'suspended', 'blacklisted') DEFAULT 'active',
    `created_at` DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at` DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. matches table
CREATE TABLE IF NOT EXISTS `matches` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(100) NOT NULL,
    `match_date` DATETIME NOT NULL,
    `stadium_name` VARCHAR(50) NOT NULL,
    `status` ENUM('UPCOMING', 'ON_SALE', 'CLOSED', 'CANCELLED') NOT NULL DEFAULT 'UPCOMING',
    `created_at` DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at` DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. seats table
CREATE TABLE IF NOT EXISTS `seats` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `match_id` BIGINT NOT NULL,
    `seat_number` VARCHAR(20) NOT NULL,
    `tier` VARCHAR(20) NOT NULL,
    `price` DECIMAL(10,2) NOT NULL,
    `status` ENUM('AVAILABLE', 'PENDING', 'RESERVED') DEFAULT 'AVAILABLE',
    `version` BIGINT DEFAULT 0,
    `created_at` DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at` DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_match_seat` (`match_id`, `seat_number`),
    CONSTRAINT `fk_seats_match_id` FOREIGN KEY (`match_id`) REFERENCES `matches` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. reservations table
CREATE TABLE IF NOT EXISTS `reservations` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `seat_id` BIGINT NOT NULL,
    `status` ENUM('PENDING', 'CONFIRMED', 'CANCELLED') DEFAULT 'PENDING',
    `created_at` DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at` DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_reservations_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_reservations_seat_id` FOREIGN KEY (`seat_id`) REFERENCES `seats` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
