package com.hotelbackend.repository;

import com.hotelbackend.model.TransaccionFolio;
import com.hotelbackend.model.enums.TipoTransaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TransaccionFolioRepository extends JpaRepository<TransaccionFolio, Long> {

    List<TransaccionFolio> findByReservaId(Long reservaId);

    @Query("""
    SELECT COALESCE(SUM(t.total), 0)
    FROM TransaccionFolio t
    WHERE t.reserva.id = :reservaId
      AND t.tipo IN :tipos
""")
    BigDecimal sumByReservaAndTipos(
            @Param("reservaId") Long reservaId,
            @Param("tipos") List<TipoTransaccion> tipos
    );

    @Query("""
    SELECT COALESCE(SUM(t.total), 0)
    FROM TransaccionFolio t
    WHERE t.tipo = :tipo
    AND t.fechaTransaccion BETWEEN :inicio AND :fin
""")
    BigDecimal obtenerIngresos(
            @Param("tipo") TipoTransaccion tipo,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

}
