package com.hotelBackend.security.auth;

import com.hotelBackend.security.auth.dto.JwtResponse;
import com.hotelBackend.security.auth.dto.LoginRequest;
import com.hotelBackend.security.jwt.JwtUtil;
import com.hotelBackend.security.user.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }


    @PostMapping("/login")
    public JwtResponse login(@Valid @RequestBody LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        String token = jwtUtil.generateToken(
                userDetails,
                userDetails.getId() //
        );

        // Authorities → Strings
        List<String> authorities = userDetails.getAuthorities()
                .stream()
                .map(a -> a.getAuthority())
                .toList();

        log.info("[AUTH_LOGIN] username={} userId={} authorities={}", userDetails.getUsername(), userDetails.getId(), authorities);

        // IMPORTANTE: log de diagnóstico. No imprimir el token completo en entornos productivos.
        String tokenPreview = token.length() <= 40 ? token : token.substring(0, 40) + "...";
        log.info("[AUTH_LOGIN] jwtPreview={}", tokenPreview);

        return new JwtResponse(
                token,
                userDetails.getId(),
                userDetails.getUsername(),
                authorities,
                authorities
        );
    }

}
