package com.hotelBackend.security.auth.dto;

import java.util.List;
public class JwtResponse {


    private String accessToken;
    private String tokenType;
    private Long userId;
    private String username;
    private List<String> roles;
    private List<String> permisos;

    public JwtResponse(String accessToken,
                       Long userId,
                       String username,
                       List<String> roles,
                       List<String> permisos) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
        this.userId = userId;
        this.username = username;
        this.roles = roles;
        this.permisos = permisos;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public List<String> getRoles() {
        return roles;
    }

    public List<String> getPermisos() {
        return permisos;
    }

}
