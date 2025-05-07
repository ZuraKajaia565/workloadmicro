package com.example.micro.logging;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

@Component
public class TransactionLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(TransactionLoggingFilter.class);
    private static final String TRANSACTION_ID = "transactionId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Check if we already have a transaction ID (might be passed from upstream service)
        String transactionId = request.getHeader("X-Transaction-ID");

        // If no transaction ID exists, generate a new one
        if (transactionId == null || transactionId.isEmpty()) {
            transactionId = UUID.randomUUID().toString();
        }

        // Set the transaction ID in MDC
        MDC.put(TRANSACTION_ID, transactionId);

        // Wrap the request and response to capture body content
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        // Set the transaction ID in the response header for downstream services
        responseWrapper.setHeader("X-Transaction-ID", transactionId);

        try {
            // Log the incoming request
            logRequest(requestWrapper, transactionId);

            // Proceed with the request
            filterChain.doFilter(requestWrapper, responseWrapper);

            // Log the outgoing response
            logResponse(requestWrapper, responseWrapper, transactionId);
        } finally {
            // Copy content to the original response
            responseWrapper.copyBodyToResponse();

            // Clear MDC
            MDC.clear();
        }
    }

    private void logRequest(ContentCachingRequestWrapper request, String transactionId) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();

        if (queryString != null) {
            uri += "?" + queryString;
        }

        // Transaction level logging
        logger.info("Transaction ID: {} - Received {} request to {}", transactionId, method, uri);

        // Operation level logging (more detailed, includes headers etc.)
        if (logger.isDebugEnabled()) {
            logger.debug("Transaction ID: {} - Request details: Method={}, URI={}, Content-Type={}, Content-Length={}",
                    transactionId, method, uri, request.getContentType(), request.getContentLength());
        }
    }

    private void logResponse(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, String transactionId) {
        int status = response.getStatus();
        String method = request.getMethod();
        String uri = request.getRequestURI();

        // Transaction level logging
        logger.info("Transaction ID: {} - Completed {} request to {} with status {}",
                transactionId, method, uri, status);

        // Operation level logging (more detailed)
        if (logger.isDebugEnabled()) {
            logger.debug("Transaction ID: {} - Response details: Status={}, Content-Type={}, Content-Length={}",
                    transactionId, status, response.getContentType(), response.getContentSize());

            // Log response body for debugging (be cautious with sensitive data)
            byte[] content = response.getContentAsByteArray();
            if (content.length > 0 && isTextBasedContentType(response.getContentType())) {
                try {
                    String contentAsString = new String(content, response.getCharacterEncoding());
                    logger.trace("Transaction ID: {} - Response body:\n{}", transactionId, contentAsString);
                } catch (UnsupportedEncodingException e) {
                    logger.warn("Transaction ID: {} - Could not log response body: {}", transactionId, e.getMessage());
                }
            }
        }
    }

    private boolean isTextBasedContentType(String contentType) {
        return contentType != null && (
                contentType.startsWith("text/") ||
                        contentType.startsWith("application/json") ||
                        contentType.startsWith("application/xml"));
    }
}