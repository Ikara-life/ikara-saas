-- =============================================================
-- CLIENT SEED DATA — development only
-- Run against: security schema (MySQL)
-- All passwords are BCrypt(10) of "Password@123"
-- Hash: $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi
-- Regenerate: new BCryptPasswordEncoder(10).encode("Password@123")
-- =============================================================

-- NOTE: security_client_type seed rows (SYS, BUS) and the SYSTEM client
-- are inserted by V1__init.sql — do not re-insert here.

-- ── 1. Clients (insert in level order — parent must exist before child)

-- Level 1: Ikara Platform (internal — lives under System)
INSERT INTO `security`.`security_clients`
    (`TYPE_CODE`, `NAME`, `CODE`, `DESCRIPTION`, `ACTIVE`, `CLIENT_LEVEL`,
     `MANAGING_CLIENT_ID`, `LEVEL_0_CLIENT_ID`, `LEVEL_1_CLIENT_ID`, `LEVEL_2_CLIENT_ID`)
VALUES
    ('SYS', 'Ikara Platform', 'IKARA_PLATFORM', 'Internal platform client for system accounts', TRUE, 1,
     (SELECT `ID` FROM `security`.`security_clients` WHERE `CODE` = 'SYSTEM'),
     (SELECT `ID` FROM `security`.`security_clients` WHERE `CODE` = 'SYSTEM'),
     NULL, NULL);

UPDATE `security`.`security_clients`
SET `LEVEL_1_CLIENT_ID` = `ID`
WHERE `CODE` = 'IKARA_PLATFORM';

-- Level 2: Demo Studio (sample gym/studio — lives under Ikara Platform)
INSERT INTO `security`.`security_clients`
    (`TYPE_CODE`, `NAME`, `CODE`, `DESCRIPTION`, `ACTIVE`, `CLIENT_LEVEL`,
     `MANAGING_CLIENT_ID`, `LEVEL_0_CLIENT_ID`, `LEVEL_1_CLIENT_ID`, `LEVEL_2_CLIENT_ID`)
VALUES
    ('BUS', 'Demo Studio', 'DEMO_STUDIO', 'Demo client for testing', TRUE, 2,
     (SELECT `ID` FROM `security`.`security_clients` WHERE `CODE` = 'IKARA_PLATFORM'),
     (SELECT `ID` FROM `security`.`security_clients` WHERE `CODE` = 'SYSTEM'),
     (SELECT `ID` FROM `security`.`security_clients` WHERE `CODE` = 'IKARA_PLATFORM'),
     NULL);

UPDATE `security`.`security_clients`
SET `LEVEL_2_CLIENT_ID` = `ID`
WHERE `CODE` = 'DEMO_STUDIO';

-- ── 2. Platform-wide permissions (CLIENT_ID = NULL)
INSERT INTO `security`.`security_permissions` (`CODE`, `DESCRIPTION`) VALUES
    ('USER_CREATE',       'Create a new user account'),
    ('USER_READ',         'View user details'),
    ('USER_UPDATE',       'Update user information'),
    ('USER_DELETE',       'Delete a user account'),
    ('ROLE_CREATE',       'Create a new role'),
    ('ROLE_READ',         'View role details'),
    ('ROLE_UPDATE',       'Update role information'),
    ('ROLE_DELETE',       'Delete a role'),
    ('PERMISSION_CREATE', 'Create a new permission'),
    ('PERMISSION_READ',   'View permission details'),
    ('PERMISSION_UPDATE', 'Update permission information'),
    ('PERMISSION_DELETE', 'Delete a permission'),
    ('CLIENT_CREATE',     'Create a new client'),
    ('CLIENT_READ',       'View client details'),
    ('CLIENT_UPDATE',     'Update client information'),
    ('CLIENT_DELETE',     'Delete a client');

-- ── 3. Platform-wide roles (CLIENT_ID = NULL)
INSERT INTO `security`.`security_roles` (`NAME`, `DESCRIPTION`) VALUES
    ('ADMIN',      'Platform administrator — full access to all clients, users, roles and permissions'),
    ('INSTRUCTOR', 'Manages own students and classes within a client; cannot modify roles, permissions or peer instructors'),
    ('USER',       'Standard end-user; read-only access to own profile');

