package studio.ikara.security.dao;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Component;

import studio.ikara.commons.jooq.dao.AbstractUpdatableDAO;
import studio.ikara.security.dto.User;

@Component
public class UserDAO extends AbstractUpdatableDAO<CoreUsersRecord, Long, User> {

	protected UserDAO() {
		super(User.class, CORE_USERS, CORE_USERS.ID);
	}

	public CompletableFuture<User> findByUsername(String username) {
		return CompletableFuture.supplyAsync(() -> dslContext
						.selectFrom(CORE_USERS)
						.where(CORE_USERS.USER_NAME.eq(username))
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
					.select(CORE_AUTHORITIES.NAME)
					.from(CORE_USER_AUTHORITIES)
					.join(CORE_AUTHORITIES)
					.on(CORE_USER_AUTHORITIES.AUTHORITY_ID.eq(CORE_AUTHORITIES.ID))
					.where(CORE_USER_AUTHORITIES.USER_ID.eq(user.getId()))
					.fetchInto(String.class);

			user.setAuthorities(authorities);
			return user;
		});
	}
}
