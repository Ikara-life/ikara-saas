package studio.ikara.security.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.jooq.types.ULong;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import studio.ikara.commons.jooq.controller.AbstractJOOQUpdatableDataController;
import studio.ikara.security.configuration.OpenApiConfig;
import studio.ikara.security.dao.PermissionDAO;
import studio.ikara.security.dto.Permission;
import studio.ikara.security.jooq.tables.records.SecurityPermissionsRecord;
import studio.ikara.security.service.PermissionService;

@Tag(name = OpenApiConfig.TAG_PERMISSION)
@RestController
@RequestMapping("/api/v1/permissions")
public class PermissionController
        extends AbstractJOOQUpdatableDataController<
                SecurityPermissionsRecord, ULong, Permission, PermissionDAO, PermissionService> {}
