package com.hotelBackend.security.auth;

import com.hotelBackend.security.auth.dto.JwtResponse;
import com.hotelBackend.security.auth.dto.LoginRequest;
import com.hotelBackend.security.jwt.JwtUtil;
import com.hotelBackend.security.user.CustomUserDetails;
import jakarta.validation.Valid;
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

        return new JwtResponse(
                token,
                userDetails.getId(),
                userDetails.getUsername(),
                authorities,
                authorities
        );
    }

}
