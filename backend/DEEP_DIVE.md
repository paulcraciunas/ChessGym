# ChessGym Backend — Technical Deep-Dive

A comprehensive walkthrough of how the backend works, from process startup to request
completion. Written as if explaining to a dumb Android engineer who never wrote a backend before in his life.

---

## Table of Contents

1. [Process Startup](#phase-1-process-startup)
2. [Module Initialization](#phase-2-module-initialization)
3. [Server Listening](#phase-3-server-listening)
4. [Request Lifecycle](#phase-4-request-lifecycle)
5. [Error Handling](#phase-5-error-handling)
6. [Deployment](#phase-6-deployment)
7. [Architecture Summary](#architecture-summary)
8. [Firebase vs Hosting Independence](#firebase-and-hosting-independence)

---

## Phase 1: Process Startup

**The JVM starts and calls `main()`.**

```kotlin
fun main(args: Array<String>): Unit = EngineMain.main(args)
```

`EngineMain` is Ktor's built-in bootstrap class for the Netty engine. When
`EngineMain.main(args)` is called, it does the following in order:

### 1. Loads `application.conf`

This file uses HOCON format (Human-Optimized Config Object Notation, a superset of JSON
created by Lightbend/Typesafe). Ktor reads:

```hocon
ktor {
    deployment {
        port = 8080
        port = ${?PORT}
    }
    application {
        modules = [ com.paulcraciunas.chessgym.ApplicationKt.module ]
    }
}
```

The `port = ${?PORT}` syntax is HOCON substitution: if the `PORT` environment variable
exists, it overrides the default `8080`. Cloud Run sets `PORT=8080` by convention, but
this makes the code portable.

The `modules` list tells Ktor which functions to call.
`com.paulcraciunas.chessgym.ApplicationKt` is the JVM class name that the Kotlin compiler
generates for top-level functions in `Application.kt` (Kotlin convention: `FileName.kt` →
`FileNameKt.class`). `.module` refers to the `Application.module()` extension function.

### 2. Creates a Netty server

Ktor calls `embeddedServer(Netty, ...)` internally. Netty is a Java NIO (Non-blocking I/O)
framework. Under the hood, Netty creates:

- A **boss group**: a small thread pool (typically 1 thread) that accepts incoming TCP
  connections.
- A **worker group**: a larger thread pool (typically `availableProcessors * 2` threads)
  that handles I/O operations (reading/writing bytes on accepted connections).
- A **channel pipeline**: for each accepted connection, Netty creates a pipeline of
  handlers that process the raw bytes. Ktor installs its own handlers into this pipeline:
  HTTP codec (parses raw bytes into HTTP request objects), HTTP/2 upgrade handler, and
  Ktor's `NettyApplicationCallHandler` (bridges Netty's world into Ktor's
  `ApplicationCall` abstraction).

### 3. Calls `Application.module()` via reflection

Ktor uses `Class.forName("com.paulcraciunas.chessgym.ApplicationKt")` and then invokes the
`module` method, passing the `Application` instance as the receiver.

---

## Phase 2: Module Initialization

```kotlin
fun module() {
    val firestore = FirebaseConfig.initialize(environment)

    val authService = FirebaseAuthService()
    val userRepository = FirestoreUserRepository(firestore)
    val userService = DefaultUserService(userRepository)
    val statisticsService = DefaultStatisticsService(userRepository)

    configureSerialization()
    configureCallLogging()
    configureStatusPages()
    configureAuthentication(authService)
    configureRouting(userService, statisticsService)
    configureOpenApi()
}
```

### Step 2a: Firebase initialization

```kotlin
object FirebaseConfig {
    fun initialize(environment: ApplicationEnvironment): Firestore {
        if (FirebaseApp.getApps().isEmpty()) {
            val projectId = environment.config
                .property("chessgym.firebase.projectId")
                .getString()

            val isEmulator = System.getenv("FIRESTORE_EMULATOR_HOST") != null

            val optionsBuilder = FirebaseOptions.builder()
                .setProjectId(projectId)

            if (!isEmulator) {
                optionsBuilder.setCredentials(GoogleCredentials.getApplicationDefault())
            }

            FirebaseApp.initializeApp(optionsBuilder.build())
        }

        return FirestoreClient.getFirestore()
    }
}
```

`FirebaseApp` is the singleton entry point for the Firebase Admin SDK. It manages a shared
gRPC channel pool and credentials. `FirebaseApp.initializeApp()` creates this singleton,
and all Firebase services (Auth, Firestore) share it.

`GoogleCredentials.getApplicationDefault()` uses Google's Application Default Credentials
(ADC) discovery chain: it checks (in order):
1. `GOOGLE_APPLICATION_CREDENTIALS` env var (path to a JSON key file)
2. gcloud CLI credentials (for local dev)
3. GCE metadata server (on Cloud Run, this is what works — Cloud Run automatically
   provides a service account identity via the instance metadata server, so no credential
   file is needed)

When running locally with emulators, we skip credentials entirely — the emulator doesn't
need authentication.

`FirestoreClient.getFirestore()` returns a `Firestore` instance, which is a gRPC client
that manages a channel to `firestore.googleapis.com:443` (or the emulator host). This
channel uses HTTP/2 multiplexing, so multiple Firestore operations share a single TCP
connection.

### Step 2b: Dependency wiring

The next four lines manually construct the object graph:

- `FirebaseAuthService()` — wraps `FirebaseAuth.getInstance()`, a static accessor to the
  Firebase singleton.
- `FirestoreUserRepository(firestore)` — receives the gRPC client, creates a
  `UserDtoMapper` internally.
- `DefaultUserService(userRepository)` — receives the repository, creates a
  `UserMergeStrategy` internally.
- `DefaultStatisticsService(userRepository)` — shares the same repository instance.

All dependencies flow downward through constructor parameters. No service locator, no
reflection, no framework. This IS dependency injection — just without a DI framework.

### Step 2c: Plugin installation

Each `configure*()` call installs a Ktor plugin. Ktor's plugin system works via an
`install(Plugin) { config }` pattern, conceptually similar to Android's `addInterceptor`
on OkHttp. Each plugin registers interceptors at specific **phases** of the request
pipeline.

Ktor's pipeline has ordered phases (simplified):

```
Setup → Monitoring → Features → Call → Fallback
```

Here's what each plugin does:

#### `configureSerialization()` — ContentNegotiation plugin

Installs at the `Call` phase. For incoming requests with `Content-Type: application/json`,
it intercepts the body and deserializes it using `kotlinx.serialization`. For outgoing
responses, it intercepts `call.respond(...)` calls and serializes the response object to
JSON.

Key configuration:
- `encodeDefaults = true`: fields with default values (like `puzzleRush = 0`) are included
  in the JSON output, not omitted.
- `ignoreUnknownKeys = true`: extra fields sent by the client are silently ignored
  (forward-compatibility).
- `prettyPrint = true`: JSON output is formatted with indentation for readability.

#### `configureCallLogging()` — CallLogging plugin

Installs at the `Monitoring` phase (runs early). Wraps each request in a log statement
with method, path, status code, and duration. Uses SLF4J → Logback (configured in
`logback.xml`).

#### `configureStatusPages()` — StatusPages plugin

Installs an exception handler. If any code in the pipeline throws, StatusPages catches it:
- `IllegalArgumentException` → 400 Bad Request
- Any other `Throwable` → 500 Internal Server Error with logging

This is a safety net. Without it, an unhandled exception would produce a raw 500 with no
JSON body, which is unfriendly for API clients.

#### `configureAuthentication()` — Authentication plugin

```kotlin
fun configureAuthentication(authService: AuthService) {
    install(Authentication) {
        bearer(AUTH_FIREBASE) {
            authenticate { tokenCredential ->
                try {
                    val decoded = authService.verifyIdToken(tokenCredential.token)
                    FirebasePrincipal(uid = decoded.uid, email = decoded.email)
                } catch (e: Exception) {
                    authLogger.warn("Token verification failed: {}", e.message)
                    null
                }
            }
        }
    }
}
```

This registers a **named authentication provider** called `"firebase"`. The `bearer(...)`
configuration tells Ktor: "When a route requires `authenticate(AUTH_FIREBASE)`, extract
the `Authorization: Bearer <token>` header, and call this lambda to validate it."

Inside the lambda, `authService.verifyIdToken(token)` calls Firebase Admin SDK, which:
1. Fetches Google's public signing keys from
   `https://www.googleapis.com/robot/v1/metadata/x509/securetoken@system.gserviceaccount.com`
   (cached for ~24h)
2. Decodes the JWT token (base64-encoded header.payload.signature)
3. Verifies the RSA-256 signature using the fetched public keys
4. Checks `exp` (expiration), `iss` (issuer), `aud` (audience = your project ID)
5. Returns a `FirebaseToken` containing the decoded claims (`uid`, `email`, etc.)

If validation succeeds, we return a `FirebasePrincipal`. Ktor attaches this to the
`ApplicationCall` as the authenticated principal. If it fails (expired token, forged
signature, etc.), we return `null`, and Ktor automatically responds with 401 Unauthorized.

#### `configureRouting()` — Routing plugin

```kotlin
routing {
    healthRoutes()
    authRoutes(userService)
    userRoutes(userService)
    statisticsRoutes(statisticsService)
}
```

`routing { }` installs Ktor's Routing plugin and builds a **route tree**. Each
`route(path)`, `get(path)`, `post(path)`, etc. adds a node to this tree:

```
/ (root)
├── /health                           → GET handler
├── /swagger                          → GET handler (Swagger UI)
├── /api/v1/auth
│   └── [authenticate(firebase)]
│       └── /signin                   → POST handler
├── /api/v1/users
│   └── [authenticate(firebase)]
│       └── /{userId}
│           └── [UserAuthorizationPlugin]
│               ├── GET handler
│               ├── PUT handler
│               └── DELETE handler
└── /api/v1/statistics
    └── /achievements                 → GET handler
```

When a request arrives, Ktor walks this tree to find a matching route. The
`[authenticate(...)]` and `[UserAuthorizationPlugin]` nodes are not path segments — they're
pipeline modifiers that add interceptors to the matched route's processing pipeline.

---

## Phase 3: Server Listening

After `module()` returns, Ktor binds Netty to `0.0.0.0:8080`. The boss thread starts
accepting TCP connections. The server logs:

```
Responding at http://0.0.0.0:8080
```

The process is now ready. On Cloud Run, the readiness probe hits `GET /health` to confirm.

---

## Phase 4: Request Lifecycle

**Example: `PUT /api/v1/users/user-123`**

An Android client sends:

```http
PUT /api/v1/users/user-123 HTTP/1.1
Authorization: Bearer eyJhbGciOiJSUzI1NiIs...
Content-Type: application/json

{"profile": {"firstName": "Paul", "lastModified": 1717000000}, "ratings": {"current": 1200}}
```

### 4a. Netty accepts the connection

The boss thread accepts the TCP connection and hands it to a worker thread. The worker
thread reads raw bytes from the socket, and Netty's HTTP codec decodes them into an HTTP
request object (method, path, headers, body stream).

### 4b. Ktor creates an `ApplicationCall`

Ktor's Netty bridge wraps the request into a `NettyApplicationCall`, which implements
`ApplicationCall`. This object represents the entire request-response lifecycle:
- `request`: the HTTP request (method, path, headers, body)
- `response`: the HTTP response (initially empty)
- `attributes`: a mutable map for storing per-request data (like the authenticated
  principal)

### 4c. Ktor launches a coroutine

This is a critical detail. Netty's worker threads are precious — they handle all I/O for
all connections. Ktor immediately dispatches the request processing to a coroutine on
Ktor's own dispatcher (which by default uses a coroutine dispatcher backed by Netty's
event group). This means request handlers are `suspend` functions and can call `delay()`,
`withContext()`, etc. without blocking Netty's I/O threads.

### 4d. The pipeline executes in phase order

**Monitoring phase — CallLogging:**
Logs: `INFO: PUT /api/v1/users/user-123` (start of request)

**Features phase — ContentNegotiation:**
Notes that `Content-Type: application/json` is present. Doesn't do anything yet — it waits
until someone calls `call.receive<T>()` to actually deserialize.

**Routing phase — Route matching:**
Ktor walks the route tree:
`/api` → `/v1` → `/users` → `/{userId}` matches with `userId = "user-123"` → `PUT`
method matches. The matched path parameters are stored in `call.parameters`.

**Authentication phase — Bearer token extraction:**
Because this route is inside `authenticate(AUTH_FIREBASE)`, Ktor's auth plugin runs:
1. Reads the `Authorization` header → `"Bearer eyJhbGciOiJSUzI1NiIs..."`
2. Strips the `"Bearer "` prefix → `"eyJhbGciOiJSUzI1NiIs..."`
3. Calls our `authenticate { tokenCredential -> ... }` lambda
4. Our lambda calls `authService.verifyIdToken(token)` → Firebase Admin SDK validates
   the JWT → returns `FirebaseToken(uid="user-123", email="paul@gmail.com")`
5. We return `FirebasePrincipal(uid="user-123", email="paul@gmail.com")`
6. Ktor stores this principal on the `ApplicationCall`

If the token were invalid, step 4 would throw, our `catch` block would log a warning and
return `null`, and Ktor would respond with `401 Unauthorized` — the route handler would
never execute.

**Authorization phase — UserAuthorizationPlugin:**

```kotlin
private val userAuthorizationPlugin = createRouteScopedPlugin("UserAuthorizationPlugin") {
    on(AuthenticationChecked) { call ->
        val userId = call.parameters["userId"] ?: run {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing userId", ErrorCode.BAD_REQUEST))
            return@on
        }
        if (!call.isAuthorized(userId)) {
            return@on
        }
    }
}
```

The `on(AuthenticationChecked)` hook fires *after* authentication completes. It:
1. Extracts `userId` from path parameters → `"user-123"`
2. Calls `call.isAuthorized("user-123")`, which retrieves the `FirebasePrincipal` from the
   call and compares `principal.uid` with `userId`
3. Since `"user-123" == "user-123"`, authorization passes

If this were a request from `user-a` trying to access `user-b`'s data, `isAuthorized`
would call `call.respond(Forbidden)` and `return@on`, and the route handler below would
never run.

### 4e. The route handler executes

```kotlin
put {
    val incoming = call.receive<UserDto>()
    val merged = userService.updateUser(userId, incoming)
    call.respond(HttpStatusCode.OK, merged)
}
```

**`call.receive<UserDto>()`:**
Triggers ContentNegotiation. The plugin reads the request body bytes, feeds them to
`kotlinx.serialization`'s `Json.decodeFromString<UserDto>(bodyText)`, and returns a
`UserDto` instance. Because `ignoreUnknownKeys = true`, any extra fields the client sends
are silently ignored (forward-compatibility). Because all DTO fields have defaults, the
client can send a partial object and the rest defaults to zero values.

**`userService.updateUser(userId, incoming)`:**

```kotlin
override suspend fun updateUser(userId: String, incoming: UserDto): UserDto {
    val existing = userRepository.findById(userId) ?: incoming
    val merged = mergeStrategy.merge(existing, incoming)
    userRepository.save(userId, merged)
    return merged
}
```

First, `userRepository.findById("user-123")`:

```kotlin
override suspend fun findById(userId: String): UserDto? = withContext(Dispatchers.IO) {
    val document = firestore.collection(COLLECTION_USERS)
        .document(userId)
        .get()
        .get()

    if (!document.exists()) return@withContext null
    mapper.fromMap(document.data ?: return@withContext null)
}
```

`withContext(Dispatchers.IO)` is critical. The Firestore Java client is blocking (it uses
`ApiFuture.get()`, which blocks the calling thread until the gRPC response arrives). We
can't block a coroutine on the main dispatcher, so `Dispatchers.IO` shifts execution to a
thread pool designed for blocking I/O (default: up to 64 threads). This is analogous to
`withContext(Dispatchers.IO)` on Android when doing Room or file operations.

`firestore.collection("users").document("user-123").get()` returns an
`ApiFuture<DocumentSnapshot>`. The second `.get()` (Java's `Future.get()`) blocks the IO
thread until the gRPC response arrives from Firestore.

If the document exists, `mapper.fromMap(document.data)` converts the Firestore
`Map<String, Any>` into a `UserDto`. Firestore returns numbers as `Long` or `Double`
depending on how they were stored, which is why the mapper uses
`(value as? Number)?.toInt()` rather than direct casting.

Next, `mergeStrategy.merge(existing, incoming)` applies our conflict resolution:
- **Numeric fields** (ratings, high-scores, statistics, achievement progress):
  `maxOf(existing, incoming)` — the higher value always wins, because these are
  monotonically increasing in a chess app.
- **Profile fields**: last-write-wins based on `lastModified` timestamp — whichever device
  edited the profile more recently wins.
- **Achievement progress map**: union of keys, `maxOf` per key.

Then `userRepository.save("user-123", merged)` writes back to Firestore.
`firestore.collection("users").document("user-123").set(map)` is a full document overwrite
via gRPC.

**`call.respond(HttpStatusCode.OK, merged)`:**
ContentNegotiation intercepts this, serializes `merged` to JSON via
`kotlinx.serialization`, sets the `Content-Type: application/json` header, and writes the
response bytes to the Netty channel.

### 4f. Response sent, logging finalized

CallLogging logs: `INFO: PUT /api/v1/users/user-123 - 200 OK (45ms)`. Netty writes the
HTTP response bytes to the TCP socket. The coroutine completes.

---

## Phase 5: Error Handling

If *anything* throws during the pipeline:

```kotlin
fun configureStatusPages() {
    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(message = cause.message ?: "Bad request", code = ErrorCode.BAD_REQUEST),
            )
        }
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(message = "Internal server error", code = ErrorCode.INTERNAL_ERROR),
            )
        }
    }
}
```

StatusPages catches the exception, matches it against the registered handlers (most
specific first — `IllegalArgumentException` before `Throwable`), and generates a
structured JSON error response. The client always gets a predictable `ErrorResponse` shape,
never a raw stack trace.

---

## Phase 6: Deployment

### Docker Multi-Stage Build

```dockerfile
FROM gradle:8.11.1-jdk17 AS build
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle ./gradle
COPY src ./src
RUN gradle buildFatJar --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/chessgym-backend.jar ./app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Stage 1 uses a full JDK image (~800MB) to compile and package the fat JAR (all
dependencies bundled into one JAR). Stage 2 uses a minimal JRE Alpine image (~180MB) that
only needs the runtime. The final image only contains the JRE and the JAR — no Gradle, no
source code, no build tools.

### Cloud Run Lifecycle

When Cloud Run receives a request and no container is running, it:
1. Pulls the Docker image from Artifact Registry
2. Starts the container with `java -jar app.jar`
3. Sets the `PORT` environment variable
4. Waits for the container to respond to the health probe at `/health`
5. Routes the incoming request to the container
6. After a period of inactivity (~15 minutes default), scales the container to zero (stops
   it to save costs)

This scale-to-zero behavior is why Cloud Run is cheap/free for low-traffic apps — you only
pay when requests are actually being handled.

---

## Architecture Summary

```
HTTP Request
    │
    ▼
┌─────────────────────────────────────────────────┐
│  Netty (NIO event loop)                         │
│  ├── Boss thread: accepts TCP connections        │
│  └── Worker threads: HTTP codec, read/write      │
└────────────────────┬────────────────────────────┘
                     │ dispatches to coroutine
                     ▼
┌─────────────────────────────────────────────────┐
│  Ktor Pipeline (coroutine-based)                │
│  ├── CallLogging (Monitoring phase)              │
│  ├── ContentNegotiation (JSON serialization)     │
│  ├── StatusPages (exception → error response)    │
│  ├── Authentication (Bearer → FirebasePrincipal) │
│  └── Routing (tree match → handler)              │
│       ├── /health                                │
│       ├── /api/v1/auth/signin                    │
│       ├── /api/v1/users/{userId}                 │
│       │   └── UserAuthorizationPlugin            │
│       └── /api/v1/statistics/achievements        │
└────────────────────┬────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────┐
│  Service Layer                                   │
│  ├── DefaultUserService (merge strategy)         │
│  └── DefaultStatisticsService (aggregation)      │
└────────────────────┬────────────────────────────┘
                     │ withContext(Dispatchers.IO)
                     ▼
┌─────────────────────────────────────────────────┐
│  Repository Layer                                │
│  ├── FirestoreUserRepository                     │
│  │   └── UserDtoMapper (Map<String,Any> ↔ DTO)  │
│  └── Firestore gRPC client                       │
│       └── firestore.googleapis.com:443           │
└─────────────────────────────────────────────────┘
```

The layers are designed so that each one only knows about the one directly below it via an
interface. Routes know about `UserService` (interface), not `DefaultUserService`. The
service knows about `UserRepository` (interface), not `FirestoreUserRepository`. 

---

## Firebase and Hosting Independence

There are **three completely separate concerns** in this architecture:

### 1. Where the code runs (the compute layer)

Cloud Run in our case — a Docker container running a JVM process. This is just a computer
in the cloud. It could be Cloud Run, an AWS EC2 instance, an Oracle Cloud VM, a Raspberry
Pi, or your laptop. Ktor doesn't know or care. It's a JVM application that opens a TCP
socket on port 8080 and listens for HTTP requests.

### 2. Where the data lives (the database)

Cloud Firestore is a Google-managed database accessible over the internet via gRPC. The
`com.google.cloud.firestore.Firestore` class is a *client library* — essentially a gRPC
client that talks to Google's servers. Think of it like Retrofit on Android: Retrofit
doesn't care where your app runs; it just makes HTTP calls to a server. Similarly, the
Firestore client doesn't care where your backend runs; it just makes gRPC calls to
`firestore.googleapis.com`.

### 3. Where the authentication lives (the identity layer)

Firebase Auth is another Google-managed service.
`FirebaseAuth.getInstance().verifyIdToken(token)` makes an HTTP call to Google's servers
to validate the token. It's just a client library calling a remote API.

### Could we deploy this on AWS or Oracle?

**Yes, with zero code changes.** Deploy the same Docker image on AWS ECS/Fargate or Oracle
Cloud Container Instances, set the `FIREBASE_PROJECT_ID` environment variable, provide
Google credentials (a service account JSON file), and it would work identically. The
Firestore and Firebase Auth clients would talk to Google's servers over the internet from
AWS/Oracle.

### What if we also wanted to replace the data and auth layers?

| Current (Google)     | AWS Equivalent     | Oracle Equivalent  |
|----------------------|--------------------|--------------------|
| Cloud Firestore      | DynamoDB           | Oracle NoSQL Cloud |
| Firebase Auth        | AWS Cognito        | OCI Identity       |
| `firebase-admin` SDK | AWS SDK for Kotlin | OCI SDK            |

That would require rewriting `FirestoreUserRepository` and `FirebaseAuthService`, but the
interfaces (`UserRepository`, `AuthService`) wouldn't change. The service layer, routes,
merge strategy, and mapper would all remain untouched.