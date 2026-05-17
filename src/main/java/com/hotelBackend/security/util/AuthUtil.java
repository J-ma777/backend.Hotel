package com.hotelBackend.security.util;

import com.hotelBackend.security.user.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUtil {

    /**
     * Obtiene el ID del usuario actualmente autenticado del SecurityContext.
     *
     * @return ID del usuario autenticado
     * @throws IllegalStateException si no hay usuario autenticado
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No hay usuario autenticado en el contexto");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails)) {
            throw new IllegalStateException("Principal no es de tipo CustomUserDetails");
        }

        return ((CustomUserDetails) principal).getId();
    }

    /**
     * Obtiene el nombre de usuario actualmente autenticado del SecurityContext.
     *
     * @return Nombre de usuario autenticado
     * @throws IllegalStateException si no hay usuario autenticado
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No hay usuario autenticado en el contexto");
        }

        return authentication.getName();
    }

    /**
     * Obtiene el objeto CustomUserDetails del usuario autenticado.
     *
     * @return CustomUserDetails del usuario autenticado
     * @throws IllegalStateException si no hay usuario autenticado
     */
    public static CustomUserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No hay usuario autenticado en el contexto");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails)) {
            throw new IllegalStateException("Principal no es de tipo CustomUserDetails");
        }

        return (CustomUserDetails) principal;
    }
}

