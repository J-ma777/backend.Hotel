package com.hotelBackend.service.Implementaciones;

import com.hotelBackend.model.ArticuloInventario;
import com.hotelBackend.repository.ArticuloInventarioRepository;
import com.hotelBackend.service.ArticuloInventarioService;
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

        if (articulo.getStockActual() == null) {
            articulo.setStockActual(0.0);
        }

        if (articulo.getStockMinimo() == null) {
            articulo.setStockMinimo(0.0);
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