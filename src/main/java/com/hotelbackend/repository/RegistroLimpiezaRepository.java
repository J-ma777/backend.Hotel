package com.hotelbackend.repository;

import com.hotelbackend.model.RegistroLimpieza;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RegistroLimpiezaRepository extends JpaRepository<RegistroLimpieza, Long> {

    List<RegistroLimpieza> findByHabitacionId(Long habitacionId);
}
