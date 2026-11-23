# CLAUDE.md

Guía para trabajar en este repo (para Claude y para cualquier persona que contribuya).

## Contexto

**Transactions Service** es un servicio REST que guarda transacciones **en memoria** y responde consultas sobre ellas:

- `PUT /transactions/{id}` crea una transacción (`amount`, `type`, `parent_id` opcional).
- `GET /transactions/types/{type}` devuelve los ids de ese tipo.
- `GET /transactions/sum/{id}` devuelve la suma de la transacción más todos sus descendientes.

Documentación:

- [`docs/REQUIREMENTS.md`](docs/REQUIREMENTS.md): qué tiene que hacer el servicio.
- [`docs/DECISIONS.md`](docs/DECISIONS.md): decisiones de diseño y reglas de negocio.
- [`docs/PLAN.md`](docs/PLAN.md): diseño de alto nivel y plan de PRs.

Cómo se trabaja:

- Toda decisión de diseño no trivial queda registrada en [`docs/DECISIONS.md`](docs/DECISIONS.md) con su justificación.
- Si algo no está definido por los requisitos ni por `DECISIONS.md`, **se pregunta antes de implementar**. No se asume.
- Se prefiere la solución simple y explicable antes que la "ingeniosa".

## Stack

| Qué | Elección |
|---|---|
| Lenguaje | Java 21 (LTS) |
| Framework | Spring Boot 4.2.x (Spring MVC) |
| Build | Maven con wrapper (`./mvnw`) |
| Tests | JUnit 6 (Jupiter), AssertJ, MockMvc (`@SpringBootTest`) |
| Validación | Jakarta Bean Validation (`spring-boot-starter-validation`) |
| Errores | `ProblemDetail` (RFC 9457) vía `@RestControllerAdvice` |
| Persistencia | En memoria con `ConcurrentHashMap`, **sin SQL** |
| Contenedor | Dockerfile multi-stage, JRE 21, usuario no-root |
| CI | GitHub Actions |

**No se usa:** Lombok (los `record` cubren el caso), bases de datos, H2, ni librerías que no se puedan justificar en una frase.

## Comandos

```bash
./mvnw test                  # unit + integración
./mvnw verify                # build completo
./mvnw spring-boot:run       # levanta en :8080
docker build -t transactions-service .
docker run --rm -p 8080:8080 transactions-service
```

## Arquitectura

