package com.hotelBackend.repository;

import com.hotelBackend.model.RegistroLimpieza;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RegistroLimpiezaRepository extends JpaRepository<RegistroLimpieza, Long> {

    List<RegistroLimpieza> findByHabitacionId(Long habitacionId);
}
