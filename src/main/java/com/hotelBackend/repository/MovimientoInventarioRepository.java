package com.hotelBackend.repository;

import com.hotelBackend.model.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    List<MovimientoInventario> findByArticuloIdOrderByFechaMovimientoDesc(Long articuloId);
}
