package studio.ikara.security.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.jooq.types.ULong;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import studio.ikara.commons.jooq.controller.AbstractJOOQUpdatableDataController;
import studio.ikara.security.configuration.OpenApiConfig;
import studio.ikara.security.dao.UserDAO;
import studio.ikara.security.dto.User;
import studio.ikara.security.jooq.tables.records.SecurityUsersRecord;
import studio.ikara.security.service.UserService;

@Tag(name = OpenApiConfig.TAG_USER)
@RestController
@RequestMapping("/api/v1/users")
public class UserController
        extends AbstractJOOQUpdatableDataController<SecurityUsersRecord, ULong, User, UserDAO, UserService> {}
