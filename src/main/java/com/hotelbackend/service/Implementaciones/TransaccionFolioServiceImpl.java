package com.hotelbackend.service.Implementaciones;

import com.hotelbackend.dto.response.FolioResumenResponse;
import com.hotelbackend.exception.ArticuloNoEncontradoException;
import com.hotelbackend.exception.ReservaNoEnCasaException;
import com.hotelbackend.exception.StockInsuficienteException;
import com.hotelbackend.model.ArticuloInventario;
import com.hotelbackend.model.MovimientoInventario;
import com.hotelbackend.model.Reserva;
import com.hotelbackend.model.TransaccionFolio;
import com.hotelbackend.model.enums.EstadoReserva;
import com.hotelbackend.model.enums.TipoMovimiento;
import com.hotelbackend.model.enums.TipoTransaccion;
import com.hotelbackend.repository.ArticuloInventarioRepository;
import com.hotelbackend.repository.MovimientoInventarioRepository;
import com.hotelbackend.repository.ReservaRepository;
import com.hotelbackend.repository.TransaccionFolioRepository;
import com.hotelbackend.service.TransaccionFolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
        transaccion.setFechaTransaccion(LocalDateTime.now(ZoneId.systemDefault()));
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
        movimiento.setFechaMovimiento(LocalDateTime.now(ZoneId.systemDefault()));
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
        transaccion.setFechaTransaccion(LocalDateTime.now(ZoneId.systemDefault()));
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

    @Override
    public BigDecimal obtenerIngresos(LocalDate inicio, LocalDate fin) {

        return transaccionFolioRepository.obtenerIngresos(
                TipoTransaccion.CARGO_NOCHE,
                inicio.atStartOfDay(),
                fin.atTime(23, 59, 59)
        );
    }
}