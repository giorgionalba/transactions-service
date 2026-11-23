# Requisitos

## Descripción

**Transactions Service** es un servicio REST que registra transacciones y responde consultas sobre ellas.

Cada transacción tiene un **tipo** y un **monto**, y puede estar vinculada a una transacción **padre**. Esos vínculos forman jerarquías de transacciones, y el servicio permite consultar el monto total de cualquier transacción junto con todas las que dependen de ella.

## Modelo de datos

| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `transaction_id` | `long` | Sí (en la URL) | Identificador de la transacción |
| `amount` | `double` | Sí | Monto de la transacción |
| `type` | `string` | Sí | Categoría de la transacción (por ejemplo, `cars` o `shopping`) |
| `parent_id` | `long` | No | Id de la transacción padre |

## Requisitos funcionales

### Registrar una transacción

```
PUT /transactions/{transaction_id}
```

```json
{ "amount": 5000, "type": "cars", "parent_id": 10 }
```

Registra una transacción nueva con el id indicado en la URL. Si la operación es exitosa, responde:

```json
{ "status": "ok" }
```

### Consultar transacciones por tipo

```
GET /transactions/types/{type}
```

Devuelve una lista JSON con los ids de todas las transacciones del tipo indicado:

```json
[10, 11, 12]
```

### Consultar el monto total de una transacción

```
GET /transactions/sum/{transaction_id}
```

Devuelve la suma de los montos de la transacción indicada y de todas las transacciones que dependen de ella, directa o indirectamente, a través de `parent_id`:

```json
{ "sum": 20000.0 }
```

## Escenario de ejemplo

| Request | Respuesta |
|---|---|
| `PUT /transactions/10` `{ "amount": 5000, "type": "cars" }` | `{ "status": "ok" }` |
| `PUT /transactions/11` `{ "amount": 10000, "type": "shopping", "parent_id": 10 }` | `{ "status": "ok" }` |
| `PUT /transactions/12` `{ "amount": 5000, "type": "shopping", "parent_id": 11 }` | `{ "status": "ok" }` |
| `GET /transactions/types/cars` | `[10]` |
| `GET /transactions/sum/10` | `{ "sum": 20000.0 }` (10 + 11 + 12) |
| `GET /transactions/sum/11` | `{ "sum": 15000.0 }` (11 + 12) |

Este escenario está cubierto por un test de aceptación.

## Requisitos no funcionales

- **Spring Boot** sobre **Java 21**.
- **Almacenamiento en memoria**, sin bases de datos SQL.
- **Tests de integración** que cubran la API.
- **Aplicación dockerizada.**
- **Diseño:** código claro, arquitectura en capas y principios SOLID.
- **Proceso:** TDD y desarrollo incremental con commits chicos.
- **Documentación** de uso y de las decisiones de diseño.

## Reglas de negocio

Los comportamientos que no están definidos arriba (ids duplicados, padres inexistentes, códigos de error, orden de los resultados, etc.) están detallados en [`DECISIONS.md`](DECISIONS.md).
