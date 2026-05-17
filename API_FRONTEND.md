# API para Frontend — PMS Hotelero (Backend)

> Documento corto tipo “contract” para que el frontend implemente flujos sin adivinar.

## 0) Base URL
- Local: `http://localhost:3030`
- Producción (Render): `https://<tu-servicio-render>`

Todos los endpoints de abajo son relativos a la Base URL.

---

## 1) Autenticación (JWT)
### 1.1 Login
**POST** `/auth/login`

Body JSON:
```json
{
  "username": "admin",
  "password": "tuPassword"
}
```

Respuesta (ejemplo: puede variar según tu implementación exacta):
```json
{
  "token": "<JWT>",
  "username": "admin",
  "authorities": ["RESERVA_VER", "RESERVA_CREAR"],
  "rol": "ADMIN"
}
```

### 1.2 Enviar token
En endpoints protegidos:
- Header: `Authorization: Bearer <JWT>`

**Errores comunes**:
- 401: token faltante / inválido
- 403: token válido, pero sin permiso (authority)

---

## 2) RBAC (permisos/authorities)
El backend valida permisos con `@PreAuthorize("hasAuthority('...')")`.

Permisos clave para el frontend:

### Reservas
- `RESERVA_VER`
- `RESERVA_CREAR`
- `RESERVA_EDITAR` (check-in / check-out)
- `RESERVA_CANCELAR`

### Folio
- `FOLIO_VER`
- `FOLIO_REGISTRAR`

### Inventario
- `INVENTARIO_VER`
- `INVENTARIO_GESTIONAR`

### Consumos
- `REGISTRAR_CONSUMO`

---

## 3) Formato de error
El backend devuelve un JSON tipo:
```json
{
  "code": 409,
  "message": "Conflicto de estado",
  "detail": "Solo se puede hacer checkout a una reserva EN_CASA. Estado actual: CONFIRMADA",
  "timestamp": "2026-05-17T13:04:35"
}
```

Códigos relevantes:
- 400: request inválido (validaciones / IllegalArgument)
- 401: no autenticado
- 403: sin permiso
- 404: no encontrado
- 409: conflicto de estado / regla de negocio
- 422: stock insuficiente
- 500: error no controlado

---

## 4) Endpoints principales (MVP PMS)

## 4.1 Reservas
### Listar
**GET** `/reservas`  
Permiso: `RESERVA_VER`

### Obtener por id
**GET** `/reservas/{id}`  
Permiso: `RESERVA_VER`

### Crear
**POST** `/reservas`  
Permiso: `RESERVA_CREAR`

Body JSON (`CrearReservaRequest`):
```json
{
  "fechaEntrada": "2026-06-01",
  "fechaSalida": "2026-06-03",
  "cantidadHuespedes": 2,
  "nombreHuesped": "Juan Perez",
  "documentoHuesped": "72345678",
  "tipoHabitacionId": 1
}
```

Notas:
- El backend setea `estado = CONFIRMADA`.
- El backend setea `creadoPor` en base al usuario logueado.

### Check-in
**PUT** `/reservas/{id}/checkin`  
Permiso: `RESERVA_EDITAR`

Reglas (para UX):
- Debe estar `CONFIRMADA`
- `hoy` no puede ser antes de `fechaEntrada`
- `hoy` no puede ser después de `fechaSalida`
- Habitación asignada y disponible

### Check-out
**PUT** `/reservas/{id}/checkout`  
Permiso: `RESERVA_EDITAR`

Reglas:
- Debe estar `EN_CASA`

### Cancelar
**PUT** `/reservas/{id}/cancelar`  
Permiso: `RESERVA_CANCELAR`

Reglas:
- No se puede cancelar una `SALIDA_CHECKOUT`

---

## 4.2 Folios (transacciones por reserva)
### Ver transacciones
**GET** `/folios/reservas/{reservaId}/transacciones`  
Permiso: `FOLIO_VER`

### Ver saldo
**GET** `/folios/reservas/{reservaId}/saldo`  
Permiso: `FOLIO_VER`

### Registrar pago
**POST** `/folios/reservas/{reservaId}/pagos?monto=100`  
Permiso: `FOLIO_REGISTRAR`

### Registrar descuento
**POST** `/folios/reservas/{reservaId}/descuentos?descripcion=Promo&monto=20`  
Permiso: `FOLIO_REGISTRAR`

---

## 4.3 Consumos de una reserva (impacta Inventario + Folio)
**POST** `/reservas/{reservaId}/consumos`  
Permiso: `REGISTRAR_CONSUMO`

Body JSON (`RegistrarConsumoRequest`):
```json
{
  "articuloId": 5,
  "cantidad": 2
}
```

Comportamiento:
- Valida que la reserva esté `EN_CASA` (si no → 409)
- Valida stock suficiente (si no → 422)
- Registra movimiento inventario (SALIDA)
- Baja stock
- Genera transacción folio `CARGO_CONSUMO`

---

## 4.4 Inventario
### Artículos
- **GET** `/inventario/articulos` (permiso `INVENTARIO_VER`)
- **POST** `/inventario/articulos` (permiso `INVENTARIO_GESTIONAR`)

### Movimientos / stock
- **POST** `/inventario/{articuloId}/entrada?cantidad=10&motivo=Compra` (permiso `INVENTARIO_GESTIONAR`)
- **POST** `/inventario/{articuloId}/salida?cantidad=1&motivo=Uso` (permiso `INVENTARIO_GESTIONAR`)
- **GET** `/inventario/articulo/{articuloId}` (permiso `INVENTARIO_VER`)
- **GET** `/inventario/alertas/stock-minimo` (permiso `INVENTARIO_VER`)

---

## 5) Recomendación de implementación Frontend (muy concreta)
- Guardar token al hacer login.
- Usar interceptor para agregar `Authorization`.
- Para cada acción, si llega:
  - 401 → mandar a login
  - 403 → mostrar “No tienes permisos”
  - 409/422 → mostrar detalle en UI (mensaje de negocio)

---

## 6) Nota importante (cambio respecto a versiones antiguas)
- Ya **no** se envía `usuarioId` por query param en endpoints que registran auditoría.
- El backend toma el usuario desde el JWT (cuando aplica).

