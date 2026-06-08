package com.hotelbackend.service.Implementaciones;

import com.hotelbackend.model.ArticuloInventario;
import com.hotelbackend.repository.ArticuloInventarioRepository;
import com.hotelbackend.service.ArticuloInventarioService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class ArticuloInventarioServiceImpl implements ArticuloInventarioService {

    private final ArticuloInventarioRepository articuloInventarioRepository;

    public ArticuloInventarioServiceImpl(
            ArticuloInventarioRepository articuloInventarioRepository) {
        this.articuloInventarioRepository = articuloInventarioRepository;
    }

    @Override
    public ArticuloInventario crear(ArticuloInventario articulo) {

        if (articulo.getCostoUnitario() == null) {
            throw new IllegalArgumentException("El costo unitario es obligatorio");
        }

        return articuloInventarioRepository.save(articulo);
    }

    @Override
    public List<ArticuloInventario> listarTodos() {
        return articuloInventarioRepository.findAll();
    }

    @Override
    public ArticuloInventario obtenerPorId(Long id) {
        return articuloInventarioRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Artículo de inventario no encontrado"));
    }

    @Override
    public List<ArticuloInventario> obtenerArticulosConStockMinimo() {
        return articuloInventarioRepository
                .findConStockBajo();
    }
}