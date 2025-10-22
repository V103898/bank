package ru.effectivemobile.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.effectivemobile.dto.JwtResponse;
import ru.effectivemobile.dto.LoginRequest;
import ru.effectivemobile.dto.SignUpRequest;
import ru.effectivemobile.dto.SignUpResponse;
import ru.effectivemobile.entity.User;
import ru.effectivemobile.security.JwtTokenProvider;
import ru.effectivemobile.service.UserService;

/**
 * Контроллер для аутентификации и регистрации пользователей
 * @author EffectiveMobile Team
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация", description = "API для регистрации и входа в систему")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;

    /**
     * Аутентификация пользователя
     * @param loginRequest данные для входа
     * @return JWT токен и информация о пользователе
     */
    @Operation(summary = "Вход в систему", description = "Аутентификация пользователя и получение JWT токена")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешная аутентификация",
                    content = @Content(schema = @Schema(implementation = JwtResponse.class))),
            @ApiResponse(responseCode = "401", description = "Неверные учетные данные"),
            @ApiResponse(responseCode = "400", description = "Невалидные данные запроса")
    })
    @PostMapping("/signin")
    public ResponseEntity<JwtResponse> authenticateUser(
            @Parameter(description = "Данные для входа", required = true)
            @Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        JwtResponse response = new JwtResponse(jwt, "Bearer");
        return ResponseEntity.ok(response);
    }

    /**
     * Регистрация нового пользователя
     * @param signUpRequest данные для регистрации
     * @return информация о зарегистрированном пользователе
     */
    @Operation(summary = "Регистрация пользователя", description = "Создание нового аккаунта пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно зарегистрирован",
                    content = @Content(schema = @Schema(implementation = SignUpResponse.class))),
            @ApiResponse(responseCode = "400", description = "Невалидные данные или пользователь уже существует"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> registerUser(
            @Parameter(description = "Данные для регистрации", required = true)
            @Valid @RequestBody SignUpRequest signUpRequest) {

        User user = userService.createUser(signUpRequest);
        SignUpResponse response = new SignUpResponse("User registered successfully", user.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}