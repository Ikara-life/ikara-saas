CREATE TABLE IF NOT EXISTS `security`.`security_permissions`
(
    `ID`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `CODE`        VARCHAR(100)    NOT NULL COMMENT 'Permission code e.g. Application_CREATE',
    `DESCRIPTION` VARCHAR(255)             DEFAULT NULL COMMENT 'Human-readable description',
    `CREATED_BY`  BIGINT UNSIGNED          DEFAULT NULL COMMENT 'ID of the user who created this row',
    `CREATED_AT`  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Time when this row is created',
    `UPDATED_BY`  BIGINT UNSIGNED          DEFAULT NULL COMMENT 'ID of the user who updated this row',
    `UPDATED_AT`  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Time when this row is updated',
    PRIMARY KEY (`ID`),
    UNIQUE KEY `UK_SECURITY_PERMISSIONS_CODE` (`CODE`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `security`.`security_roles`
(
    `ID`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `NAME`        VARCHAR(100)    NOT NULL COMMENT 'Role name e.g. ADMIN',
    `DESCRIPTION` VARCHAR(255)             DEFAULT NULL COMMENT 'Human-readable description',
    `CREATED_BY`  BIGINT UNSIGNED          DEFAULT NULL COMMENT 'ID of the user who created this row',
    `CREATED_AT`  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Time when this row is created',
    `UPDATED_BY`  BIGINT UNSIGNED          DEFAULT NULL COMMENT 'ID of the user who updated this row',
    `UPDATED_AT`  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Time when this row is updated',
    PRIMARY KEY (`ID`),
    UNIQUE KEY `UK_SECURITY_ROLES_NAME` (`NAME`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `security`.`security_role_permissions`
(
    `ID`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `ROLE_ID`       BIGINT UNSIGNED NOT NULL COMMENT 'FK to security_roles',
    `PERMISSION_ID` BIGINT UNSIGNED NOT NULL COMMENT 'FK to security_permissions',
    `CREATED_BY`    BIGINT UNSIGNED          DEFAULT NULL COMMENT 'ID of the user who created this row',
    `CREATED_AT`    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Time when this row is created',
    `UPDATED_BY`    BIGINT UNSIGNED          DEFAULT NULL COMMENT 'ID of the user who updated this row',
    `UPDATED_AT`    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Time when this row is updated',
    PRIMARY KEY (`ID`),
    UNIQUE KEY `UK_SECURITY_ROLE_PERMISSIONS` (`ROLE_ID`, `PERMISSION_ID`),
    CONSTRAINT `FK1_ROLE_PERMISSIONS_ROLE_ID` FOREIGN KEY (`ROLE_ID`) REFERENCES `security`.`security_roles` (`ID`),
    CONSTRAINT `FK2_ROLE_PERMISSIONS_PERMISSION_ID` FOREIGN KEY (`PERMISSION_ID`) REFERENCES `security`.`security_permissions` (`ID`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `security`.`security_user_roles`
(
    `ID`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `USER_ID`    BIGINT UNSIGNED NOT NULL COMMENT 'FK to security_users',
    `ROLE_ID`    BIGINT UNSIGNED NOT NULL COMMENT 'FK to security_roles',
    `CREATED_BY` BIGINT UNSIGNED          DEFAULT NULL COMMENT 'ID of the user who created this row',
    `CREATED_AT` TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Time when this row is created',
    `UPDATED_BY` BIGINT UNSIGNED          DEFAULT NULL COMMENT 'ID of the user who updated this row',
    `UPDATED_AT` TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Time when this row is updated',
    PRIMARY KEY (`ID`),
    UNIQUE KEY `UK_SECURITY_USER_ROLES` (`USER_ID`, `ROLE_ID`),
    CONSTRAINT `FK1_USER_ROLES_USER_ID` FOREIGN KEY (`USER_ID`) REFERENCES `security`.`security_users` (`ID`),
    CONSTRAINT `FK2_USER_ROLES_ROLE_ID` FOREIGN KEY (`ROLE_ID`) REFERENCES `security`.`security_roles` (`ID`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
