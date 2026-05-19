package com.hotelBackend.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil,
                                   UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // 1 Si no hay header Bearer, dejamos pasar
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2 Extraer token
        String jwtToken = authHeader.substring(7);
        String username;

        try {
            username = jwtUtil.extractUsername(jwtToken);
        } catch (Exception e) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3 Si hay usuario y no está autenticado aún
        if (username != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(username);

            // 4 Validar token
            if (jwtUtil.isTokenValid(jwtToken, userDetails)) {

                // Diagnóstico: lo que el token realmente trae
                try {
                    var tokenAuthorities = jwtUtil.extractAuthorities(jwtToken);
                    var tokenUserId = jwtUtil.extractUserId(jwtToken);
                    log.info("[JWT_FILTER] request={} {} username={} tokenUserId={} tokenAuthorities={}",
                            request.getMethod(), request.getRequestURI(), username, tokenUserId, tokenAuthorities);
                } catch (Exception ignored) {
                    // no bloquear request por logs
                }

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                // 5️ Guardar en el contexto de seguridad
                SecurityContextHolder.getContext()
                        .setAuthentication(authToken);

                log.info("[JWT_FILTER] SecurityContext set username={} authorities={}",
                        username, authToken.getAuthorities());
            }
        }

        // 6️ Continuar la cadena
        filterChain.doFilter(request, response);
    }

}
