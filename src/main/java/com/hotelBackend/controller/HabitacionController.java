package com.hotelBackend.controller;

import com.hotelBackend.dto.response.HabitacionResponse;
import com.hotelBackend.model.Habitacion;
import com.hotelBackend.service.HabitacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
@RestController
@RequestMapping("/habitaciones")
@RequiredArgsConstructor
public class HabitacionController {


    private final HabitacionService habitacionService;

    @PreAuthorize("hasAuthority('HABITACION_VER')")
    @GetMapping
    public List<HabitacionResponse> listar() {
        return habitacionService.listar();
    }

    @PreAuthorize("hasAuthority('HABITACION_VER')")
    @GetMapping("/{id}")
    public Habitacion obtener(@PathVariable Long id) {
        return habitacionService.obtenerPorId(id);
    }

    @PreAuthorize("hasAuthority('HABITACION_CREAR')")
    @PostMapping
    public Habitacion crear(@RequestBody Habitacion habitacion) {
        return habitacionService.guardar(habitacion);
    }

    @PreAuthorize("hasAuthority('HABITACION_EDITAR')")
    @PutMapping("/{id}")
    public Habitacion actualizar(
            @PathVariable Long id,
            @RequestBody Habitacion habitacion
    ) {
        return habitacionService.actualizar(id, habitacion);
    }

    @PreAuthorize("hasAuthority('HABITACION_ELIMINAR')")
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        habitacionService.eliminar(id);
    }

    @GetMapping("/disponibles-mantenimiento")
    @PreAuthorize("hasAuthority('MANTENIMIENTO_CREAR')")
    public ResponseEntity<List<HabitacionResponse>> obtenerParaMantenimiento() {
        List<HabitacionResponse> response = habitacionService.obtenerParaMantenimiento()
                .stream()
                .map(HabitacionResponse::new)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('HABITACION_VER')")
    @GetMapping("/disponibles")
    public List<HabitacionResponse> obtenerDisponibles(
            @RequestParam Long tipoHabitacionId,
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin) {

        return habitacionService.obtenerDisponiblesPorTipo(
                tipoHabitacionId,
                LocalDate.parse(fechaInicio),
                LocalDate.parse(fechaFin)
        );
    }

}
