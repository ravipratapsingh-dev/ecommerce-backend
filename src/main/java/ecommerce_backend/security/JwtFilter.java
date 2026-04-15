package ecommerce_backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getServletPath().startsWith("/auth");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("🔥 JWT FILTER CALLED");

        String authHeader = request.getHeader("Authorization");
        System.out.println("Header: " + authHeader);

        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {

                String token = authHeader.substring(7);

                // ✅ IMPORTANT CHECK
                if (jwtUtil.validateToken(token) &&
                        SecurityContextHolder.getContext().getAuthentication() == null) {

                    String email = jwtUtil.extractEmail(token);
                    System.out.println("Extracted email: " + email);

                    if (email != null) {

                        UserDetails userDetails = User
                                .withUsername(email)
                                .password("")
                                .authorities("ROLE_USER")
                                .build();

                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );

                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        // 🔥 MOST IMPORTANT
                        SecurityContextHolder.getContext().setAuthentication(auth);

                        System.out.println("✅ Authentication set for: " + email);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("❌ JWT Error: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
