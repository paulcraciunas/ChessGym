# ChessGym Backend

Kotlin (Ktor) backend for the [ChessGym](https://github.com/paulcraciunas/ChessGym) Android application. Manages user accounts, syncs progress across devices, and provides achievement statistics.

## Architecture

```
Android App ──Bearer token──▶ Cloud Run (Ktor API) ──▶ Cloud Firestore
     │                              │
     └──Sign-in flow──▶ Firebase Auth ◀──Token validation──┘

Web Browser ──HTTPS──▶ Firebase Hosting (static pages)
```

| Component | Technology | Free Tier |
|-----------|-----------|-----------|
| API Server | Ktor 3 on Google Cloud Run | 2M requests/month |
| Database | Cloud Firestore | 50K reads, 20K writes/day |
| Authentication | Firebase Auth (Google + Email/Password) | Unlimited |
| Web Pages | Firebase Hosting | 10 GB storage, 10 GB/month transfer |
| CI/CD | GitHub Actions | Free for public repos |

## API Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `GET` | `/health` | No | Readiness probe |
| `GET` | `/swagger` | No | Interactive API docs |
| `POST` | `/api/v1/auth/signin` | Bearer | Sign in (create or retrieve user) |
| `GET` | `/api/v1/users/{userId}` | Bearer | Get user data |
| `PUT` | `/api/v1/users/{userId}` | Bearer | Update user (merge strategy) |
| `DELETE` | `/api/v1/users/{userId}` | Bearer | Delete user (GDPR) |
| `GET` | `/api/v1/statistics/achievements` | No | Achievement percentages |

## Prerequisites

- **JDK 17** or higher ([Eclipse Temurin](https://adoptium.net/) recommended)
- **Firebase CLI** — `npm install -g firebase-tools`
- **Google Cloud SDK** (`gcloud`) — [install guide](https://cloud.google.com/sdk/docs/install) (only for deployment)

## Phase 1: Local Development

No cloud account needed. Everything runs on your machine.

### 1. Start Firebase Emulators

The emulators simulate Firestore and Auth locally:

```bash
# Install Firebase CLI if you haven't
npm install -g firebase-tools

# From the project root (not /backend)
firebase init emulators
# Select: Authentication Emulator and Firestore Emulator
# Accept default ports (Auth: 9099, Firestore: 8081)

# Start emulators
firebase emulators:start --only auth,firestore
```

You should see:

```
✔  All emulators ready! It is now safe to connect your app.
│ Emulator  │ Host:Port      │
│ Auth      │ localhost:9099  │
│ Firestore │ localhost:8081  │
│ UI        │ localhost:4000  │
```

Leave this terminal running.

### 2. Build and Run the Backend

Open a new terminal:

```bash
cd backend

# Set environment variables for local development
export FIREBASE_PROJECT_ID=chessgym-local
export FIRESTORE_EMULATOR_HOST=localhost:8081
export FIREBASE_AUTH_EMULATOR_HOST=localhost:9099

# Build and run
./gradlew run
```

You should see:

```
... Responding at http://0.0.0.0:8080
```

### 3. Test the Endpoints

**Health check:**

```bash
curl http://localhost:8080/health
# {"status":"ok"}
```

**Swagger UI:**

Open http://localhost:8080/swagger in your browser for interactive API docs.

**Sign in (with emulator):**

First, create a test user in the Auth emulator at http://localhost:4000/auth, then use the emulator to obtain an ID token. Alternatively, see the test suite for programmatic examples.

### 4. Run Tests

```bash
cd backend
./gradlew test
```

All tests use in-memory fakes (no emulators required for tests).

## Phase 2: Cloud Deployment

### 1. Reuse Your Existing Firebase Project

You already have a Firebase project for Crashlytics. Let's reuse it:

```bash
# Log in to Firebase (if not already)
firebase login

# List your projects
firebase projects:list
# Find your ChessGym project ID (e.g., chessgym-12345)
```

### 2. Enable Required APIs

Go to the [Google Cloud Console](https://console.cloud.google.com) and select your project, then enable:

- **Cloud Run API** — [Enable here](https://console.cloud.google.com/apis/library/run.googleapis.com)
- **Cloud Firestore API** — [Enable here](https://console.cloud.google.com/apis/library/firestore.googleapis.com)
- **Artifact Registry API** — [Enable here](https://console.cloud.google.com/apis/library/artifactregistry.googleapis.com)

### 3. Create the Firestore Database

```bash
gcloud firestore databases create --location=us-central1
```

### 4. Enable Firebase Authentication

In the [Firebase Console](https://console.firebase.google.com):

1. Go to **Authentication** → **Sign-in method**
2. Enable **Google** provider
3. Enable **Email/Password** provider

### 5. Deploy to Cloud Run

```bash
cd backend

gcloud run deploy chessgym-backend \
  --source . \
  --region us-central1 \
  --allow-unauthenticated \
  --set-env-vars "FIREBASE_PROJECT_ID=YOUR_PROJECT_ID"
```

First deployment takes a few minutes (builds the Docker image). Subsequent deployments are faster.

You should see:

```
Service [chessgym-backend] revision [...] has been deployed
Service URL: https://chessgym-backend-XXXXX-uc.a.run.app
```

### 6. Verify

```bash
curl https://chessgym-backend-XXXXX-uc.a.run.app/health
# {"status":"ok"}
```

### 7. Deploy Web Pages

```bash
cd backend/web

# Initialize Firebase Hosting (first time only)
firebase init hosting
# Select your project
# Set public directory to: public
# Configure as single-page app: No

# Deploy
firebase deploy --only hosting
```

## Phase 3: Domain, Email, and Polish

### 1. Register a Domain

Purchase a domain from [Cloudflare Registrar](https://www.cloudflare.com/products/registrar/) (at-cost pricing):

- `chessgym.app` (~$14/year) — recommended
- `chessgym.dev` (~$12/year)

### 2. Set Up Cloudflare DNS

1. Create a free [Cloudflare](https://www.cloudflare.com/) account
2. Add your domain
3. Update nameservers at your registrar to Cloudflare's
4. Cloudflare will auto-import DNS records

### 3. Connect Custom Domain to Cloud Run

```bash
gcloud beta run domain-mappings create \
  --service chessgym-backend \
  --domain api.chessgym.app \
  --region us-central1
```

Follow the DNS verification instructions (add the CNAME record in Cloudflare).

### 4. Connect Custom Domain to Firebase Hosting

In the [Firebase Console](https://console.firebase.google.com):

1. Go to **Hosting** → **Add custom domain**
2. Enter `chessgym.app` (or `www.chessgym.app`)
3. Add the DNS records shown in Cloudflare

### 5. Set Up Email

In Cloudflare Dashboard:

1. Go to **Email** → **Email Routing**
2. Add route: `contact@chessgym.app` → your personal Gmail
3. Add route: `feedback@chessgym.app` → your personal Gmail
4. Add the required DNS records (MX, TXT) — Cloudflare does this automatically

To reply from `contact@chessgym.app` via Gmail:

1. Gmail → Settings → Accounts → **Send mail as** → Add another email
2. Enter `contact@chessgym.app`
3. Follow the verification steps

## Environment Variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `PORT` | No | `8080` | Server port (Cloud Run sets this) |
| `FIREBASE_PROJECT_ID` | Yes | `chessgym-local` | Firebase/GCP project ID |
| `FIRESTORE_EMULATOR_HOST` | No | — | Firestore emulator (local dev) |
| `FIREBASE_AUTH_EMULATOR_HOST` | No | — | Auth emulator (local dev) |

## GitHub Actions CI/CD

The workflow at `.github/workflows/backend-ci.yml` runs on every push/PR that touches `backend/`:

- **build-and-test** — builds the project and runs all tests
- **deploy** — deploys to Cloud Run (only on push to `master`)

### Required Secrets for Deployment

Set these in GitHub → Settings → Secrets:

| Secret | Description |
|--------|-------------|
| `GCP_WORKLOAD_IDENTITY_PROVIDER` | Workload Identity Federation provider |
| `GCP_SERVICE_ACCOUNT` | Service account email for deployment |
| `FIREBASE_PROJECT_ID` | Your Firebase project ID |

See [Workload Identity Federation setup](https://github.com/google-github-actions/auth#setting-up-workload-identity-federation) for details.

## Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── kotlin/com/paulcraciunas/chessgym/
│   │   │   ├── Application.kt          # Ktor entry point
│   │   │   ├── config/
│   │   │   │   └── FirebaseConfig.kt    # Firebase initialization
│   │   │   ├── models/
│   │   │   │   ├── UserDto.kt           # Data transfer objects
│   │   │   │   └── ApiResponse.kt       # Request/response types
│   │   │   ├── plugins/
│   │   │   │   ├── Authentication.kt    # Auth + authorization
│   │   │   │   ├── CallLogging.kt       # Request logging
│   │   │   │   ├── OpenApi.kt           # Swagger UI
│   │   │   │   ├── Routing.kt           # Route installation
│   │   │   │   ├── Serialization.kt     # JSON config
│   │   │   │   └── StatusPages.kt       # Error handling
│   │   │   ├── repositories/
│   │   │   │   ├── UserRepository.kt    # Interface
│   │   │   │   ├── FirestoreUserRepository.kt
│   │   │   │   └── UserDtoMapper.kt     # Firestore ↔ DTO mapping
│   │   │   ├── routes/
│   │   │   │   ├── AuthRoutes.kt
│   │   │   │   ├── HealthRoutes.kt
│   │   │   │   ├── StatisticsRoutes.kt
│   │   │   │   └── UserRoutes.kt
│   │   │   └── services/
│   │   │       ├── AuthService.kt       # Interface
│   │   │       ├── FirebaseAuthService.kt
│   │   │       ├── UserService.kt       # Interface
│   │   │       ├── DefaultUserService.kt
│   │   │       ├── UserMergeStrategy.kt # Merge strategy
│   │   │       ├── StatisticsService.kt # Interface
│   │   │       └── DefaultStatisticsService.kt
│   │   └── resources/
│   │       ├── application.conf
│   │       ├── logback.xml
│   │       └── openapi/documentation.yaml
│   └── test/
│       └── kotlin/com/paulcraciunas/chessgym/
│           ├── helpers/                 # Fakes and test utilities
│           ├── routes/                  # Route integration tests
│           └── services/                # Service unit tests
├── web/
│   ├── public/                          # Firebase Hosting files
│   │   ├── index.html                   # Landing page
│   │   └── donations.html               # Supporters page
│   └── firebase.json
├── Dockerfile
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## License

The backend component of ChessGym is proprietary software. See [LICENSE](LICENSE) for details.

The Android application is licensed separately under GPL-3.0. See the root [LICENSE](../LICENSE) file.
