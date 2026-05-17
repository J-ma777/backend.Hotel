# Guía de Despliegue - Backend PMS Hotelero

## 📦 Artefactos Generados

**JAR Compilado:**
```
target/backendHotel-0.0.1-SNAPSHOT.jar
Tamaño: 231.3 MB
Java Version: 21
```

---

## 🚀 Pasos de Despliegue a Render

### 1. Verificar que el JAR se generó correctamente

```bash
# Desde la carpeta del proyecto
.\mvnw.cmd clean install -DskipTests

# Verificar JAR
dir target\backendHotel-0.0.1-SNAPSHOT.jar
```

### 2. Hacer push del código a GitHub

```bash
git add -A
git commit -m "Backend completo: CHECK-IN, CHECK-OUT, validaciones, RBAC, tests"
git push origin main
```

### 3. Render se redeploy automáticamente

- Render detectará cambios en GitHub
- Ejecutará: `mvn clean install`
- Levantará nueva instancia con JAR actualizado
- PostgreSQL en Render mantiene datos

---

## ✅ Verificación Post-Despliegue

### 1. Verificar que el servicio levanta

```bash
# Ver logs en Render dashboard
# Debe aparecer: "Started BackendHotelApplication in X seconds"
```

### 2. Probar endpoints principales con Postman/cURL

**Login (obtener token):**
```bash
POST http://backend-render-url:3030/auth/login
{
  "username": "admin",
  "password": "admin123"  # Cambiar por credencial real
}

Response:
{
  "token": "eyJhbGc...",
  "userId": 1,
  "username": "admin",
  "authorities": ["RESERVA_VER", "RESERVA_CREAR", ...],
  "roles": ["ADMIN"]
}
```

**Crear Reserva:**
```bash
POST http://backend-render-url:3030/reservas
Authorization: Bearer {token}
{
  "fechaEntrada": "2026-05-20",
  "fechaSalida": "2026-05-22",
  "cantidadHuespedes": 2,
  "nombreHuesped": "Juan Pérez",
  "documentoHuesped": "12345678",
  "tipoHabitacionId": 1
}
```

**Check-In:**
```bash
PUT http://backend-render-url:3030/reservas/1/checkin
Authorization: Bearer {token}
```

**Check-Out:**
```bash
PUT http://backend-render-url:3030/reservas/1/checkout
Authorization: Bearer {token}
```

### 3. Verificar Códigos HTTP Correctos

- **200 OK** - Operación exitosa
- **400 Bad Request** - Validación de fechas fallida
- **401 Unauthorized** - Token inválido/expirado
- **403 Forbidden** - Usuario sin permiso
- **404 Not Found** - Recurso no existe
- **409 Conflict** - Estado inválido / Habitación no disponible
- **422 Unprocessable Entity** - Stock insuficiente
- **500 Internal Server Error** - Error del servidor

---

## 🔍 Troubleshooting

### Error: "Habitación no disponible"
**Causa:** Habitación no existe, está ocupada, o fuera de servicio  
**Solución:** Verificar estado de habitación en base de datos

### Error: "Transición de estado no permitida"
**Causa:** Intento de check-out sin haber hecho check-in  
**Solución:** Seguir flujo correcto: CONFIRMADA → EN_CASA → SALIDA_CHECKOUT

### Error: "Validación de fechas inválida"
**Causa:** Fecha de entrada en el futuro o fecha de salida en el pasado  
**Solución:** Usar fechas actuales o futuras cercanas

### Error: "Cantidad de huéspedes excede capacidad"
**Causa:** Huéspedes > capacidad máxima de tipo habitación  
**Solución:** Elegir tipo de habitación con capacidad suficiente

---

## 📊 Monitoreo

### Logs Importantes (Render Dashboard)

```
INFO: Started BackendHotelApplication in X seconds (process running for Y)
INFO: Tomcat initialized with port 3030 (http)
INFO: Bootstrapping Spring Data JPA repositories in DEFAULT mode
INFO: Found X JPA repository interfaces
INFO: HikariPool-1 - Start completed
INFO: Will secure any request with [... filters ...]
```

### Alertas

- **Cold Start:** Primer request después de inactividad tarda ~10 segundos (Render Free)
- **Memory:** Si excede 512MB disponibles, Render reinicia automáticamente
- **Errors:** Ver en pestaña "Logs" de Render dashboard

---

## 🔐 Variables de Entorno (Ya Configuradas)

```properties
DATABASE_URL=postgresql://...render.com/dbname
DATABASE_USER=...
DATABASE_PASSWORD=...
JWT_SECRET=DzCmK7EdbeZGLxmrxPBSp0k6wQYaZFWGJy8j5FAuBREbXoXsocQrWeG+0V//3/oO+6e6vTT0fU0lWaMs8DaJVQ==
PORT=3030
```

---

## 📝 Cambios en API

### Ahora Requieren userId del Usuario Autenticado

Estos endpoints ya no requieren parámetro `usuarioId`:

```bash
# Antes:
POST /limpieza/habitacion/1?estadoNuevo=SUCIA&usuarioId=1

# Ahora:
POST /limpieza/habitacion/1?estadoNuevo=SUCIA
# userId se obtiene del JWT
```

Lo mismo para:
- `/mantenimiento/{id}/resolver` 
- `/folios/reservas/{id}/consumos`
- `/folios/reservas/{id}/pagos`
- `/folios/reservas/{id}/descuentos`
- `/reservas/{id}/consumos`

---

## ✅ Checklist Final

- [ ] Código compilado sin errores
- [ ] Tests pasados (16/16)
- [ ] JAR generado en `target/`
- [ ] Código pusheado a GitHub
- [ ] Render redeploy automático completado
- [ ] POST /auth/login funciona (retorna token)
- [ ] POST /reservas funciona con token válido
- [ ] PUT /reservas/{id}/checkin funciona
- [ ] PUT /reservas/{id}/checkout funciona
- [ ] Códigos HTTP correctos en respuestas
- [ ] Mensajes de error en español descriptivos
- [ ] Sin errores 401/403 para usuarios con permisos correctos

---

## 🎉 ¡Listo para Producción!

El backend está completamente funcional con:
- ✅ Autenticación JWT (stateless)
- ✅ RBAC basado en permisos
- ✅ Validaciones robustas
- ✅ Manejo centralizado de errores
- ✅ Tests unitarios completos
- ✅ Auditoría con userId real
- ✅ Código limpio y profesional

---

**Fecha:** 2026-05-17  
**Versión:** 0.0.1-SNAPSHOT  
**Status:** 🟢 LISTO PARA DESPLIEGUE

