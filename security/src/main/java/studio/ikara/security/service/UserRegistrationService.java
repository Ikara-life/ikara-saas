package studio.ikara.security.service;

import java.util.HashSet;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import studio.ikara.commons.exception.GenericException;
import studio.ikara.commons.thread.VirtualThreadExecutor;
import studio.ikara.security.dto.User;
import studio.ikara.security.enums.UserStatusCode;
import studio.ikara.security.model.AuthenticationRequest;
import studio.ikara.security.model.AuthenticationResponse;
import studio.ikara.security.model.UserRegistrationRequest;

@Service
public class UserRegistrationService {

	private final UserService userService;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationService authenticationService;

	public UserRegistrationService(
			UserService userService, PasswordEncoder passwordEncoder, AuthenticationService authenticationService) {
		this.userService = userService;
		this.passwordEncoder = passwordEncoder;
		this.authenticationService = authenticationService;
	}

	public CompletableFuture<AuthenticationResponse> registerUser(
			UserRegistrationRequest registrationRequest, HttpServletRequest request) {
		return this.createUser(registrationRequest.toUser())
				.thenCompose(registeredUser -> authenticationService.authenticate(
						AuthenticationRequest.of(registeredUser.getUserName(), registrationRequest.getPassword()),
						request));
	}

	private CompletableFuture<User> createUser(User user) {
		return this.validateNewUser(user).thenComposeAsync(validUser -> {
			validUser.setPassword(passwordEncoder.encode(validUser.getPassword()));
			validUser.setPasswordHashed(true);

			validUser.setUserStatusCode(UserStatusCode.ACTIVE);

			if (validUser.getAuthorities() == null) validUser.setAuthorities(new HashSet<>());

			validUser.setNoFailedAttempt((short) 0);

			return userService.create(validUser);
		});
	}

	private CompletableFuture<User> validateNewUser(User user) {
		return VirtualThreadExecutor.supplyAsync(() -> {
			if (user.getUserName() == null || user.getUserName().isBlank())
				throw new GenericException(HttpStatus.BAD_REQUEST, "Username is required");

			if (user.getPassword() == null || user.getPassword().isBlank())
				throw new GenericException(HttpStatus.BAD_REQUEST, "Password is required");

			User existingUser = userService.findByUsername(user.getUserName()).join();
			if (existingUser != null) throw new GenericException(HttpStatus.CONFLICT, "Username already exists");

			return user;
		});
	}
}
