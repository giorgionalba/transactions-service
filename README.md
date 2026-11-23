# Transactions Service

Servicio REST que registra transacciones en memoria y permite consultarlas por tipo y calcular el monto total de una transacción junto con todas las que dependen de ella.

## Requisitos

- Java 21, o Docker

No hace falta instalar Maven: el proyecto incluye el Maven wrapper (`./mvnw`). La primera vez, el wrapper descarga Maven y las dependencias, así que necesita conexión a internet.

## Cómo correrlo

Con Maven:

```bash
./mvnw spring-boot:run
```

Con Docker:

```bash
docker build -t transactions-service .
docker run --rm -p 8080:8080 transactions-service
```

En los dos casos, el servicio queda escuchando en `http://localhost:8080`.

## Tests

```bash
./mvnw verify
```

Corre los tests unitarios, los de integración y el test de aceptación con el escenario de ejemplo. GitHub Actions los ejecuta en cada pull request.

## API

| Método | Ruta | Descripción |
|---|---|---|
| `PUT` | `/transactions/{id}` | Registra una transacción nueva |
| `GET` | `/transactions/types/{type}` | Devuelve los ids de las transacciones de un tipo, en orden ascendente |
| `GET` | `/transactions/sum/{id}` | Devuelve la suma de la transacción y de todas sus descendientes |

### Ejemplo

```bash
curl -X PUT localhost:8080/transactions/10 \
  -H 'Content-Type: application/json' \
  -d '{ "amount": 5000, "type": "cars" }'
# {"status":"ok"}

curl -X PUT localhost:8080/transactions/11 \
  -H 'Content-Type: application/json' \
  -d '{ "amount": 10000, "type": "shopping", "parent_id": 10 }'
# {"status":"ok"}

curl -X PUT localhost:8080/transactions/12 \
  -H 'Content-Type: application/json' \
  -d '{ "amount": 5000, "type": "shopping", "parent_id": 11 }'
# {"status":"ok"}

curl localhost:8080/transactions/types/cars
# [10]

curl localhost:8080/transactions/sum/10
# {"sum":20000.0}

curl localhost:8080/transactions/sum/11
# {"sum":15000.0}
```

### Body de `PUT /transactions/{id}`

| Campo | Tipo | Obligatorio | Reglas |
|---|---|---|---|
| `amount` | número | Sí | Acepta cero y negativos |
| `type` | texto | Sí | No puede estar en blanco. Distingue mayúsculas y minúsculas |
| `parent_id` | entero | No | Mayor a 0. La transacción padre tiene que existir |

El `{id}` de la URL tiene que ser un entero mayor a 0. Las transacciones son inmutables: un id ya registrado no se puede volver a usar.

### Errores

| Código | Cuándo |
|---|---|
| `400 Bad Request` | Body mal formado o inválido, o un id que no es un entero mayor a 0 |
| `404 Not Found` | La transacción padre no existe (`PUT`), o la transacción consultada no existe (`GET sum`) |
| `409 Conflict` | Ya existe una transacción con ese id |

Los errores siguen el formato `ProblemDetail` (RFC 9457), con content type `application/problem+json`:

```json
{
  "detail": "Parent transaction 999 does not exist",
  "status": 404,
  "title": "Not Found"
}
```

## Diseño

- **Arquitectura en capas** (`api`, `service`, `domain`, `persistence`) con el repositorio definido como interfaz en el dominio: la lógica de negocio no depende del almacenamiento.
- **Almacenamiento en memoria** thread-safe, con índices por tipo y por padre para responder sin recorrer todas las transacciones.
- **Transacciones inmutables** y padre obligatoriamente existente: las jerarquías nunca pueden formar ciclos.
- **Suma con recorrido iterativo**, sin recursión, para soportar jerarquías de cualquier profundidad.

Más detalle en:

- [`docs/REQUIREMENTS.md`](docs/REQUIREMENTS.md): requisitos del servicio
- [`docs/DECISIONS.md`](docs/DECISIONS.md): decisiones de diseño y su justificación
- [`docs/PLAN.md`](docs/PLAN.md): diseño de alto nivel y plan de implementación

## Limitaciones conocidas

- **Los datos viven en memoria:** se pierden al reiniciar, no hay límite de tamaño y no se comparten entre varias instancias del servicio.
- **Los montos son `double`:** las sumas con decimales pueden tener errores de redondeo (por ejemplo, `0.1 + 0.2` da `0.30000000000000004`).
