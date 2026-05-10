package studio.ikara.security.dao;

import static studio.ikara.security.jooq.Tables.SECURITY_PERMISSIONS;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.jooq.types.ULong;
import org.springframework.stereotype.Component;
import studio.ikara.commons.jooq.dao.AbstractUpdatableDAO;
import studio.ikara.security.dto.Permission;
import studio.ikara.security.jooq.tables.records.SecurityPermissionsRecord;

@Component
public class PermissionDAO extends AbstractUpdatableDAO<SecurityPermissionsRecord, ULong, Permission> {

    protected PermissionDAO() {
        super(Permission.class, SECURITY_PERMISSIONS, SECURITY_PERMISSIONS.ID);
    }

    public CompletableFuture<Optional<Permission>> findByCode(String code) {
        return CompletableFuture.supplyAsync(() -> dslContext
                .selectFrom(SECURITY_PERMISSIONS)
                .where(SECURITY_PERMISSIONS.CODE.eq(code))
                .fetchOptionalInto(Permission.class));
    }
}
