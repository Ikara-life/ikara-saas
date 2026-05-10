package studio.ikara.security.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.jooq.types.ULong;
import org.springframework.stereotype.Service;
import studio.ikara.commons.jooq.service.AbstractJOOQUpdatableDataService;
import studio.ikara.security.dao.RoleDAO;
import studio.ikara.security.dto.Role;
import studio.ikara.security.jooq.tables.records.SecurityRolesRecord;

@Service
public class RoleService
        extends AbstractJOOQUpdatableDataService<SecurityRolesRecord, ULong, Role, RoleDAO> {

    @Override
    public CompletableFuture<Role> create(Role entity) {
        return super.create(entity).thenCompose(saved -> enrichPermissions(saved, entity.getPermissions()));
    }

    @Override
    public CompletableFuture<Role> update(Role entity) {
        return super.update(entity).thenCompose(saved -> enrichPermissions(saved, entity.getPermissions()));
    }

    public CompletableFuture<Void> assignToUser(ULong roleId, ULong userId) {
        return this.dao.assignToUser(roleId, userId);
    }

    public CompletableFuture<Void> revokeFromUser(ULong roleId, ULong userId) {
        return this.dao.revokeFromUser(roleId, userId);
    }

    @Override
    protected CompletableFuture<Role> updatableEntity(Role entity) {
        return CompletableFuture.supplyAsync(() -> {
            Role existing = this.read(entity.getId()).join();
            if (existing == null) return entity;
            existing.setName(entity.getName());
            existing.setDescription(entity.getDescription());
            return existing;
        });
    }

    private CompletableFuture<Role> enrichPermissions(Role saved, List<String> codes) {
        if (codes == null || codes.isEmpty()) return CompletableFuture.completedFuture(saved);
        return this.dao.syncPermissions(saved.getId(), codes)
                .thenCompose(v -> this.dao.findPermissionCodes(saved.getId()))
                .thenApply(saved::setPermissions);
    }
}
