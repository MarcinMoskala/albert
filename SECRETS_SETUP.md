# Secrets Setup Guide

This guide explains how to securely store sensitive configuration values for local development.

## Universal Properties System

All properties in `local.properties` (except `sdk.dir`) are automatically converted to environment
variables:

- Property name format: `lowercase.dot.separated`
- Environment variable format: `UPPERCASE_UNDERSCORE_SEPARATED`

**Example:**

```properties
firebase.project.id=albert-8091e
# Becomes: FIREBASE_PROJECT_ID=albert-8091e

my.custom.api.key=secret123
# Becomes: MY_CUSTOM_API_KEY=secret123
```

## Firebase Service Account Setup

### 1. Download Service Account Key

1. Go
   to [Firebase Console](https://console.firebase.google.com/project/albert-8091e/settings/serviceaccounts/adminsdk)
2. Click **"Generate New Private Key"**
3. Save the downloaded JSON file

### 2. Add to local.properties

Edit `local.properties` and add:

```properties
# Firebase Configuration
firebase.project.id=albert-8091e
firebase.service.account.json={"type":"service_account","project_id":"albert-8091e",...}
```

**Note:** Replace `...` with the actual JSON content from the downloaded file. Keep it on one line
and escape special characters if needed.

### 3. Run the Server

```bash
./gradlew :server:run
```

The server will automatically:

1. Load properties from `local.properties`
2. Convert them to environment variables (FIREBASE_PROJECT_ID, FIREBASE_SERVICE_ACCOUNT_JSON)
3. Initialize Firebase Admin SDK with these credentials

## Current Configuration

Your `local.properties` already contains:

- ✅ `firebase.project.id` → `FIREBASE_PROJECT_ID`
- ✅ `firebase.service.account.json` → `FIREBASE_SERVICE_ACCOUNT_JSON`

## Alternative: Environment Variables

Instead of `local.properties`, you can set environment variables directly:

**Linux/macOS:**

```bash
export FIREBASE_PROJECT_ID=albert-8091e
export FIREBASE_SERVICE_ACCOUNT_JSON='{"type":"service_account",...}'
./gradlew :server:run
```

**Windows PowerShell:**

```powershell
$env:FIREBASE_PROJECT_ID="albert-8091e"
$env:FIREBASE_SERVICE_ACCOUNT_JSON='{"type":"service_account",...}'
./gradlew :server:run
```

## Security Notes

- ✅ **DO NOT** commit `local.properties` to git (already in `.gitignore`)
- ✅ **DO NOT** commit service account JSON files to git
- ✅ **DO NOT** hardcode credentials in source code
- ✅ Use environment variables for production deployments
- ✅ Rotate service account keys periodically

## What Works Without Service Account

The current implementation works for **token verification** without a service account because
Firebase Admin SDK can verify tokens using public keys.

**Works:**

- ✅ Verifying Firebase ID tokens from Android/iOS apps
- ✅ Extracting user information from tokens
- ✅ User authentication via Google Sign-In

**Requires Service Account:**

- ⚠️ Creating custom tokens
- ⚠️ Accessing Firebase Database/Firestore from server
- ⚠️ Managing user accounts server-side

For the Albert app, **token verification is sufficient** for the current login implementation.

## Production Deployment

For production deployment (Docker, cloud, etc.), set environment variables:

```bash
docker run -e FIREBASE_PROJECT_ID=albert-8091e \
           -e FIREBASE_SERVICE_ACCOUNT_JSON='{"type":...}' \
           albert-server:latest
```

Or use secret management services provided by your cloud platform (AWS Secrets Manager, Google
Secret Manager, Azure Key Vault, etc.).

## Adding Custom Properties

You can add any custom property to `local.properties`:

```properties
# Database connection
database.url=jdbc:postgresql://localhost:5432/albert
database.username=admin
database.password=secret123

# API Keys
api.openai.key=sk-...
api.stripe.key=sk_test_...
```

These will be available as:

- `DATABASE_URL`
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`
- `API_OPENAI_KEY`
- `API_STRIPE_KEY`

Access them in your code with: `System.getenv("DATABASE_URL")`

## Troubleshooting

### Server fails to start

Check if Firebase credentials are properly set:

```bash
./gradlew :server:run
# Look for: "✅ Firebase Admin SDK initialized for project: albert-8091e"
```

### Credentials not loaded

Verify `local.properties` exists and contains the properties:

```bash
cat local.properties | grep firebase
```

### JSON parsing errors

Ensure the JSON is on a single line with proper escaping:

- Replace newlines in private key with `\\n`
- Escape special characters if needed
