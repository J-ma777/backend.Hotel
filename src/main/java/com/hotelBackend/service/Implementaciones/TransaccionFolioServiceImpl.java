package com.hotelBackend.service.Implementaciones;

import com.hotelBackend.dto.response.FolioResumenResponse;
import com.hotelBackend.exception.ArticuloNoEncontradoException;
import com.hotelBackend.exception.ReservaNoEnCasaException;
import com.hotelBackend.exception.StockInsuficienteException;
import com.hotelBackend.model.ArticuloInventario;
import com.hotelBackend.model.MovimientoInventario;
import com.hotelBackend.model.Reserva;
import com.hotelBackend.model.TransaccionFolio;
import com.hotelBackend.model.enums.EstadoReserva;
import com.hotelBackend.model.enums.TipoMovimiento;
import com.hotelBackend.model.enums.TipoTransaccion;
import com.hotelBackend.repository.ArticuloInventarioRepository;
import com.hotelBackend.repository.MovimientoInventarioRepository;
import com.hotelBackend.repository.ReservaRepository;
import com.hotelBackend.repository.TransaccionFolioRepository;
import com.hotelBackend.service.TransaccionFolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransaccionFolioServiceImpl implements TransaccionFolioService {

    private final TransaccionFolioRepository transaccionFolioRepository;
    private final ReservaRepository reservaRepository;
    private final ArticuloInventarioRepository articuloInventarioRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;

    @Override
    @Transactional
    public TransaccionFolio registrarTransaccion(
            Long reservaId,
            TipoTransaccion tipo,
            String descripcion,
            BigDecimal precioUnitario,
            Integer cantidad,
            Long registradoPor
    ) {

        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        BigDecimal total = precioUnitario.multiply(BigDecimal.valueOf(cantidad));

        TransaccionFolio transaccion = new TransaccionFolio();
        transaccion.setReserva(reserva);
        transaccion.setTipo(tipo);
        transaccion.setDescripcion(descripcion);
        transaccion.setPrecioUnitario(precioUnitario);
        transaccion.setCantidad(cantidad);
        transaccion.setTotal(total);
        transaccion.setFechaTransaccion(LocalDateTime.now());
        transaccion.setRegistradoPor(registradoPor);

        return transaccionFolioRepository.save(transaccion);
    }

    @Override
    public List<TransaccionFolio> obtenerTransaccionesPorReserva(Long reservaId) {
        return transaccionFolioRepository.findByReservaId(reservaId);
    }

    @Override
    public BigDecimal obtenerSaldoReserva(Long reservaId) {

        List<TransaccionFolio> transacciones =
                transaccionFolioRepository.findByReservaId(reservaId);

        return transacciones.stream()
                .map(t -> {
                    switch (t.getTipo()) {
                        case PAGO:
                        case DESCUENTO:
                            return t.getTotal().negate();
                        default:
                            return t.getTotal();
                    }
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional
    public TransaccionFolio registrarConsumo(
            Long reservaId,
            Long articuloId,
            int cantidad,
            Long registradoPor
    ) {

        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        if (reserva.getEstado() != EstadoReserva.EN_CASA) {
            throw new ReservaNoEnCasaException(reservaId);
        }

        ArticuloInventario articulo = articuloInventarioRepository.findById(articuloId)
                .orElseThrow(() -> new ArticuloNoEncontradoException(articuloId));

        if (cantidad <= 0) {
            throw new RuntimeException("La cantidad debe ser mayor a cero");
        }

        BigDecimal cantidadBD = BigDecimal.valueOf(cantidad);

        // VALIDACIÓN CORRECTA
        if (articulo.getStockActual().compareTo(cantidadBD) < 0) {
            throw new StockInsuficienteException(
                    articulo.getNombre(),
                    articulo.getStockActual(),
                    cantidadBD
            );
        }

        // MOVIMIENTO INVENTARIO
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setArticulo(articulo);
        movimiento.setTipo(TipoMovimiento.SALIDA);
        movimiento.setCantidad(cantidadBD);
        movimiento.setFechaMovimiento(LocalDateTime.now());
        movimiento.setMotivo("Consumo habitación " + reserva.getHabitacion().getNumero());
        movimiento.setRegistradoPor(registradoPor);

        movimientoInventarioRepository.save(movimiento);

        // ACTUALIZAR STOCK
        articulo.setStockActual(
                articulo.getStockActual().subtract(cantidadBD)
        );
        articuloInventarioRepository.save(articulo);

        //  TRANSACCIÓN
        BigDecimal precioUnitario = articulo.getCostoUnitario();

        BigDecimal total = precioUnitario.multiply(cantidadBD);

        TransaccionFolio transaccion = new TransaccionFolio();
        transaccion.setReserva(reserva);
        transaccion.setTipo(TipoTransaccion.CARGO_CONSUMO);
        transaccion.setDescripcion("Consumo: " + articulo.getNombre() + " x" + cantidad);
        transaccion.setPrecioUnitario(precioUnitario);
        transaccion.setCantidad(cantidad);
        transaccion.setTotal(total);
        transaccion.setFechaTransaccion(LocalDateTime.now());
        transaccion.setRegistradoPor(registradoPor);

        return transaccionFolioRepository.save(transaccion);
    }

    @Override
    public FolioResumenResponse obtenerFolioResumen(Long reservaId) {

        BigDecimal cargos = transaccionFolioRepository.sumByReservaAndTipos(
                reservaId,
                List.of(
                        TipoTransaccion.CARGO_NOCHE,
                        TipoTransaccion.CARGO_CONSUMO
                )
        );

        BigDecimal pagos = transaccionFolioRepository.sumByReservaAndTipos(
                reservaId,
                List.of(TipoTransaccion.PAGO)
        );

        BigDecimal descuentos = transaccionFolioRepository.sumByReservaAndTipos(
                reservaId,
                List.of(TipoTransaccion.DESCUENTO)
        );

        BigDecimal totalCargosNeto = cargos.subtract(descuentos);
        BigDecimal balance = totalCargosNeto.subtract(pagos);

        return new FolioResumenResponse(
                totalCargosNeto,
                pagos,
                descuentos,
                balance
        );
    }
}