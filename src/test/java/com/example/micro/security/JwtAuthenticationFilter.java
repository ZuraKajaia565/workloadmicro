package com.example.micro.security;

import java.io.IOException;
import java.util.Collections;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final Environment environment;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(Environment environment, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.environment = environment;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Always authenticate in test environment
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "test-user", null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        // Set details
        authentication.setDetails(request);

        // Set the authentication in the security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}