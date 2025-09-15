package com.damian.photogram.core.security;


import com.damian.photogram.app.auth.exception.EmailNotFoundException;
import com.damian.photogram.core.common.JwtUtil;
import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.exception.JwtTokenExpiredException;
import com.damian.photogram.core.exception.JwtTokenInvalidException;
import com.damian.photogram.core.security.user.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that handles the authentication of every request.
 * It checks if the JWT is valid and if so, it sets the Authentication Object to the SecurityContext.
 */
@Component
public class AuthenticationFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    public AuthenticationFilter(
            JwtUtil jwtUtil,
            CustomUserDetailsService customUserDetailsService,
            AuthenticationEntryPoint authenticationEntryPoint
    ) {
        this.jwtUtil = jwtUtil;
        this.customUserDetailsService = customUserDetailsService;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }


    /**
     * @param request     The request object.
     * @param response    The response object.
     * @param filterChain The filter chain.
     * @throws ServletException If there is an error.
     * @throws IOException      If there is an error.
     */
    @Override
    protected void doFilterInternal(
            @NonNull
            HttpServletRequest request,
            @NonNull
            HttpServletResponse response,
            @NonNull
            FilterChain filterChain
    )
            throws ServletException, IOException {
        // Get the Authorization header.
        final String jwtToken = this.extractToken(request);

        // If the header is null or does not start with "Bearer " then we
        // don't have a token, so we can just continue the filter chain.
        if (jwtToken == null || jwtToken.isEmpty()) {
            log.debug("No jwt token detected. processing request without auth.");
            filterChain.doFilter(request, response);
            return;
        }

        // Check if the token has expired.
        if (!jwtUtil.isTokenValid(jwtToken)) {
            log.debug("Jwt token is invalid.");
            // token is invalid. 401
            authenticationEntryPoint.commence(
                    request, response, new JwtTokenInvalidException(Exceptions.JWT.TOKEN.INVALID)
            );
            return;
        }

        if (jwtUtil.isTokenExpired(jwtToken)) {
            log.debug("Jwt token is expired.");
            // If the token has expired, then we need to send back a 401.
            authenticationEntryPoint.commence(
                    request, response, new JwtTokenExpiredException(Exceptions.JWT.TOKEN.EXPIRED)
            );
            return;
        }

        // Extract the email from the JWT.
        final String email = jwtUtil.extractEmail(jwtToken);

        // If the email found in token is not null and there is no Authentication object
        // in the SecurityContext, then we can go ahead and authenticate the user.
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails;
            try {
                // Load the customer details from the database.
                userDetails = customUserDetailsService.loadUserByEmail(email);
            } catch (EmailNotFoundException exception) {
                // In case no such user exists by this email, then we sent 401
                log.debug("Failed to authenticate user with email: {}", email);
                authenticationEntryPoint.commence(
                        request, response, exception
                );
                return;
            }

            // Create an Authentication object.
            var authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

            // Add some extra details to the Authentication object.
            authToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            log.debug("Configuring context with the jwt token");
            // Finally, set the Authentication object in the SecurityContext.
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        // Continue the filter chain.
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        log.debug("Extracting jwt token from request.");
        // First find the token in the header
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            log.debug("jwt token found in header.");
            return header.substring(7);
        }

        // Find the token in a cookie
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    log.debug("jwt token found in cookies.");
                    return cookie.getValue();
                }
            }
        }

        log.debug("No jwt token found.");
        return null; // no token found
    }
}