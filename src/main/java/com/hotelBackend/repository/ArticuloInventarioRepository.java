package com.hotelBackend.repository;

import com.hotelBackend.model.ArticuloInventario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticuloInventarioRepository extends JpaRepository<ArticuloInventario,Long> {
}
