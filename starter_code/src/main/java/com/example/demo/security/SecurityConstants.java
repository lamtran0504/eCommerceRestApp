package com.example.demo.security;

public class SecurityConstants {

    public static final String SIGN_UP_URL = "/api/user/create";
    public static final long EXPIRATION_TIME = 15*24*60*60*1000; // 15 days in milliseconds
    public static final String SECRET = "supersecretkey";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER = "Authorization";

}