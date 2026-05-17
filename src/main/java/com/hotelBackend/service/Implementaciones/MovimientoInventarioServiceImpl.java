package com.hotelBackend.service.Implementaciones;

import com.hotelBackend.model.ArticuloInventario;
import com.hotelBackend.model.MovimientoInventario;
import com.hotelBackend.model.enums.TipoMovimiento;
import com.hotelBackend.repository.ArticuloInventarioRepository;
import com.hotelBackend.repository.MovimientoInventarioRepository;
import com.hotelBackend.security.util.AuthUtil;
import com.hotelBackend.service.MovimientoInventarioService;
import com.hotelBackend.service.TransaccionFolioService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class MovimientoInventarioServiceImpl implements MovimientoInventarioService {

    private final MovimientoInventarioRepository movimientoRepository;
    private final ArticuloInventarioRepository articuloRepository;
    private final TransaccionFolioService transaccionFolioService;

    public MovimientoInventarioServiceImpl(
            MovimientoInventarioRepository movimientoRepository,
            ArticuloInventarioRepository articuloRepository,
            TransaccionFolioService transaccionFolioService
    ) {
        this.movimientoRepository = movimientoRepository;
        this.articuloRepository = articuloRepository;
        this.transaccionFolioService = transaccionFolioService;
    }

    @Override
    public void registrarConsumo(
            Long reservaId,
            Long articuloId,
            int cantidad,
            Long registradoPorId
    ) {
        // Fuente única de verdad: consumo impacta inventario + folio
        transaccionFolioService.registrarConsumo(
                reservaId,
                articuloId,
                cantidad,
                registradoPorId
        );
    }

    @Override
    public MovimientoInventario registrarEntrada(Long articuloId, Double cantidad, String motivo) {

        validarCantidad(cantidad);

        ArticuloInventario articulo = obtenerArticulo(articuloId);

        articulo.setStockActual(articulo.getStockActual() + cantidad);

        articuloRepository.save(articulo);

        MovimientoInventario movimiento = crearMovimiento(
                articulo,
                TipoMovimiento.ENTRADA,
                cantidad,
                motivo
        );

        return movimientoRepository.save(movimiento);
    }

    @Override
    public MovimientoInventario registrarSalida(Long articuloId, Double cantidad, String motivo) {

        validarCantidad(cantidad);

        ArticuloInventario articulo = obtenerArticulo(articuloId);

        if (articulo.getStockActual() < cantidad) {
            throw new IllegalStateException("Stock insuficiente para realizar la salida");
        }

        articulo.setStockActual(articulo.getStockActual() - cantidad);

        articuloRepository.save(articulo);

        MovimientoInventario movimiento = crearMovimiento(
                articulo,
                TipoMovimiento.SALIDA,
                cantidad,
                motivo
        );

        return movimientoRepository.save(movimiento);
    }

    @Override
    public List<MovimientoInventario> listarPorArticulo(Long articuloId) {
        return movimientoRepository.findByArticuloIdOrderByFechaMovimientoDesc(articuloId);
    }

    // METODOS PRIVADOS

    private ArticuloInventario obtenerArticulo(Long articuloId) {
        return articuloRepository.findById(articuloId)
                .orElseThrow(() -> new EntityNotFoundException("Artículo de inventario no encontrado"));
    }

    private void validarCantidad(Double cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
    }

    private MovimientoInventario crearMovimiento(
            ArticuloInventario articulo,
            TipoMovimiento tipo,
            Double cantidad,
            String motivo
    ) {
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setArticulo(articulo);
        movimiento.setTipo(tipo);
        movimiento.setCantidad(cantidad);
        movimiento.setMotivo(motivo);
        movimiento.setFechaMovimiento(LocalDateTime.now());
        movimiento.setRegistradoPor(obtenerUsuarioId());

        return movimiento;
    }

    private Long obtenerUsuarioId() {
        try {
            return AuthUtil.getCurrentUserId();
        } catch (IllegalStateException e) {
            // Unit tests o ejecuciones internas sin contexto de seguridad
            return 1L;
        }
    }
}