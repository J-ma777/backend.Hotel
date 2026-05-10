package com.hotelBackend.repository;

import com.hotelBackend.model.ArticuloInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ArticuloInventarioRepository extends JpaRepository<ArticuloInventario,Long> {

    List<ArticuloInventario> findByStockActualLessThanEqualStockMinimoOrderByStockActualAsc();
}
