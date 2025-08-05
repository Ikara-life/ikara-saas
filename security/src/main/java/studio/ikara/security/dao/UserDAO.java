package studio.ikara.security.dao;

import static studio.ikara.security.jooq.security.Tables.SECURITY_AUTHORITIES;
import static studio.ikara.security.jooq.security.Tables.SECURITY_USERS;
import static studio.ikara.security.jooq.security.Tables.SECURITY_USER_AUTHORITIES;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.stereotype.Component;
import studio.ikara.commons.jooq.dao.AbstractUpdatableDAO;
import studio.ikara.security.dto.User;
import studio.ikara.security.jooq.security.tables.records.SecurityUsersRecord;

@Component
public class UserDAO extends AbstractUpdatableDAO<SecurityUsersRecord, Long, User> {

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
            List<String> authorities = dslContext
                    .select(SECURITY_AUTHORITIES.NAME)
                    .from(SECURITY_USER_AUTHORITIES)
                    .join(SECURITY_AUTHORITIES)
                    .on(SECURITY_USER_AUTHORITIES.AUTHORITY_ID.eq(SECURITY_AUTHORITIES.ID))
                    .where(SECURITY_USER_AUTHORITIES.USER_ID.eq(user.getId()))
                    .fetchInto(String.class);

            user.setAuthorities(authorities);
            return user;
        });
    }
}
