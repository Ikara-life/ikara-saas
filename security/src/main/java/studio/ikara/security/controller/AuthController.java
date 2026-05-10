package studio.ikara.security.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.concurrent.CompletableFuture;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import studio.ikara.security.configuration.OpenApiConfig;
import studio.ikara.security.model.AuthenticationRequest;
import studio.ikara.security.model.AuthenticationResponse;
import studio.ikara.security.model.UserRegistrationRequest;
import studio.ikara.security.service.AuthenticationService;
import studio.ikara.security.service.UserRegistrationService;

@Tag(name = OpenApiConfig.TAG_AUTH)
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final UserRegistrationService userRegistrationService;

    public AuthController(
            AuthenticationService authenticationService, UserRegistrationService userRegistrationService) {
        this.authenticationService = authenticationService;
        this.userRegistrationService = userRegistrationService;
    }

    @Operation(summary = "Login", description = "Authenticate with username and password. Returns JWT access token.")
    @PostMapping("/login")
    public CompletableFuture<ResponseEntity<AuthenticationResponse>> login(
            @Valid @RequestBody AuthenticationRequest request, HttpServletRequest httpRequest) {
        return authenticationService.authenticate(request, httpRequest).thenApply(ResponseEntity::ok);
    }

    @Operation(summary = "Register", description = "Create a new user account and return JWT access token.")
    @PostMapping("/register")
    public CompletableFuture<ResponseEntity<AuthenticationResponse>> register(
            @Valid @RequestBody UserRegistrationRequest request, HttpServletRequest httpRequest) {
        return userRegistrationService.registerUser(request, httpRequest).thenApply(ResponseEntity::ok);
    }
}
