package com.example.demo.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

@Component
public class JWTVerificationFilter extends BasicAuthenticationFilter {

    public JWTVerificationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String header = request.getHeader(SecurityConstants.HEADER);
        if (header == null || !header.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken tokenAuthentication = getAuthentication(request);
        SecurityContextHolder.getContext().setAuthentication(tokenAuthentication);
        chain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(SecurityConstants.HEADER);
        if (token == null) {
            System.out.println("No token provided");
            return null;
        }

        byte[] secretBytes = SecurityConstants.SECRET.getBytes();
        try {
            String user = JWT.require(HMAC512(secretBytes))
                    .build()
                    .verify(token.substring(SecurityConstants.TOKEN_PREFIX.length()))
                    .getSubject();
            if (user == null)
                return null;
            return new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
        }
        catch (SignatureVerificationException e) {
            System.out.println("Invalid token");
            return null;
        }
    }

}

