CREATE SCHEMA IF NOT EXISTS `security` DEFAULT CHARACTER SET utf8mb4;

CREATE TABLE IF NOT EXISTS `security`.`security_users`
(
    `ID`                BIGINT UNSIGNED                                                      NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `USER_NAME`         VARCHAR(320)                                                         NOT NULL DEFAULT 'NONE' COMMENT 'Unique login username; NONE when not set',
    `EMAIL`             VARCHAR(512)                                                                  DEFAULT NULL COMMENT 'Email related to this owner.',
    `DIAL_CODE`         SMALLINT                                                                      DEFAULT 91 COMMENT 'Dial code of the phone number this owner has.',
    `PHONE_NUMBER`      CHAR(15)                                                                      DEFAULT NULL COMMENT 'Phone number related to this owner.',
    `FIRST_NAME`        VARCHAR(128)                                                                  DEFAULT NULL COMMENT 'First name',
    `LAST_NAME`         VARCHAR(128)                                                                  DEFAULT NULL COMMENT 'Last name',
    `MIDDLE_NAME`       VARCHAR(128)                                                                  DEFAULT NULL COMMENT 'Middle name',
    `LOCALE_CODE`       VARCHAR(10)                                                                   DEFAULT NULL COMMENT 'Locale code e.g. en_US',
    `PASSWORD`          VARCHAR(512)                                                         NOT NULL COMMENT 'BCrypt-hashed password',
    `PASSWORD_HASHED`   BOOLEAN                                                                       DEFAULT TRUE COMMENT 'TRUE if password is BCrypt-hashed',
    `USER_STATUS_CODE`  ENUM ('ACTIVE', 'INACTIVE', 'DELETED', 'LOCKED', 'PASSWORD_EXPIRED') NOT NULL DEFAULT 'ACTIVE' COMMENT 'Current account status',
    `NO_FAILED_ATTEMPT` SMALLINT                                                                      DEFAULT 0 COMMENT 'Consecutive failed login attempts',
    `CREATED_BY`        BIGINT UNSIGNED                                                               DEFAULT NULL COMMENT 'ID of the user who created this row',
    `CREATED_AT`        TIMESTAMP                                                            NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Time when this row is created',
    `UPDATED_BY`        BIGINT UNSIGNED                                                               DEFAULT NULL COMMENT 'ID of the user who updated this row',
    `UPDATED_AT`        TIMESTAMP                                                            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Time when this row is updated',
    PRIMARY KEY (`ID`),
    UNIQUE KEY `UK_SECURITY_USERS_USER_NAME` (`USER_NAME`),
    UNIQUE KEY `UK_SECURITY_USERS_EMAIL` (`EMAIL`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `security`.`security_authorities`
(
    `ID`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `NAME`       VARCHAR(50)     NOT NULL COMMENT 'Authority name e.g. ROLE_ADMIN',
    `CREATED_BY` BIGINT UNSIGNED          DEFAULT NULL COMMENT 'ID of the user who created this row',
    `CREATED_AT` TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Time when this row is created',
    `UPDATED_BY` BIGINT UNSIGNED          DEFAULT NULL COMMENT 'ID of the user who updated this row',
    `UPDATED_AT` TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Time when this row is updated',
    PRIMARY KEY (`ID`),
    UNIQUE KEY `UK_SECURITY_AUTHORITIES_NAME` (`NAME`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `security`.`security_user_authorities`
(
    `ID`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `USER_ID`      BIGINT UNSIGNED NOT NULL COMMENT 'FK to security_users',
    `AUTHORITY_ID` BIGINT UNSIGNED NOT NULL COMMENT 'FK to security_authorities',
    `CREATED_BY`   BIGINT UNSIGNED          DEFAULT NULL COMMENT 'ID of the user who created this row',
    `CREATED_AT`   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Time when this row is created',
    `UPDATED_BY`   BIGINT UNSIGNED          DEFAULT NULL COMMENT 'ID of the user who updated this row',
    `UPDATED_AT`   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Time when this row is updated',
    PRIMARY KEY (`ID`),
    UNIQUE KEY `UK1_USER_AUTHORITIES_USER_ID_AUTHORITY_ID` (`USER_ID`, `AUTHORITY_ID`),
    CONSTRAINT `FK1_USER_AUTHORITIES_USER_ID_USERS_ID` FOREIGN KEY (`USER_ID`) REFERENCES `security`.`security_users` (`ID`),
    CONSTRAINT `FK2_USER_AUTHORITIES_AUTHORITY_ID_AUTHORITIES_ID` FOREIGN KEY (`AUTHORITY_ID`) REFERENCES `security`.`security_authorities` (`ID`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