-- ── 4. ADMIN gets every platform permission
INSERT INTO `security`.`security_role_permissions` (`ROLE_ID`, `PERMISSION_ID`)
SELECT r.`ID`, p.`ID`
FROM `security`.`security_roles` r
CROSS JOIN `security`.`security_permissions` p
WHERE r.`NAME` = 'ADMIN'
  AND r.`CLIENT_ID` IS NULL
  AND p.`CLIENT_ID` IS NULL;

-- ── 5. INSTRUCTOR: full user CRUD + read-only on roles, permissions, clients
INSERT INTO `security`.`security_role_permissions` (`ROLE_ID`, `PERMISSION_ID`)
SELECT r.`ID`, p.`ID`
FROM `security`.`security_roles` r
JOIN `security`.`security_permissions` p
    ON p.`CODE` IN (
        'USER_CREATE', 'USER_READ', 'USER_UPDATE', 'USER_DELETE',
        'ROLE_READ', 'PERMISSION_READ', 'CLIENT_READ'
    )
WHERE r.`NAME` = 'INSTRUCTOR'
  AND r.`CLIENT_ID` IS NULL
  AND p.`CLIENT_ID` IS NULL;

-- ── 6. USER: read-only reference data
INSERT INTO `security`.`security_role_permissions` (`ROLE_ID`, `PERMISSION_ID`)
SELECT r.`ID`, p.`ID`
FROM `security`.`security_roles` r
JOIN `security`.`security_permissions` p
    ON p.`CODE` IN ('USER_READ', 'ROLE_READ', 'PERMISSION_READ', 'CLIENT_READ')
WHERE r.`NAME` = 'USER'
  AND r.`CLIENT_ID` IS NULL
  AND p.`CLIENT_ID` IS NULL;

-- ── 7. Sample users
INSERT INTO `security`.`security_users`
    (`CLIENT_ID`, `USER_NAME`, `EMAIL`, `FIRST_NAME`, `LAST_NAME`, `PASSWORD`, `PASSWORD_HASHED`, `USER_STATUS_CODE`)
VALUES
    -- Platform admin — no client (platform-level)
    (NULL,
     'admin@ikara.studio', 'admin@ikara.studio', 'System', 'Admin',
     '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', TRUE, 'ACTIVE'),

    -- Demo instructor — Demo Studio (level 2)
    ((SELECT `ID` FROM `security`.`security_clients` WHERE `CODE` = 'DEMO_STUDIO'),
     'instructor@demo.studio', 'instructor@demo.studio', 'Demo', 'Instructor',
     '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', TRUE, 'ACTIVE'),

    -- Demo student — Demo Studio (level 2)
    ((SELECT `ID` FROM `security`.`security_clients` WHERE `CODE` = 'DEMO_STUDIO'),
     'user@demo.studio', 'user@demo.studio', 'Demo', 'User',
     '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', TRUE, 'ACTIVE');

-- ── 8. Assign roles to users
INSERT INTO `security`.`security_user_roles` (`USER_ID`, `ROLE_ID`)
SELECT u.`ID`, r.`ID`
FROM `security`.`security_users` u
JOIN `security`.`security_roles` r ON r.`NAME` = 'ADMIN' AND r.`CLIENT_ID` IS NULL
WHERE u.`USER_NAME` = 'admin@ikara.studio';

INSERT INTO `security`.`security_user_roles` (`USER_ID`, `ROLE_ID`)
SELECT u.`ID`, r.`ID`
FROM `security`.`security_users` u
JOIN `security`.`security_roles` r ON r.`NAME` = 'INSTRUCTOR' AND r.`CLIENT_ID` IS NULL
WHERE u.`USER_NAME` = 'instructor@demo.studio';

INSERT INTO `security`.`security_user_roles` (`USER_ID`, `ROLE_ID`)
SELECT u.`ID`, r.`ID`
FROM `security`.`security_users` u
JOIN `security`.`security_roles` r ON r.`NAME` = 'USER' AND r.`CLIENT_ID` IS NULL
WHERE u.`USER_NAME` = 'user@demo.studio';
