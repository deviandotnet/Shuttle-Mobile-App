---
name: android-data-layer
description: >
  Data layer patterns for the Shuttle app - Retrofit APIs (AuthApi, ShuttleApi) against the
  ASP.NET Core backend, DTOs, Room entities and DAOs, mappers, XRepository / XRepositoryImpl,
  Dispatchers.IO, token storage, and the offline-first shuttle pass draft/finalize/sync/purge
  lifecycle. Use this skill whenever writing or reviewing a repository, adding an endpoint, DTO,
  Room entity or DAO, writing a mapper, changing the database schema, or touching sync. Trigger on
  phrases like "create a repository", "add an endpoint", "Retrofit", "DTO", "add a DAO",
  "Room entity", "write a mapper", "network call", "sync shuttle passes", "offline-first",
  "database version", or "token storage".
---

# Data Layer (Retrofit + Room)

## Project context

| Path | Contents |
|---|---|
| `config/API.kt` | `BASE_URL = "http://10.0.2.2:5105/api/"`, the emulator alias for a backend running locally on port 5105 |
| `config/Database.kt` | `ShuttleDatabase`: entity list, DAO accessors, `version = 26`, `exportSchema = false` |
| `data/network/` | `AuthApi` (login/register), `ShuttleApi` (providers, routes, shuttles, shuttle-pass upload) |
| `data/network/request/`, `response/` | DTOs |
| `data/local/`, `data/local/dao/` | Room entities and DAOs |
| `data/mappers/` | Extension-function mappers |
| `data/repositoryImpl/` | `XRepositoryImpl`, which implements the `domain/repository` interfaces |

## Rules

- **Naming (kept from the existing code):** `XRepository` interface in `domain/repository`, `XRepositoryImpl` in `data/repositoryImpl`, bound in `di/RepositoryModule.kt`. Use `XDto` for network, `XEntity` for Room, and plain `X` for the domain model. There is no separate "data source" layer; the repository talks to the DAO and API directly.
- **Repository interfaces speak domain only.** Parameters and return types are `domain/model` types (or primitives, `Flow`, `Resource`), never entities, DTOs or `retrofit2.Response`.
- **All I/O runs off the main thread** inside the repository: `withContext(Dispatchers.IO)` for `suspend` functions and `.flowOn(Dispatchers.IO)` for `flow {}` builders. Room `suspend`/`Flow` DAO functions already run on their own executor.
- **Errors become `Resource`** in the repository (see **android-error-handling**).
- **DTO field names must match the backend's camelCase JSON.** Retrofit uses `GsonConverterFactory`, so:
  - annotate fields with Gson's `@SerializedName("camelCaseName")`;
  - don't rely on kotlinx `@Serializable`/`@SerialName`. Gson ignores them, and many existing DTOs carry `@Serializable` with no effect.
  - `.cursorrules` prefers kotlinx.serialization for new DTOs, but that only works after switching the converter in `AppModule` to the kotlinx converter for *all* APIs. Treat that as its own migration task, and do it only when asked.
  - Release builds are minified by R8. Gson reads fields by reflection, so a DTO field without `@SerializedName` (and with no working keep rule) can be renamed and break JSON **only in release**. This is one more reason every DTO field needs `@SerializedName`.
- **Room is the source of truth for shuttle passes.** The UI observes DAO `Flow`s, and network results are written to Room, never shown directly.
- **Schema changes:** bump `version` in `config/Database.kt` and register new entities/DAOs there. `fallbackToDestructiveMigration` means a version bump **wipes local data, including unsynced passes**. Before shipping a schema change, add a `Migration` (or enable `exportSchema` + `AutoMigration`) and remove the destructive fallback. Flag this to the user whenever you bump the version.
- **Tokens and PII** go in `EncryptedSharedPreferences` (or an encrypted DB), per `.cursorrules`. Never store them in plain Room tables or plain SharedPreferences.

## Patterns

### Retrofit API

```kotlin
interface ShuttleApi {
    @GET("routes/")
    suspend fun getRoutes(): Response<List<RouteDto>>

    @POST("shuttlepasses/")
    suspend fun postShuttlePassWithPassenger(
        @Body shuttlePasses: List<ShuttlePassWithPassengerDto>
    ): Response<ShuttlePassWithPassengerResponseBodyDto>
}
```

Return `Response<T>` so the repository can check the status code. Paths are relative to `BASE_URL` and end with `/`, like the existing endpoints.

### DTO

```kotlin
data class ShuttlePassWithPassengerDto(
    @SerializedName("id") val id: String,
    @SerializedName("driverId") val driverId: String,
    @SerializedName("shuttleId") val shuttleId: String,
    @SerializedName("isLateShuttle") val isLateShuttle: Boolean,
    @SerializedName("passengers") val passengers: List<PassengerDto>
)
```

Mark fields nullable when the backend may omit them.

### Mappers (`data/mappers/XMapper.kt`)

