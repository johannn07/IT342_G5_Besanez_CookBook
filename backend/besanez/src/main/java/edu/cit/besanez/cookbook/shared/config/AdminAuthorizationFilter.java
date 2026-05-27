package edu.cit.besanez.cookbook.shared.config;

import edu.cit.besanez.cookbook.shared.util.JwtUtil;
import edu.cit.besanez.cookbook.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AdminAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/admin");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No token provided");
            return;
        }

        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);

            userRepository.findById(userId).ifPresentOrElse(user -> {
                if (!"ADMIN".equals(user.getRole())) {
                    try {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN,
                                "Admin access required");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    try {
                        filterChain.doFilter(request, response);
                    } catch (IOException | ServletException e) {
                        throw new RuntimeException(e);
                    }
                }
            }, () -> {
                try {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

        } catch (RuntimeException e) {
            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            }
        }
    }
}