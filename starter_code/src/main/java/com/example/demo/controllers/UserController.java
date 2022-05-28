package com.example.demo.controllers;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;

@RestController
@RequestMapping("/api/user")
public class UserController {

	private static final Logger log = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@GetMapping("/id/{id}")
	public ResponseEntity<User> findById(@PathVariable Long id) {
		log.info("Retrieving user by id.");
		return ResponseEntity.of(userRepository.findById(id));
	}

	@GetMapping("/{username}")
	public ResponseEntity<User> findByUserName(@PathVariable String username) {
		User user = userRepository.findByUsername(username);
		if (user == null) {
			log.info("Error 404 - user not found.");
			return ResponseEntity.notFound().build();
		}
		log.info("User found.");
		return ResponseEntity.ok(user);
	}

	@PostMapping("/create")
	public ResponseEntity<User> createUser(@RequestBody CreateUserRequest createUserRequest) {
		String username = createUserRequest.getUsername();
		String password = createUserRequest.getPassword();
		String confirmPassword = createUserRequest.getConfirmPassword();

		if (username == null || password == null || confirmPassword == null) {
			log.error("Failed to create user - null parameter");
			return ResponseEntity.badRequest().build();
		}
		if (username.length() < 6) {
			log.info("Username must be at least 6 characters in length");
			return ResponseEntity.badRequest().build();
		}
		if (username.length() > 15) {
			log.info("Username must be at most 15 characters in length");
			return ResponseEntity.badRequest().build();
		}
		if (userRepository.findByUsername(username) != null) {
			log.info("Username taken");
			return ResponseEntity.badRequest().build();
		}
		if (password.length() < 8) {
			log.info("Username must be at least 8 characters in length");
			return ResponseEntity.badRequest().build();
		}
		if (!password.equals(confirmPassword)) {
			log.info("Please confirm your password");
			return ResponseEntity.badRequest().build();
		}

		User user = new User();
		user.setUsername(username);
		user.setPassword(bCryptPasswordEncoder.encode(password));
		Cart cart = new Cart();
		cartRepository.save(cart);
		user.setCart(cart);
		userRepository.save(user);
		log.info("User created");
		return ResponseEntity.ok(user);
	}

}