Arquitectura en capas con **puertos y adaptadores** (hexagonal) en la persistencia: el dominio define el puerto `TransactionRepository` y la persistencia lo implementa. El dominio no conoce ni Spring MVC ni la implementación de almacenamiento. El diagrama y la justificación están en [`docs/DECISIONS.md`](docs/DECISIONS.md#7-arquitectura-en-capas-con-puertos-y-adaptadores).

```
io.github.giorgionalba.transactions
├── api/            ← HTTP: controller, DTOs request/response, exception handler
├── service/        ← casos de uso / reglas de negocio (TransactionService)
├── domain/         ← Transaction (record), excepciones de dominio, puerto TransactionRepository
└── persistence/    ← InMemoryTransactionRepository (implementa el puerto)
```

Reglas de dependencia (las flechas sólo apuntan hacia `domain`):

- `api → service → domain ← persistence`
- `domain` no importa nada de `api`, `service` ni `persistence`, ni de Spring Web.
- Los DTOs HTTP (`snake_case`, `parent_id`) **no** salen de `api/`. El controller mapea DTO ↔ dominio.
- Las excepciones de dominio (`TransactionAlreadyExistsException`, `TransactionNotFoundException`, ...) se traducen a HTTP **sólo** en el `@RestControllerAdvice`.

## Principios SOLID aplicados acá

- **S — Single Responsibility:** cada clase tiene un motivo para cambiar. El controller sólo traduce HTTP, el service sólo aplica reglas de negocio y el repository sólo guarda e indexa. Si una clase empieza a hacer dos cosas, se separa.
- **O — Open/Closed:** agregar otro almacenamiento (Redis, por ejemplo) = nueva implementación de `TransactionRepository`, sin tocar el service.
- **L — Liskov:** cualquier implementación del repository tiene que respetar el mismo contrato (por ejemplo, que `saveIfAbsent` sea atómico). El Javadoc de la interfaz lo documenta.
- **I — Interface Segregation:** el puerto expone sólo lo que el service usa. Nada de métodos "por si acaso".
- **D — Dependency Inversion:** el service depende de la interfaz `TransactionRepository` (definida en `domain`), no de la clase en memoria. La inyección es **por constructor**, nunca `@Autowired` en campos.

## Buenas prácticas de código

- **Nombres que expliquen la intención.** Nada de `data`, `tmp`, `util`, `manager`. Los métodos son verbos (`create`, `sumWithDescendants`).
- **Métodos cortos** con un solo nivel de abstracción. Si hace falta un comentario para explicar *qué* hace un bloque, va un método con buen nombre. Los comentarios son para el *porqué*.
- **Inmutabilidad por defecto:** `record` para el dominio y los DTOs, campos `final` y colecciones devueltas como copias inmutables (`List.copyOf`).
- **Fallar rápido:** se valida en el borde (Bean Validation en el DTO) y las invariantes en el dominio. No se devuelve `null`: se usa `Optional` en lecturas del repository.
- **Sin estado mutable compartido** fuera del repository. El repository es thread-safe (servidor web = concurrencia).
- **Sin recursión para recorrer el árbol:** iterativo con `ArrayDeque`, así una cadena larga de padres no causa `StackOverflowError`.
- **Sin números mágicos ni strings repetidos.** Se usan constantes con nombre.
- **YAGNI:** no se agregan endpoints, capas ni configuraciones que los requisitos no pidan. Lo opcional se discute antes.
- **Formato:** 4 espacios e imports explícitos (sin `*`). No se deja código comentado ni `System.out`.

## Testing (TDD)

Ciclo **red → green → refactor**, visible en los commits:

1. `test: ...` agrega un test que falla. **El commit rojo siempre compila:** si hace falta, incluye la firma mínima del método o la clase de la excepción, para que el test falle por la aserción y no por compilación.
2. `feat: ...` hace lo mínimo para que pase.
3. `refactor: ...` (si hace falta) mejora sin cambiar comportamiento.

Pirámide:

- **Unit** — `TransactionService` (con el repository en memoria real como *fake*) e `InMemoryTransactionRepository`. Sin Spring.
- **Integración** — `@SpringBootTest` + MockMvc contra el contexto completo. **El escenario de ejemplo de la API es un test de aceptación** y tiene que pasar tal cual.
- Cada test tiene un caso por comportamiento, con nombre descriptivo (`shouldReturn409WhenTransactionAlreadyExists`) y estructura Given / When / Then.
- Los tests son independientes:
  - Los de integración usan `@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)`, así cada test arranca con un contexto y un repository nuevos.
  - Los unitarios crean su propio repository con `new`.
  - No se agregan métodos al puerto (como `clear()`) sólo para los tests.
- Se prefieren *fakes* (el repository en memoria real) antes que mocks: los tests verifican comportamiento, no interacciones.
- Se cubren los casos borde: padre inexistente, id duplicado, tipo sin transacciones, suma de una hoja y cadena larga.
- Los montos son `double`: se comparan con tolerancia (`isCloseTo`, `closeTo`), nunca con igualdad exacta cuando hay decimales.

## Idioma

| Qué | Idioma |
|---|---|
| Código (clases, métodos, variables, comentarios, tests) | Inglés |
| Mensajes de commit | Español, en Conventional Commits (el tipo y el scope quedan en inglés) |
| PRs (título y descripción) | Español |
| Documentación (`README`, `docs/`) | Español |

## Git y PRs

- **Nunca commitear, pushear ni abrir PRs sin aprobación explícita del autor.**
- **Nunca agregar `Co-Authored-By` ni ninguna otra atribución a Claude o a una IA**, ni en commits ni en PRs.
- **Flujo:** cada funcionalidad se trabaja en una rama que sale de `main`, se abre un PR y, con el CI en verde, se mergea a `main`. Nunca se commitea directo a `main`.
- **Merge:** siempre con **merge commit**, nunca con squash. Así los commits del ciclo TDD quedan en el historial de `main` y se ve qué commits entraron en cada PR.
- Nombres de rama: `feat/...`, `fix/...`, `chore/...`, `docs/...`, `test/...`, `build/...`, `ci/...`.
- **Conventional Commits** con scope por capa o área: `tipo(scope): descripción`.
  - La descripción va en español, en presente, en minúscula y sin punto final. Los nombres de clases, endpoints y comandos quedan como están en el código.
  - Ejemplos: `feat(domain): agrega el record Transaction`, `feat(api): agrega el endpoint PUT /transactions/{id}`, `test(service): cubre el caso de id duplicado`.
  - Los tipos son `feat`, `fix`, `test`, `refactor`, `docs`, `chore`, `build` y `ci`.
  - Los scopes son `domain`, `service`, `persistence`, `api`, `docker` y `ci`. Si un cambio es transversal, va sin scope.
- Commits chicos y atómicos: cada uno compila y deja los tests en verde, salvo el commit `test:` rojo del ciclo TDD.
- PRs chicos, uno por funcionalidad, en español, con el template de [`.github/pull_request_template.md`](.github/pull_request_template.md). Siempre llevan las secciones **"Qué"** y **"Cambios"**.
- **CI:** GitHub Actions corre `./mvnw verify` en cada PR y en cada push a `main` ([`.github/workflows/ci.yml`](.github/workflows/ci.yml)). No se mergea un PR con el CI en rojo.

## Antes de dar algo por terminado

- [ ] `./mvnw verify` en verde.
- [ ] Hay tests para el comportamiento nuevo, incluidos los casos borde.
- [ ] Si se tomó una decisión nueva, está en `docs/DECISIONS.md`.
- [ ] Si cambió algo visible (endpoint, comando o error), el README está actualizado.
- [ ] La imagen Docker buildea y responde.
