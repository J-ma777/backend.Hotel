package com.hotelBackend.service.Implementaciones;

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
    public TransaccionFolio registrarConsumo(
            Long reservaId,
            Long articuloId,
            int cantidad,
            Long registradoPor
    ) {
        // Implementacion para buscar reserva, validar que este EN-CASA.
        // Método
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() ->
                        new RuntimeException("Reserva no encontrada")
                );

        if (reserva.getEstado() != EstadoReserva.EN_CASA) {
            throw new RuntimeException(
                    "Solo se pueden registrar consumos a reservas EN_CASA"
            );
        }

        // Validación de Articulo de inventario y stock.
        ArticuloInventario articulo = articuloInventarioRepository
                .findById(articuloId)
                .orElseThrow(() ->
                        new RuntimeException("Artículo de inventario no encontrado")
                );

        if (cantidad <= 0) {
            throw new RuntimeException("La cantidad debe ser mayor a cero");
        }

        if (articulo.getStockActual() < cantidad) {
            throw new RuntimeException(
                    "Stock insuficiente para el artículo: " + articulo.getNombre()
            );
        }

        // Validar que todo consumo menora la cantidad de stock actual, para evitar registrar consumos parciales.
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setArticulo(articulo);
        movimiento.setTipo(TipoMovimiento.SALIDA);
        movimiento.setCantidad((double) cantidad);
        movimiento.setFechaMovimiento(LocalDateTime.now());
        movimiento.setMotivo("Consumo habitación " + reserva.getHabitacion().getNumero());
        movimiento.setRegistradoPor(registradoPor);

        // Movimiento de inventario ya guardado
        movimientoInventarioRepository.save(movimiento);

        // Actualizar stock del artículo
        articulo.setStockActual(
                articulo.getStockActual() - cantidad
        );
        articuloInventarioRepository.save(articulo);

        // Obtener el precio unitario del artículo para calcular el total de la transacción

        BigDecimal precioUnitario = articulo.getCostoUnitario(); // usa el getter REAL del modelo que es costoUnitario.

        // Calcular el total
        BigDecimal total = precioUnitario.multiply(
                BigDecimal.valueOf(cantidad)
        );

        TransaccionFolio transaccion = new TransaccionFolio();
        transaccion.setReserva(reserva);
        transaccion.setTipo(TipoTransaccion.CARGO_CONSUMO);
        transaccion.setDescripcion(
                "Consumo: " + articulo.getNombre() + " x" + cantidad
        );
        transaccion.setPrecioUnitario(precioUnitario);
        transaccion.setCantidad(cantidad);
        transaccion.setTotal(total);
        transaccion.setFechaTransaccion(LocalDateTime.now());
        transaccion.setRegistradoPor(registradoPor);

        return transaccionFolioRepository.save(transaccion);

    }
}