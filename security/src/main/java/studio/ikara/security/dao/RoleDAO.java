package studio.ikara.security.dao;

import static studio.ikara.security.jooq.Tables.SECURITY_PERMISSIONS;
import static studio.ikara.security.jooq.Tables.SECURITY_ROLE_PERMISSIONS;
import static studio.ikara.security.jooq.Tables.SECURITY_ROLES;
import static studio.ikara.security.jooq.Tables.SECURITY_USER_ROLES;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.jooq.types.ULong;
import org.springframework.stereotype.Component;
import studio.ikara.commons.jooq.dao.AbstractUpdatableDAO;
import studio.ikara.security.dto.Role;
import studio.ikara.security.jooq.tables.records.SecurityRolesRecord;

@Component
public class RoleDAO extends AbstractUpdatableDAO<SecurityRolesRecord, ULong, Role> {

    protected RoleDAO() {
        super(Role.class, SECURITY_ROLES, SECURITY_ROLES.ID);
    }

    public CompletableFuture<List<String>> findPermissionCodes(ULong roleId) {
        return CompletableFuture.supplyAsync(() -> dslContext
                .select(SECURITY_PERMISSIONS.CODE)
                .from(SECURITY_ROLE_PERMISSIONS)
                .join(SECURITY_PERMISSIONS)
                .on(SECURITY_ROLE_PERMISSIONS.PERMISSION_ID.eq(SECURITY_PERMISSIONS.ID))
                .where(SECURITY_ROLE_PERMISSIONS.ROLE_ID.eq(roleId))
                .fetchInto(String.class));
    }

    public CompletableFuture<Void> syncPermissions(ULong roleId, List<String> codes) {
        return CompletableFuture.supplyAsync(() -> {
            List<ULong> permissionIds = dslContext
                    .select(SECURITY_PERMISSIONS.ID)
                    .from(SECURITY_PERMISSIONS)
                    .where(SECURITY_PERMISSIONS.CODE.in(codes))
                    .fetch(SECURITY_PERMISSIONS.ID);

            dslContext.deleteFrom(SECURITY_ROLE_PERMISSIONS)
                    .where(SECURITY_ROLE_PERMISSIONS.ROLE_ID.eq(roleId))
                    .execute();

            permissionIds.forEach(pid -> dslContext
                    .insertInto(SECURITY_ROLE_PERMISSIONS,
                            SECURITY_ROLE_PERMISSIONS.ROLE_ID,
                            SECURITY_ROLE_PERMISSIONS.PERMISSION_ID)
                    .values(roleId, pid)
                    .onDuplicateKeyIgnore()
                    .execute());

            return null;
        });
    }

    public CompletableFuture<Void> assignToUser(ULong roleId, ULong userId) {
        return CompletableFuture.supplyAsync(() -> {
            dslContext.insertInto(
                            SECURITY_USER_ROLES,
                            SECURITY_USER_ROLES.USER_ID,
                            SECURITY_USER_ROLES.ROLE_ID)
                    .values(userId, roleId)
                    .onDuplicateKeyIgnore()
                    .execute();
            return null;
        });
    }

    public CompletableFuture<Void> revokeFromUser(ULong roleId, ULong userId) {
        return CompletableFuture.supplyAsync(() -> {
            dslContext.deleteFrom(SECURITY_USER_ROLES)
                    .where(SECURITY_USER_ROLES.ROLE_ID.eq(roleId)
                            .and(SECURITY_USER_ROLES.USER_ID.eq(userId)))
                    .execute();
            return null;
        });
    }
}
