package com.damian.photogram.core.security;


import com.damian.photogram.app.auth.exception.JwtAuthenticationException;
import com.damian.photogram.core.service.CustomerDetailsService;
import com.damian.photogram.core.utils.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomerDetailsService customerDetailsService;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    public AuthenticationFilter(
            JwtUtil jwtUtil,
            CustomerDetailsService customerDetailsService,
            AuthenticationEntryPoint authenticationEntryPoint
    ) {
        this.jwtUtil = jwtUtil;
        this.customerDetailsService = customerDetailsService;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }


    /**
     * Checks if the JWT is valid and if so, it sets the Authentication Object
     * to the SecurityContext.
     *
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
        final String authHeader = request.getHeader("Authorization");

        // If the header is null or does not start with "Bearer " then we
        // don't have a token, so we can just continue the filter chain.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract the JWT from the Authorization header.
        final String jwtToken = authHeader.substring(7);

        // Check if the token has expired.
        try {
            jwtUtil.isTokenExpired(jwtToken);
        } catch (ExpiredJwtException e) {
            // If the token has expired, then we need to send back a 401
            // Unauthorized response.
            authenticationEntryPoint.commence(request, response, new JwtAuthenticationException("Token expired"));
            return;
        }

        // Extract the email from the JWT.
        final String email = jwtUtil.extractEmail(jwtToken);

        // If the email is not null and there is no Authentication object
        // currently in the SecurityContext, then we can go ahead and
        // authenticate the user.
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Load the customer details from the database.
            CustomerDetails customerDetails = customerDetailsService.loadCustomerByEmail(email);

            // If the token is valid, then we can go ahead and
            // authenticate the user.
            if (jwtUtil.isTokenValid(jwtToken, customerDetails)) {
                // Create an Authentication object.
                var authToken = new UsernamePasswordAuthenticationToken(
                        customerDetails,
                        null,
                        customerDetails.getAuthorities()
                );

                // Add some extra details to the Authentication object.
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Finally, set the Authentication object in the SecurityContext.
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Continue the filter chain.
        filterChain.doFilter(request, response);
    }
}