package com.hotelBackend.repository;

import com.hotelBackend.model.ArticuloInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ArticuloInventarioRepository extends JpaRepository<ArticuloInventario,Long> {
    @Query("""
    SELECT a
    FROM ArticuloInventario a
    WHERE a.stockActual <= a.stockMinimo
    ORDER BY a.stockActual ASC
""")
    List<ArticuloInventario> findConStockBajo();
}