```kotlin
fun ShuttlePassEntity.toDomain(): ShuttlePass = ShuttlePass(id = id, driver = driver, /* ... */)
fun ShuttlePass.toEntity(): ShuttlePassEntity = ShuttlePassEntity(id = id, driver = driver, /* ... */)
fun ShuttlePassWithPassengerEntity.toDto(): ShuttlePassWithPassengerDto = /* ... */
```

Mappers are pure top-level extension functions with no I/O, and they are the only place types cross layers.

### Repository implementation

Target shape: observe Room and refresh from the network. Today `RoutesDao` only has `suspend` queries, so a `Flow` query like `observeAllRoutes()` would need adding first.

```kotlin
class RoutesRepositoryImpl @Inject constructor(
    private val api: ShuttleApi,
    private val database: ShuttleDatabase
) : RoutesRepository {

    override fun observeRoutes(): Flow<List<Route>> =
        database.RoutesDao().observeAllRoutes().map { list -> list.map { it.toDomain() } }

    override suspend fun refreshRoutes(): Resource<Unit> = withContext(Dispatchers.IO) {
        when (val result = safeApiCall { api.getRoutes() }) {
            is Resource.Success -> {
                database.RoutesDao().insertRoute(result.data.orEmpty().map { it.toEntity() })
                Resource.Success(Unit)
            }
            is Resource.Error -> Resource.Error(result.message ?: "Could not refresh routes.")
            is Resource.Loading -> Resource.Loading()
        }
    }
}
```

### Room entity and DAO

```kotlin
@Entity(tableName = "shuttle_pass", indices = [Index("routeId")])
data class ShuttlePassEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val driver: String = "",      // holds the account id
    val isDraft: Boolean = true,
    val isSynced: Boolean = false,
    /* ... */
)

@Dao
interface ShuttlePassDao {
    @Transaction
    @Query("SELECT * FROM shuttle_pass WHERE isDraft = 0 AND isSynced = 0 AND driver = :driverId")
    suspend fun getUnsyncedShuttlePasses(driverId: String): List<ShuttlePassWithPassengerEntity>
}
```

Every query that lists passes filters by the `driver` column (the logged-in account id). Use `@Transaction` for `@Relation` results and for multi-step writes.

### Offline-first shuttle pass lifecycle

1. **Draft:** a driver's in-progress pass is a `ShuttlePassEntity` with `isDraft = true`. There is at most one per driver (`getShuttlePassIdDraftByDriverId`). Scanned `PassengerQREntity` rows attach via `shuttlePassId`.
2. **Finalize:** `finalizeShuttleDraft` flips the pass and its passengers to `isDraft = false` in one transaction.
3. **Sync:** passes with `isDraft = 0 AND isSynced = 0` are mapped to `ShuttlePassWithPassengerDto` and POSTed to `shuttlepasses/`. On `Resource.Success`, mark them `isSynced = true`. On error, leave them unsynced so the next sync retries.
4. **Purge:** synced passes older than one month are deleted by `deleteShuttlePassOnDue`.

`isLateShuttle` drives the "late" filter and counter. Never delete or overwrite unsynced passes as a side effect of other work.

## Legacy code / known gaps

Don't copy these patterns into new code, and don't mass-refactor them unless asked:

- `ShuttlePassRepository`, `AuthRepository` and others expose entities, DTOs and `retrofit2.Response` in the domain interface.
- Most entities and domain models use `var` fields. New ones use `val`.
- Several `ShuttleApi` GETs return bare `List<T>` instead of `Response<T>`, so HTTP errors surface as exceptions.
- `ShuttleDatabase` DAO accessors are PascalCase functions (`ShuttlePassDao()`). Keep calling them as they are. Renaming them is a separate refactor.
- `AuthRepositoryImpl` logs the full user data with `Log.i`. Don't log PII or tokens.
- `app/proguard-rules.pro` keeps `com.example.shuttle.data.network.**`, which is the wrong package (the real one is `com.example.shuttleapp`), so DTOs are currently unprotected in release builds. See **android-r8-analyzer**.

## Checklist: adding data access

- [ ] The domain model is in `domain/model`, and the interface (domain types only) is in `domain/repository`.
- [ ] The DTO is in `data/network/request|response`, with `@SerializedName` matching the backend camelCase and nullable optional fields.
- [ ] The endpoint is on `AuthApi`/`ShuttleApi` (or a new API provided in `AppModule`) and returns `Response<T>`.
- [ ] The entity and DAO are in `data/local`, registered in `config/Database.kt`, the version is bumped, **and the data-loss risk has been flagged**.
- [ ] Mappers are in `data/mappers`.
- [ ] `XRepositoryImpl @Inject constructor` is written, with IO on `Dispatchers.IO` and errors mapped to `Resource`.
- [ ] The `@Binds` is added to `di/RepositoryModule.kt`.

## Related skills

- **android-error-handling**: `Resource`, `safeApiCall`, error messages
- **android-di-hilt**: binding the repository, providing APIs
- **android-testing**: fake repositories, in-memory Room DAO tests
- **android-r8-analyzer**: auditing the release keep rules that protect DTOs
