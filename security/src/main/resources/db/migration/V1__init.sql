CREATE SCHEMA IF NOT EXISTS `security` DEFAULT CHARACTER SET utf8mb4;

-- Client type lookup — must exist before security_clients (FK reference)
CREATE TABLE IF NOT EXISTS `security`.`security_client_type`
(
    `ID` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `CODE` CHAR(4) NOT NULL COMMENT 'Short code e.g. SYS, BUS',
    `TYPE` VARCHAR(256) NOT NULL COMMENT 'Display name of the type',
    `DESCRIPTION` TEXT DEFAULT NULL COMMENT 'Description of the client type',
    `CREATED_BY` BIGINT UNSIGNED DEFAULT NULL COMMENT 'ID of the user who created this row',
    `CREATED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Time when this row is created',
    `UPDATED_BY` BIGINT UNSIGNED DEFAULT NULL COMMENT 'ID of the user who updated this row',
    `UPDATED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Time when this row is updated',
    PRIMARY KEY (`ID`),
    UNIQUE KEY `UK1_CLIENT_TYPE_CODE` (`CODE`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

INSERT IGNORE INTO `security`.`security_client_type` (`CODE`, `TYPE`, `DESCRIPTION`)
VALUES ('SYS', 'System',
        'System client to manage system. Primarily only one client of this type will be created to manage the entire system.'),
    ('BUS', 'Business',
     'Business client is for the clients who wants to create a business, may it be a business partner (marketing agency) or an industry player (real estate developer, bank or any business).');

-- Clients must be created first — users, roles, permissions FK to it
-- Hierarchy: SYSTEM (level 0) → Region/Franchise (level 1) → Location/Studio (level 2)
-- LEVEL_N_CLIENT_ID = ancestor at depth N, or own ID if this client IS at depth N, NULL if N/A
-- Query all clients under a region: WHERE LEVEL_1_CLIENT_ID = :regionId
CREATE TABLE IF NOT EXISTS `security`.`security_clients`
(
    `ID` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `TYPE_CODE` CHAR(4) NOT NULL COMMENT 'FK to security_client_type',
    `NAME` VARCHAR(255) NOT NULL COMMENT 'Organisation display name',
    `CODE` VARCHAR(100) NOT NULL COMMENT 'Unique short code e.g. GYM_DOWNTOWN',
    `DESCRIPTION` VARCHAR(512) DEFAULT NULL COMMENT 'Optional description',
    `ACTIVE` BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Whether client is active',
    `LOCALE_CODE` VARCHAR(10) DEFAULT 'en-US' COMMENT 'Client default locale',
    `STATUS_CODE` ENUM ('ACTIVE', 'INACTIVE', 'DELETED', 'LOCKED') NOT NULL DEFAULT 'ACTIVE' COMMENT 'Status of the client',
    `CLIENT_LEVEL` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Depth in hierarchy: 0=SYSTEM, 1=Region/Franchise, 2=Location/Studio',
    `MANAGING_CLIENT_ID` BIGINT UNSIGNED DEFAULT NULL COMMENT 'Direct parent client; NULL for SYSTEM root',
    `LEVEL_0_CLIENT_ID` BIGINT UNSIGNED DEFAULT NULL COMMENT 'Ancestor at level 0 (SYSTEM); NULL for SYSTEM itself',
    `LEVEL_1_CLIENT_ID` BIGINT UNSIGNED DEFAULT NULL COMMENT 'Ancestor at level 1, or own ID if this is a level-1 client',
    `LEVEL_2_CLIENT_ID` BIGINT UNSIGNED DEFAULT NULL COMMENT 'Ancestor at level 2, or own ID if this is a level-2 client',
    `CREATED_BY` BIGINT UNSIGNED DEFAULT NULL COMMENT 'ID of the user who created this row',
    `CREATED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Time when this row is created',
    `UPDATED_BY` BIGINT UNSIGNED DEFAULT NULL COMMENT 'ID of the user who updated this row',
    `UPDATED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Time when this row is updated',
    PRIMARY KEY (`ID`),
    UNIQUE KEY `UK_SECURITY_CLIENTS_CODE` (`CODE`),
    CONSTRAINT `FK1_CLIENTS_TYPE_CODE` FOREIGN KEY (`TYPE_CODE`) REFERENCES `security`.`security_client_type` (`CODE`),
    CONSTRAINT `FK2_CLIENTS_MANAGING_CLIENT_ID` FOREIGN KEY (`MANAGING_CLIENT_ID`) REFERENCES `security`.`security_clients` (`ID`),
    CONSTRAINT `FK3_CLIENTS_LEVEL_0_CLIENT_ID` FOREIGN KEY (`LEVEL_0_CLIENT_ID`) REFERENCES `security`.`security_clients` (`ID`),
    CONSTRAINT `FK4_CLIENTS_LEVEL_1_CLIENT_ID` FOREIGN KEY (`LEVEL_1_CLIENT_ID`) REFERENCES `security`.`security_clients` (`ID`),
    CONSTRAINT `FK5_CLIENTS_LEVEL_2_CLIENT_ID` FOREIGN KEY (`LEVEL_2_CLIENT_ID`) REFERENCES `security`.`security_clients` (`ID`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

INSERT IGNORE INTO `security`.`security_clients` (`TYPE_CODE`, `CODE`, `NAME`)
VALUES ('SYS', 'SYSTEM', 'System Internal');

CREATE TABLE IF NOT EXISTS `security`.`security_users`
(
    `ID` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `CLIENT_ID` BIGINT UNSIGNED DEFAULT NULL COMMENT 'FK to security_clients; NULL = platform-level user',
    `USER_NAME` VARCHAR(320) NOT NULL DEFAULT 'NONE' COMMENT 'Unique login username; NONE when not set',
    `EMAIL` VARCHAR(512) DEFAULT NULL COMMENT 'Email address',
    `DIAL_CODE` SMALLINT DEFAULT 91 COMMENT 'Dial code of the phone number',
    `PHONE_NUMBER` CHAR(15) DEFAULT NULL COMMENT 'Phone number',
    `FIRST_NAME` VARCHAR(128) DEFAULT NULL COMMENT 'First name',
    `LAST_NAME` VARCHAR(128) DEFAULT NULL COMMENT 'Last name',
    `MIDDLE_NAME` VARCHAR(128) DEFAULT NULL COMMENT 'Middle name',
    `LOCALE_CODE` VARCHAR(10) DEFAULT NULL COMMENT 'Locale code e.g. en_US',
    `PASSWORD` VARCHAR(512) NOT NULL COMMENT 'BCrypt-hashed password',
    `PASSWORD_HASHED` BOOLEAN DEFAULT TRUE COMMENT 'TRUE if password is BCrypt-hashed',
    `USER_STATUS_CODE` ENUM ('ACTIVE', 'INACTIVE', 'DELETED', 'LOCKED', 'PASSWORD_EXPIRED') NOT NULL DEFAULT 'ACTIVE' COMMENT 'Current account status',
    `NO_FAILED_ATTEMPT` SMALLINT DEFAULT 0 COMMENT 'Consecutive failed login attempts',
    `CREATED_BY` BIGINT UNSIGNED DEFAULT NULL COMMENT 'ID of the user who created this row',
    `CREATED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Time when this row is created',
    `UPDATED_BY` BIGINT UNSIGNED DEFAULT NULL COMMENT 'ID of the user who updated this row',
    `UPDATED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Time when this row is updated',
    PRIMARY KEY (`ID`),
    UNIQUE KEY `UK_SECURITY_USERS_USER_NAME` (`USER_NAME`),
    UNIQUE KEY `UK_SECURITY_USERS_EMAIL` (`EMAIL`),
    CONSTRAINT `FK1_USERS_CLIENT_ID` FOREIGN KEY (`CLIENT_ID`) REFERENCES `security`.`security_clients` (`ID`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `security`.`security_permissions`
(
    `ID` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `CLIENT_ID` BIGINT UNSIGNED DEFAULT NULL COMMENT 'FK to security_clients; NULL = platform-wide permission',
    `CODE` VARCHAR(100) NOT NULL COMMENT 'Permission code e.g. USER_CREATE',
    `DESCRIPTION` VARCHAR(255) DEFAULT NULL COMMENT 'Human-readable description',
    `CREATED_BY` BIGINT UNSIGNED DEFAULT NULL COMMENT 'ID of the user who created this row',
    `CREATED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Time when this row is created',
    `UPDATED_BY` BIGINT UNSIGNED DEFAULT NULL COMMENT 'ID of the user who updated this row',
    `UPDATED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Time when this row is updated',
    PRIMARY KEY (`ID`),
    UNIQUE KEY `UK_SECURITY_PERMISSIONS_CODE` (`CODE`),
    CONSTRAINT `FK1_PERMISSIONS_CLIENT_ID` FOREIGN KEY (`CLIENT_ID`) REFERENCES `security`.`security_clients` (`ID`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `security`.`security_roles`
(
    `ID` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `CLIENT_ID` BIGINT UNSIGNED DEFAULT NULL COMMENT 'FK to security_clients; NULL = platform-wide role',
    `NAME` VARCHAR(100) NOT NULL COMMENT 'Role name e.g. ADMIN',
    `DESCRIPTION` VARCHAR(255) DEFAULT NULL COMMENT 'Human-readable description',
    `CREATED_BY` BIGINT UNSIGNED DEFAULT NULL COMMENT 'ID of the user who created this row',
    `CREATED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Time when this row is created',
    `UPDATED_BY` BIGINT UNSIGNED DEFAULT NULL COMMENT 'ID of the user who updated this row',
    `UPDATED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Time when this row is updated',
    PRIMARY KEY (`ID`),
    UNIQUE KEY `UK_SECURITY_ROLES_NAME` (`NAME`),
    CONSTRAINT `FK1_ROLES_CLIENT_ID` FOREIGN KEY (`CLIENT_ID`) REFERENCES `security`.`security_clients` (`ID`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `security`.`security_role_permissions`
(
    `ID` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `ROLE_ID` BIGINT UNSIGNED NOT NULL COMMENT 'FK to security_roles',
    `PERMISSION_ID` BIGINT UNSIGNED NOT NULL COMMENT 'FK to security_permissions',
    `CREATED_BY` BIGINT UNSIGNED DEFAULT NULL COMMENT 'ID of the user who created this row',
    `CREATED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Time when this row is created',
    `UPDATED_BY` BIGINT UNSIGNED DEFAULT NULL COMMENT 'ID of the user who updated this row',
    `UPDATED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Time when this row is updated',
    PRIMARY KEY (`ID`),
    UNIQUE KEY `UK_SECURITY_ROLE_PERMISSIONS` (`ROLE_ID`, `PERMISSION_ID`),
    CONSTRAINT `FK1_ROLE_PERMISSIONS_ROLE_ID` FOREIGN KEY (`ROLE_ID`) REFERENCES `security`.`security_roles` (`ID`),
    CONSTRAINT `FK2_ROLE_PERMISSIONS_PERMISSION_ID` FOREIGN KEY (`PERMISSION_ID`) REFERENCES `security`.`security_permissions` (`ID`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `security`.`security_user_roles`
(
    `ID` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `USER_ID` BIGINT UNSIGNED NOT NULL COMMENT 'FK to security_users',
    `ROLE_ID` BIGINT UNSIGNED NOT NULL COMMENT 'FK to security_roles',
    `CREATED_BY` BIGINT UNSIGNED DEFAULT NULL COMMENT 'ID of the user who created this row',
    `CREATED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Time when this row is created',
    `UPDATED_BY` BIGINT UNSIGNED DEFAULT NULL COMMENT 'ID of the user who updated this row',
    `UPDATED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Time when this row is updated',
    PRIMARY KEY (`ID`),
    UNIQUE KEY `UK_SECURITY_USER_ROLES` (`USER_ID`, `ROLE_ID`),
    CONSTRAINT `FK1_USER_ROLES_USER_ID` FOREIGN KEY (`USER_ID`) REFERENCES `security`.`security_users` (`ID`),
    CONSTRAINT `FK2_USER_ROLES_ROLE_ID` FOREIGN KEY (`ROLE_ID`) REFERENCES `security`.`security_roles` (`ID`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
