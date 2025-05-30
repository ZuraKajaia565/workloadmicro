package com.example.micro.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Value("${spring.security.jwt.enabled:true}")
    private boolean jwtEnabled;

    @Value("${spring.core.service.url:http://localhost:8081}")
    private String coreServiceUrl;

    @Value("${spring.core.service.validate-token-endpoint:/api/auth/validate}")
    private String validateTokenEndpoint;

    private final Environment environment;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public JwtAuthenticationFilter(Environment environment, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.environment = environment;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Skip JWT validation in test environment or if JWT is disabled
        if (environment.matchesProfiles("test") || !jwtEnabled) {
            logger.debug("JWT validation skipped: test profile active or JWT disabled");
            filterChain.doFilter(request, response);
            return;
        }

        // Extract Authorization header
        String header = request.getHeader("Authorization");

        // Check if header is missing or doesn't start with "Bearer "
        if (header == null || !header.startsWith("Bearer ")) {
            logger.warn("No JWT token found in request headers");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("No JWT token found in request headers");
            return;
        }

        // Extract the token
        String token = header.substring(7);

        try {
            // Validate token with spring_core microservice
            validateTokenWithCoreService(token, response, request, filterChain);
        } catch (Exception e) {
            logger.error("Error validating JWT token", e);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Invalid JWT token: " + e.getMessage());
        }
    }

    private void validateTokenWithCoreService(String token, HttpServletResponse response,
                                              HttpServletRequest request, FilterChain filterChain)
            throws IOException, ServletException {

        try {
            // Create headers for request to core service
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);

            // Make request to core service to validate token
            ResponseEntity<String> coreResponse = restTemplate.exchange(
                    coreServiceUrl + validateTokenEndpoint,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class
            );

            // Check if validation was successful
            if (coreResponse.getStatusCode() == HttpStatus.OK) {
                // Parse the response
                JsonNode root = objectMapper.readTree(coreResponse.getBody());

                // Extract user information
                String username = root.path("username").asText();
                List<String> roles = extractRoles(root);

                // Create authentication token with user information and roles
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        roles.stream()
                                .map(SimpleGrantedAuthority::new)
                                .toList()
                );

                // Set details like you would have from a real JWT
                authentication.setDetails(request);

                // Set the authentication in the security context
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Continue with the filter chain
                filterChain.doFilter(request, response);
            } else {
                logger.warn("Token validation failed: {}", coreResponse.getStatusCode());
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Invalid JWT token: authentication failed");
            }
        } catch (RestClientException e) {
            logger.error("Error connecting to core service for token validation", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Error validating token: core service unavailable");
        }
    }

    private List<String> extractRoles(JsonNode root) {
        JsonNode rolesNode = root.path("roles");
        if (rolesNode.isArray()) {
            return objectMapper.convertValue(rolesNode, List.class);
        } else {
            String role = root.path("role").asText();
            if (!role.isEmpty()) {
                return Collections.singletonList(role);
            }
        }
        // Default role if no roles found
        return Collections.singletonList("ROLE_USER");
    }
}