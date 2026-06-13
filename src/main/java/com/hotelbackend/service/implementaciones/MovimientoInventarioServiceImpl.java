package com.hotelbackend.service.implementaciones;

import com.hotelbackend.model.ArticuloInventario;
import com.hotelbackend.model.MovimientoInventario;
import com.hotelbackend.model.enums.TipoMovimiento;
import com.hotelbackend.repository.ArticuloInventarioRepository;
import com.hotelbackend.repository.MovimientoInventarioRepository;
import com.hotelbackend.service.MovimientoInventarioService;
import com.hotelbackend.service.TransaccionFolioService;
import com.hotelbackend.security.util.AuthUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MovimientoInventarioServiceImpl implements MovimientoInventarioService {

    private final MovimientoInventarioRepository movimientoRepository;
    private final ArticuloInventarioRepository articuloRepository;
    private final TransaccionFolioService transaccionFolioService;

    //Cosumo reserva, se registra la salida en inventario y el consumo en folio
    @Override
    public void registrarConsumo(
            Long reservaId,
            Long articuloId,
            int cantidad,
            Long registradoPorId
    ) {

        BigDecimal cantidadBD = BigDecimal.valueOf(cantidad);

        // 1. INVENTARIO (fuente de verdad)
        registrarSalidaInterna(
                articuloId,
                cantidadBD,
                "RESERVA#" + reservaId,
                registradoPorId
        );

        // 2. FOLIO
        transaccionFolioService.registrarConsumo(
                reservaId,
                articuloId,
                cantidad,
                registradoPorId
        );
    }

    // Entrada de nuevo stock al inventario, no afecta folio
    @Override
    public MovimientoInventario registrarEntrada(
            Long articuloId,
            BigDecimal cantidad,
            String motivo
    ) {
        validarCantidad(cantidad);

        ArticuloInventario articulo = obtenerArticulo(articuloId);

        log.info("Articulo ID: {}", articulo.getId());
        log.info("Stock actual: {}", articulo.getStockActual());
        log.info("Costo Unitario: {}", articulo.getCostoUnitario());
        log.info("Version: {}", articulo.getVersion());

        articulo.setStockActual(
                articulo.getStockActual().add(cantidad)
        );

        articuloRepository.save(articulo);

        MovimientoInventario movimiento = crearMovimiento(
                articulo,
                TipoMovimiento.ENTRADA,
                cantidad,
                motivo
        );

        return movimientoRepository.save(movimiento);
    }

    // Salida de stock del inventario
    @Override
    public MovimientoInventario registrarSalida(
            Long articuloId,
            BigDecimal cantidad,
            String motivo
    ) {
        validarCantidad(cantidad);

        return registrarSalidaInterna(
                articuloId,
                cantidad,
                motivo,
                obtenerUsuarioId()
        );
    }

    // Listado de movimientos por artículo, ordenados por fecha descendente
    @Override
    public List<MovimientoInventario> listarPorArticulo(Long articuloId) {
        return movimientoRepository
                .findByArticuloIdOrderByFechaMovimientoDesc(articuloId);
    }

    // Ajuste manual de stock, para corregir discrepancias detectadas en auditorías o conteos físicos
    @Override
    public MovimientoInventario ajustarStock(
            Long articuloId,
            BigDecimal nuevoStock,
            String motivo
    ) {
        validarCantidad(nuevoStock);

        ArticuloInventario articulo = obtenerArticulo(articuloId);

        BigDecimal stockActual = articulo.getStockActual();
        BigDecimal diferencia = nuevoStock.subtract(stockActual);

        if (diferencia.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalStateException("El stock ya coincide con el valor ingresado");
        }

        TipoMovimiento tipo = diferencia.compareTo(BigDecimal.ZERO) > 0
                ? TipoMovimiento.ENTRADA
                : TipoMovimiento.SALIDA;

        articulo.setStockActual(nuevoStock);
        articuloRepository.save(articulo);

        MovimientoInventario movimiento = crearMovimiento(
                articulo,
                tipo,
                diferencia.abs(),
                "AJUSTE: " + motivo
        );

        return movimientoRepository.save(movimiento);
    }

    // Core interno
    private MovimientoInventario registrarSalidaInterna(
            Long articuloId,
            BigDecimal cantidad,
            String motivo,
            Long usuarioId
    ) {
        validarCantidad(cantidad);

        ArticuloInventario articulo = obtenerArticulo(articuloId);

        // VALIDACIÓN CORRECTA CON BIGDECIMAL
        if (articulo.getStockActual().compareTo(cantidad) < 0) {
            throw new IllegalStateException("Stock insuficiente");
        }

        articulo.setStockActual(
                articulo.getStockActual().subtract(cantidad)
        );

        articuloRepository.save(articulo);

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setArticulo(articulo);
        movimiento.setTipo(TipoMovimiento.SALIDA);
        movimiento.setCantidad(cantidad);
        movimiento.setMotivo(motivo);
        movimiento.setFechaMovimiento(LocalDateTime.now(ZoneId.systemDefault()));
        movimiento.setRegistradoPor(usuarioId);

        return movimientoRepository.save(movimiento);
    }

    // Utilidades privadas
    private ArticuloInventario obtenerArticulo(Long articuloId) {
        return articuloRepository.findById(articuloId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Artículo no encontrado"));
    }

    private void validarCantidad(BigDecimal cantidad) {
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
    }

    private MovimientoInventario crearMovimiento(
            ArticuloInventario articulo,
            TipoMovimiento tipo,
            BigDecimal cantidad,
            String motivo
    ) {
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setArticulo(articulo);
        movimiento.setTipo(tipo);
        movimiento.setCantidad(cantidad);
        movimiento.setMotivo(motivo);
        movimiento.setFechaMovimiento(LocalDateTime.now(ZoneId.systemDefault()));
        movimiento.setRegistradoPor(obtenerUsuarioId());

        return movimiento;
    }

    private Long obtenerUsuarioId() {
        try {
            return AuthUtil.getCurrentUserId();
        } catch (Exception e) {
            return 1L; // fallback
        }
    }
}
