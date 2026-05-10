package studio.ikara.security.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.concurrent.CompletableFuture;
import org.jooq.types.ULong;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import studio.ikara.commons.jooq.controller.AbstractJOOQUpdatableDataController;
import studio.ikara.security.configuration.OpenApiConfig;
import studio.ikara.security.dao.RoleDAO;
import studio.ikara.security.dto.Role;
import studio.ikara.security.jooq.tables.records.SecurityRolesRecord;
import studio.ikara.security.service.RoleService;

@Tag(name = OpenApiConfig.TAG_ROLE)
@RestController
@RequestMapping("/api/v1/roles")
public class RoleController
        extends AbstractJOOQUpdatableDataController<SecurityRolesRecord, ULong, Role, RoleDAO, RoleService> {

    @Operation(summary = "Assign role to user")
    @PostMapping("/{roleId}/users/{userId}")
    public CompletableFuture<ResponseEntity<Void>> assignToUser(
            @PathVariable Long roleId, @PathVariable Long userId) {
        return this.service.assignToUser(ULong.valueOf(roleId), ULong.valueOf(userId))
                .thenApply(v -> ResponseEntity.noContent().build());
    }

    @Operation(summary = "Revoke role from user")
    @DeleteMapping("/{roleId}/users/{userId}")
    public CompletableFuture<ResponseEntity<Void>> revokeFromUser(
            @PathVariable Long roleId, @PathVariable Long userId) {
        return this.service.revokeFromUser(ULong.valueOf(roleId), ULong.valueOf(userId))
                .thenApply(v -> ResponseEntity.noContent().build());
    }
}
