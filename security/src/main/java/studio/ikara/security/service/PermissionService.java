package studio.ikara.security.service;

import java.util.concurrent.CompletableFuture;
import org.jooq.types.ULong;
import org.springframework.stereotype.Service;
import studio.ikara.commons.jooq.service.AbstractJOOQUpdatableDataService;
import studio.ikara.security.dao.PermissionDAO;
import studio.ikara.security.dto.Permission;
import studio.ikara.security.jooq.tables.records.SecurityPermissionsRecord;

@Service
public class PermissionService
        extends AbstractJOOQUpdatableDataService<SecurityPermissionsRecord, ULong, Permission, PermissionDAO> {

    @Override
    protected CompletableFuture<Permission> updatableEntity(Permission entity) {
        return CompletableFuture.supplyAsync(() -> {
            Permission existing = this.read(entity.getId()).join();
            if (existing == null) return entity;
            existing.setCode(entity.getCode());
            existing.setDescription(entity.getDescription());
            return existing;
        });
    }
}
