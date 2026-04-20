package com.hotelBackend.controller;

import com.hotelBackend.model.TipoHabitacion;
import com.hotelBackend.service.TipoHabitacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import  java.util.List;
@RestController
@RequestMapping("/tipo-habitaciones")
@RequiredArgsConstructor
public class TipoHabitacionController {


    private final TipoHabitacionService tipoHabitacionService;

    @PreAuthorize("hasAuthority('TIPO_HABITACION_VER')")
    @GetMapping
    public List<TipoHabitacion> listar() {
        return tipoHabitacionService.listar();
    }

    @PreAuthorize("hasAuthority('TIPO_HABITACION_CREAR')")
    @PostMapping
    public TipoHabitacion crear(@RequestBody TipoHabitacion tipoHabitacion) {
        return tipoHabitacionService.guardar(tipoHabitacion);
    }

}
