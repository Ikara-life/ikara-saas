package studio.ikara.security.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import studio.ikara.commons.jooq.service.AbstractJOOQUpdatableDataService;
import studio.ikara.commons.security.jwt.ContextUser;
import studio.ikara.commons.thread.VirtualThreadExecutor;
import studio.ikara.security.dao.UserDAO;
import studio.ikara.security.dto.User;

@Service
public class UserService extends AbstractJOOQUpdatableDataService<CoreUsersRecord, Long, User, UserDAO> {

	private final PasswordEncoder passwordEncoder;

	public UserService(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	public CompletableFuture<User> findByUsername(String username) {
		return this.dao.findByUsername(username);
	}

	public CompletableFuture<Boolean> validatePassword(User user, String password) {

		if (!user.isPasswordHashed()) return VirtualThreadExecutor.completedFuture(password.equals(user.getPassword()));

		return VirtualThreadExecutor.completedFuture(passwordEncoder.matches(password, user.getPassword()));
	}

	public CompletableFuture<ContextUser> toContextUser(User user) {
		return VirtualThreadExecutor.completedFuture(user.toContextUser());
	}

	@Override
	protected CompletableFuture<User> updatableEntity(User entity) {
		return CompletableFuture.supplyAsync(() -> {
			User existingUser = this.read(entity.getId()).join();
			if (existingUser == null) return entity;

			existingUser.setPassword(passwordEncoder.encode(entity.getPassword()));
			existingUser.setFirstName(entity.getFirstName());
			existingUser.setLastName(entity.getLastName());
			existingUser.setMiddleName(entity.getMiddleName());
			existingUser.setLocaleCode(entity.getLocaleCode());
			existingUser.setNoFailedAttempt(entity.getNoFailedAttempt());
			existingUser.setUserName(entity.getUserName());
			existingUser.setEmailId(entity.getEmailId());
			existingUser.setPhoneNumber(entity.getPhoneNumber());
			existingUser.setAuthorities(entity.getAuthorities());

			return existingUser;
		});
	}
}
