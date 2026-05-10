package com.hotelBackend.repository;

import com.hotelBackend.model.TransaccionFolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransaccionFolioRepository extends JpaRepository<TransaccionFolio, Long> {

    List<TransaccionFolio> findByReservaId(Long reservaId);
}
