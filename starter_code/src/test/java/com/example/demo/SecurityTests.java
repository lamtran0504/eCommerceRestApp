package com.example.demo;

import com.example.demo.controllers.UserController;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import com.example.demo.model.requests.ModifyCartRequest;
import com.example.demo.security.SecurityConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityTests {

    @Autowired
    private MockMvc mvc;

    @InjectMocks
    private UserController userController;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private CreateUserRequest userRequest;
    private ModifyCartRequest cartRequest;

    private String convertToJson(Object obj) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            final String jsonContent = mapper.writeValueAsString(obj);
            return jsonContent;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    public void setup() {
        userRequest = new CreateUserRequest();
        userRequest.setUsername("LamTran");
        userRequest.setPassword("password");
        userRequest.setConfirmPassword("password");

        cartRequest = new ModifyCartRequest();
        cartRequest.setUsername("LamTran");
        cartRequest.setItemId(1L);
        cartRequest.setQuantity(1);
    }

    @Test
    public void testLogin() throws Exception {
        // Create user
        mvc.perform(
                MockMvcRequestBuilders.post("/api/user/create").content(convertToJson(userRequest))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));
        // Login with created credential
        mvc.perform(
                MockMvcRequestBuilders.post("/login").content("{\"username\":\"LamTran\", \"password\":\"password\"}")
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        // Login with wrong credential
        mvc.perform(
                MockMvcRequestBuilders.post("/login").content("{\"username\":\"LamTran\", \"password\":\"wrongPassword\"}")
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized()).andReturn();
    }

    @Test
    public void testUnauthenticated() throws Exception {
        // Create user
        mvc.perform(
                MockMvcRequestBuilders.post("/api/user/create").content(convertToJson(userRequest))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));
        // The user exists, but you don't have the authentication to make the request.
        mvc.perform(
                MockMvcRequestBuilders.post("api/cart/addToCart").content(convertToJson(cartRequest))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testAuthenticated() throws Exception {
        // Create user
        mvc.perform(
                MockMvcRequestBuilders.post("/api/user/create").content(convertToJson(userRequest))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));
        // Login with created credential
        MvcResult result = mvc.perform(
                        MockMvcRequestBuilders.post("/login").content("{\"username\":\"LamTran\", \"password\":\"password\"}"))
                .andExpect(status().isOk()).andReturn();
        // Get returned token from login
        String token = result.getResponse().getHeader(SecurityConstants.HEADER);
        // Add token to the request as a header
        mvc.perform(
                MockMvcRequestBuilders.post("/api/cart/addToCart")
                        .content(convertToJson(cartRequest))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                        .header(SecurityConstants.HEADER, token))
                .andExpect(status().isOk());
    }

}