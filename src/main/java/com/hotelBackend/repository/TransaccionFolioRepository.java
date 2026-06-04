package com.hotelBackend.repository;

import com.hotelBackend.model.TransaccionFolio;
import com.hotelBackend.model.enums.TipoTransaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
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

}
