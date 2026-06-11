package com.hotelbackend.dto.request;

import lombok.Data;

@Data
public class CrearUsuarioRequest {

    private String nombreUsuario;
    private String correo;
    private String contrasena;
    private Long rolId;

}
