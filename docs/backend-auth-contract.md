# Backend auth contract (ASP.NET Core API)

The Android app is **offline-first**. Drivers create, finalize and edit shuttle passes with no connection for hours or days. Only **login** and **upload** go to the server. The app is already built for the contract below (Phase 3 of `docs/layer-refactor-plan.md`). Until the backend sends a token, the app keeps working unauthenticated.

## 1. Login returns a token

`POST /api/users/login/`

Request (unchanged):
```json
{ "username": "jdelacruz", "password": "..." }
```

Response on success: today's fields, plus `token` and `expiresAt`:
```json
{
  "message": "login successful",
  "userData": { "id": "…", "employeeNumber": "…", "firstName": "…", "lastName": "…", "middleName": "…", "shuttleProvider": { … } },
  "token": "<JWT>",
  "expiresAt": "2025-02-01T00:00:00Z"
}
```
- `expiresAt`: ISO-8601 in UTC (`Z`) or with an offset. Fractional seconds are allowed. The app reads it as a hint to show "Sign in again to upload" before the first failed upload.
- Keep `message == "login successful"` for now; the app still checks it.
- Failed login: keep the current behaviour (a non-success message or a non-2xx status). Don't return a token.

## 2. Protect the data endpoints

Add `[Authorize]` (JWT Bearer) to:
- `POST /api/shuttlepasses/` (upload)
- `GET /api/routes/`, `GET /api/shuttles/providerid/{id}/`, `GET /api/shuttleproviders/`

Leave `users/login` and `users/register` anonymous. The app never sends the token to them.

The app sends `Authorization: Bearer <token>` on every other request.

## 3. Expired or invalid token → 401

Return **401** for a missing, invalid or expired token, without redirecting to a login page. On 401 the app:
- keeps the driver signed in and keeps every pass on the device (unsynced passes stay unsynced)
- shows "Sign in again to upload"; the driver enters their password and the app calls `users/login` again
- refuses a re-login as a **different** account (the `userData.id` must match the signed-in driver)

## 4. Token lifetime

Drivers can be offline for days, and an expired token only blocks uploads, so **use a long-lived token (about 30 days)**. v1 has no refresh token: the app re-authenticates with the password, and keeps the username on the device (encrypted). A refresh-token flow can be added later without changing the endpoints above.

## 5. Upload idempotency (recommended)

Pass ids are client-generated UUIDs. If the connection drops after the server saved an upload but before the app got the 200, the app uploads the same passes again. Treat `shuttlepasses/` as **upsert by `id`**, so a retry doesn't create duplicates.
