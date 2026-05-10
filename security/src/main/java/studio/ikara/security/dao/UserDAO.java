package studio.ikara.security.dao;

import static studio.ikara.security.jooq.Tables.SECURITY_AUTHORITIES;
import static studio.ikara.security.jooq.Tables.SECURITY_PERMISSIONS;
import static studio.ikara.security.jooq.Tables.SECURITY_ROLE_PERMISSIONS;
import static studio.ikara.security.jooq.Tables.SECURITY_USERS;
import static studio.ikara.security.jooq.Tables.SECURITY_USER_AUTHORITIES;
import static studio.ikara.security.jooq.Tables.SECURITY_USER_ROLES;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.jooq.types.ULong;
import org.springframework.stereotype.Component;
import studio.ikara.commons.jooq.dao.AbstractUpdatableDAO;
import studio.ikara.security.dto.User;
import studio.ikara.security.jooq.tables.records.SecurityUsersRecord;

@Component
public class UserDAO extends AbstractUpdatableDAO<SecurityUsersRecord, ULong, User> {

    protected UserDAO() {
        super(User.class, SECURITY_USERS, SECURITY_USERS.ID);
    }

    public CompletableFuture<User> findByUsername(String username) {
        return CompletableFuture.supplyAsync(() -> dslContext
                        .selectFrom(SECURITY_USERS)
                        .where(SECURITY_USERS.USER_NAME.eq(username))
                        .fetchOptionalInto(User.class)
                        .orElse(null))
                .thenCompose(user -> {
                    if (user != null) return loadAuthorities(user);
                    return CompletableFuture.completedFuture(null);
                });
    }

    private CompletableFuture<User> loadAuthorities(User user) {
        return CompletableFuture.supplyAsync(() -> {
            List<String> authorities = new ArrayList<>();

            // direct named authorities (legacy)
            authorities.addAll(dslContext
                    .select(SECURITY_AUTHORITIES.NAME)
                    .from(SECURITY_USER_AUTHORITIES)
                    .join(SECURITY_AUTHORITIES)
                    .on(SECURITY_USER_AUTHORITIES.AUTHORITY_ID.eq(SECURITY_AUTHORITIES.ID))
                    .where(SECURITY_USER_AUTHORITIES.USER_ID.eq(user.getId()))
                    .fetchInto(String.class));

            // role-based permissions prefixed with "Authorities."
            dslContext
                    .selectDistinct(SECURITY_PERMISSIONS.CODE)
                    .from(SECURITY_USER_ROLES)
                    .join(SECURITY_ROLE_PERMISSIONS)
                    .on(SECURITY_USER_ROLES.ROLE_ID.eq(SECURITY_ROLE_PERMISSIONS.ROLE_ID))
                    .join(SECURITY_PERMISSIONS)
                    .on(SECURITY_ROLE_PERMISSIONS.PERMISSION_ID.eq(SECURITY_PERMISSIONS.ID))
                    .where(SECURITY_USER_ROLES.USER_ID.eq(user.getId()))
                    .fetchInto(String.class)
                    .stream()
                    .map(code -> "Authorities." + code)
                    .forEach(authorities::add);

            user.setAuthorities(authorities);
            return user;
        });
    }
}
