package com.example.micro;

import com.example.micro.security.JwtAuthenticationFilter;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String SECRET_KEY = "testSecretKeyWithAtLeast256BitsForHmacSha256Algorithm";
    private static final long EXPIRATION_TIME = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtAuthenticationFilter, "secretKey", SECRET_KEY);
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_WithNoAuthHeader_ShouldContinueWithoutSettingAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_WithNonBearerToken_ShouldContinueWithoutSettingAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcm5hbWU6cGFzc3dvcmQ=");

        // Act
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_WithValidJwt_ShouldSetAuthentication() throws ServletException, IOException {
        // Arrange
        String validToken = createValidToken("testuser", "ROLE_USER");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);

        SecurityContext securityContext = new SecurityContextImpl();
        SecurityContextHolder.setContext(securityContext);

        // Act
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals("testuser", authentication.getName());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void doFilter_WithExpiredJwt_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        String expiredToken = createExpiredToken("testuser", "ROLE_USER");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + expiredToken);

        // Act
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_WithInvalidSignature_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        String tamperedToken = createValidToken("testuser", "ROLE_USER") + "tampered";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + tamperedToken);

        // Act
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_WithMalformedJwt_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer malformed.jwt.token");

        // Act
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void extractUsername_WithValidToken_ShouldReturnUsername() {
        // Arrange
        String token = createValidToken("testuser", "ROLE_USER");

        // Act
        String username = jwtAuthenticationFilter.extractUsername(token);

        // Assert
        assertEquals("testuser", username);
    }

    @Test
    void doFilter_WithMultipleRoles_ShouldSetAllAuthorities() throws ServletException, IOException {
        // Arrange
        String validToken = createValidToken("testuser", "ROLE_USER,ROLE_ADMIN");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);

        SecurityContext securityContext = new SecurityContextImpl();
        SecurityContextHolder.setContext(securityContext);

        // Act
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals("testuser", authentication.getName());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void doFilter_WithExceptionDuringProcessing_ShouldClearContext() throws ServletException, IOException {
        // Arrange
        String validToken = createValidToken("testuser", "ROLE_USER");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);

        // Set up a security context that should be cleared
        SecurityContext securityContext = new SecurityContextImpl();
        Authentication mockAuth = mock(Authentication.class);
        securityContext.setAuthentication(mockAuth);
        SecurityContextHolder.setContext(securityContext);

        // Make extractUsername throw an exception
        JwtAuthenticationFilter spyFilter = Mockito.spy(jwtAuthenticationFilter);
        doThrow(new RuntimeException("Test exception")).when(spyFilter).extractUsername(anyString());

        // Act
        spyFilter.doFilter(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    // Helper methods to create tokens
    private String createValidToken(String username, String roles) {
        return createToken(username, roles, new Date(System.currentTimeMillis() + EXPIRATION_TIME));
    }

    private String createExpiredToken(String username, String roles) {
        return createToken(username, roles, new Date(System.currentTimeMillis() - EXPIRATION_TIME));
    }

    private String createToken(String username, String roles, Date expiration) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expiration)
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }
}