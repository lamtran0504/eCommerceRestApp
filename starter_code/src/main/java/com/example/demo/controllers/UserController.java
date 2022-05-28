package com.example.demo.controllers;

import java.util.Optional;

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

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@GetMapping("/id/{id}")
	public ResponseEntity<User> findById(@PathVariable Long id) {
		return ResponseEntity.of(userRepository.findById(id));
	}

	@GetMapping("/{username}")
	public ResponseEntity<User> findByUserName(@PathVariable String username) {
		User user = userRepository.findByUsername(username);
		return user == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(user);
	}

	@PostMapping("/create")
	public ResponseEntity<User> createUser(@RequestBody CreateUserRequest createUserRequest) {
		String username = createUserRequest.getUsername();
		String password = createUserRequest.getPassword();
		String confirmPassword = createUserRequest.getConfirmPassword();

		if (username == null || password == null || confirmPassword == null) {
			System.out.println("Username or password was not provided");
			return ResponseEntity.badRequest().build();
		}
		if (username.length() < 6) {
			System.out.println("Username must be at least 6 characters in length");
			return ResponseEntity.badRequest().build();
		}
		if (username.length() > 15) {
			System.out.println("Username must be at most 15 characters in length");
			return ResponseEntity.badRequest().build();
		}
		if (userRepository.findByUsername(username) != null) {
			System.out.println("Username taken");
			return ResponseEntity.badRequest().build();
		}
		if (password.length() < 8) {
			System.out.println("Username must be at least 8 characters in length");
			return ResponseEntity.badRequest().build();
		}
		if (!password.equals(confirmPassword)) {
			System.out.println("Please confirm your password");
			return ResponseEntity.badRequest().build();
		}

		User user = new User();
		user.setUsername(username);
		user.setPassword(bCryptPasswordEncoder.encode(password));
		Cart cart = new Cart();
		cartRepository.save(cart);
		user.setCart(cart);
		userRepository.save(user);
		System.out.println("User created");
		return ResponseEntity.ok(user);
	}

}
