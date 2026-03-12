package com.xietg.kc.controller;

import com.xietg.kc.db.entity.UserEntity;
import com.xietg.kc.db.entity.UserRole;
import com.xietg.kc.db.repo.UserRepository;
import com.xietg.kc.error.BusinessException;
import com.xietg.kc.security.JwtService;
import com.xietg.kc.security.PasswordService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Optional;

@RestController
public class AuthController
{

	private final UserRepository userRepository;
	private final PasswordService passwordService;
	private final JwtService jwtService;

	public AuthController(UserRepository userRepository, PasswordService passwordService, JwtService jwtService)
	{
		this.userRepository = userRepository;
		this.passwordService = passwordService;
		this.jwtService = jwtService;
	}

	@PostMapping("/auth/login")
	public LoginResponse login(@Valid @RequestBody LoginRequest req)
	{

		String email = req.email().trim().toLowerCase(Locale.ROOT);
		String password = req.password();

		UserEntity user = userRepository.findByEmail(email)
				.orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

		if (!passwordService.verifyPassword(password, user.getPasswordHash()))
		{
			throw new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
		}

		String token = jwtService.createAccessToken(user.getEmail(), user.getRole().name(), 3600);

		return new LoginResponse(token, user.getRole().name());

	}

	@PostMapping("/auth/register")
	public LoginResponse register(@Valid @RequestBody LoginRequest req)
	{

		String email = req.email().trim().toLowerCase(Locale.ROOT);
		String password = req.password();
		UserEntity user = new UserEntity();

		Optional<UserEntity> existingUser = userRepository.findByEmail(email);

		if (existingUser.isPresent())
		{
			throw new BusinessException(HttpStatus.CONFLICT, "User already exist");
		}

		user.setEmail(email);
		user.setPasswordHash(passwordService.hashPassword(password));
		user.setRole(UserRole.admin);

		userRepository.save(user);

		return new LoginResponse(null, user.getRole().name());
	}

	public record LoginRequest(@NotBlank @Email String email, @NotBlank String password)
	{
	}

	public record LoginResponse(String access_token, String role)
	{
	}
}
