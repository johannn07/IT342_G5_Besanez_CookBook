## Overview

CookBook is a digital recipe organizer that lets home cooks store, organize, and share their recipes from any device. Users can create and edit recipes with ingredients and step-by-step instructions, organize them into collections, and share recipes with anyone via a public link — no account required to view.

**Live Web App:** [https://cook-book-mu-flame.vercel.app](https://cook-book-mu-flame.vercel.app)  
**Backend API:** [https://cookbook-besanez.onrender.com](https://cookbook-besanez.onrender.com)

---

## Features

### Core
- **Recipe Management** — Create, edit, and delete recipes with rich details: ingredients (with quantities and units), step-by-step instructions, prep/cook/total times, images, and notes
- **Collections** — Organize recipes into named collections with optional descriptions; collections display image slideshows from their recipes
- **Sharing** — Generate a shareable link for any recipe; anyone with the link can view and save a copy without an account
- **Privacy Controls** — Each recipe can be set to public or private (private by default)

### Auth
- Email/password registration and login
- Google OAuth2 sign-in (web and Android)
- JWT-based authentication with auto-expiry handling
- Email verification via 6-digit code
- Password change (via current password or email code) and password reset

### User
- Profile management: name, birthdate, cooking skill level
- Profile photo upload (stored on Cloudinary)
- Account deletion

### Admin Panel
- Dashboard with user/recipe/collection stats and growth charts
- User management: search, promote/demote to admin, delete
- Recipe and collection management with search and pagination

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3.5, Spring Security, Spring Data JPA |
| Database | PostgreSQL |
| ORM | Hibernate |
| Auth | JWT (jjwt), Spring OAuth2 (Google) |
| Image Storage | Cloudinary |
| Email | Spring Mail (SMTP) |
| Web Frontend | React 18, React Router, Axios, CSS Modules |
| Android | Kotlin, Retrofit2, Glide, DataStore, Coroutines, ViewModel/LiveData |
| Deployment | Backend: Render (Docker), Frontend: Vercel |

---

## Project Structure

```
IT342_G5_BESANEZ/
├── backend/
│   └── besanez/               # Spring Boot application
│       ├── src/main/java/edu/cit/besanez/cookbook/
│       │   ├── auth/           # Authentication & OAuth2
│       │   ├── user/           # User management
│       │   ├── recipe/         # Recipe CRUD
│       │   ├── ingredient/     # Ingredient sub-resource
│       │   ├── instruction/    # Instruction sub-resource
│       │   ├── collection/     # Collection management
│       │   ├── share/          # Recipe sharing via token
│       │   ├── admin/          # Admin dashboard & management
│       │   ├── image/          # Cloudinary image upload
│       │   └── shared/         # Config, utils, exceptions
│       ├── Dockerfile
│       └── pom.xml
│
├── web/
│   └── src/
│       ├── features/           # Feature-based modules
│       │   ├── auth/           # Login, register, OAuth2 callback
│       │   ├── recipe/         # Recipe list, detail, create/edit
│       │   ├── collection/     # Collections list and detail
│       │   ├── dashboard/      # User dashboard
│       │   ├── profile/        # User profile
│       │   ├── settings/       # Account settings
│       │   ├── admin/          # Admin panel
│       │   └── legal/          # Landing, about, privacy, terms
│       └── shared/             # API clients, layout, utilities
│
├── mobile/
│   └── app/src/main/java/com/it342/besanez/
│       ├── ui/
│       │   ├── auth/           # Login, register, forgot password
│       │   ├── recipe/         # Recipe list, detail, create/edit
│       │   ├── collection/     # Collections list and detail
│       │   ├── profile/        # Profile fragment
│       │   └── settings/       # Settings activity
│       ├── network/            # Retrofit API service
│       ├── repository/         # Data repositories
│       ├── model/              # Data models
│       └── data/               # TokenManager (DataStore)
│
└── README.md
```

---

## Getting Started

### Prerequisites

- **Backend:** Java 21, Maven 3.9+, PostgreSQL 14+
- **Web:** Node.js 18+, npm
- **Android:** Android Studio Hedgehog or newer, Android SDK 34, Kotlin 1.9+

---

### Backend Setup

1. Clone the repository and navigate to the backend:
   ```bash
   cd backend/besanez
   ```

2. Create a `.env` file in `backend/besanez/` (see [Environment Variables](#environment-variables)).

3. Build and run:
   ```bash
   ./mvnw spring-boot:run
   ```

   The API will be available at `http://localhost:8080`.

4. **Docker** (optional):
   ```bash
   docker build -t cookbook-backend .
   docker run -p 8080:8080 --env-file .env cookbook-backend
   ```

---

### Web Frontend Setup

1. Navigate to the web directory:
   ```bash
   cd web
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Create a `.env` file:
   ```env
   REACT_APP_API_URL=http://localhost:8080
   ```

4. Start the development server:
   ```bash
   npm start
   ```

   The app will be available at `http://localhost:3000`.

---

### Android Setup

1. Open the `mobile/` directory in Android Studio.

2. Update the base URL in `ApiClient.kt` to point to your backend:
   ```kotlin
   // For Android emulator:
   private const val BASE_URL = "http://10.0.2.2:8080/"

   // For a physical device (use your machine's local IP):
   private const val BASE_URL = "http://192.168.x.x:8080/"
   ```

3. Sync Gradle and run on an emulator or device (API 24+).

---

## Environment Variables

Create a `.env` file in `backend/besanez/` with the following:

```env
# Database
DB_HOST=your_db_host
DB_PORT=5432
DB_NAME=your_db_name
DB_USERNAME=your_db_user
DB_PASSWORD=your_db_password

# JWT
JWT_SECRET=your_jwt_secret_min_32_chars

# Google OAuth2
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

# Cloudinary
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret

# Mail (SMTP)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password
```

---

## API Reference

All authenticated endpoints require an `Authorization: Bearer <token>` header.

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | — | Register a new user |
| POST | `/api/auth/login` | — | Login and receive JWT |
| POST | `/api/auth/logout` | ✓ | Logout |
| POST | `/api/auth/change-password` | ✓ | Change password |
| POST | `/api/auth/forgot-password` | — | Reset password by email |
| POST | `/api/auth/send-verification-code` | — | Send email verification code |
| POST | `/api/auth/verify-code` | — | Verify email code |
| POST | `/api/auth/google/mobile` | — | Google login (mobile) |
| GET | `/api/user/me` | ✓ | Get current user |
| PUT | `/api/user/:id` | ✓ | Update user profile |
| POST | `/api/user/me/profile-image/upload` | ✓ | Upload profile photo |
| DELETE | `/api/user/:id` | ✓ | Delete account |
| GET | `/api/recipe` | ✓ | List user's recipes (paginated, searchable) |
| GET | `/api/recipe/public` | — | Browse public recipes |
| POST | `/api/recipe` | ✓ | Create recipe |
| GET | `/api/recipe/:id` | ✓ | Get recipe by ID |
| PUT | `/api/recipe/:id` | ✓ | Update recipe |
| DELETE | `/api/recipe/:id` | ✓ | Delete recipe |
| GET/POST/PUT/DELETE | `/api/recipe/:id/ingredient` | ✓ | Manage ingredients |
| GET/POST/PUT/DELETE | `/api/recipe/:id/instruction` | ✓ | Manage instructions |
| GET | `/api/collection` | ✓ | List user's collections |
| POST | `/api/collection` | ✓ | Create collection |
| GET | `/api/collection/:id` | ✓ | Get collection by ID |
| PUT | `/api/collection/:id` | ✓ | Update collection |
| DELETE | `/api/collection/:id` | ✓ | Delete collection |
| POST | `/api/collection/:id/recipe/:recipeId` | ✓ | Add recipe to collection |
| DELETE | `/api/collection/:id/recipe/:recipeId` | ✓ | Remove recipe from collection |
| POST | `/api/share/recipe/:recipeId` | ✓ | Generate share link |
| DELETE | `/api/share/recipe/:recipeId` | ✓ | Revoke share link |
| GET | `/api/share/:token` | — | View shared recipe |
| POST | `/api/share/:token/save` | ✓ | Save shared recipe to cookbook |
| POST | `/api/image/upload` | ✓ | Upload image to Cloudinary |
| GET | `/api/admin/stats` | Admin | Dashboard statistics |
| GET | `/api/admin/users` | Admin | List/search users |
| PATCH | `/api/admin/users/:id/toggle-role` | Admin | Promote/demote admin |
| DELETE | `/api/admin/users/:id` | Admin | Delete user |
| GET | `/api/admin/recipes` | Admin | List/search all recipes |
| DELETE | `/api/admin/recipes/:id` | Admin | Delete any recipe |
| GET | `/api/admin/collections` | Admin | List all collections |
| DELETE | `/api/admin/collections/:id` | Admin | Delete any collection |

---

## Color Palette

| Token | Hex | Usage |
|---|---|---|
| Terracotta | `#C97D4E` | Primary brand, buttons, accents |
| Terracotta Dark | `#A05E33` | Hover states |
| Cream | `#FDF8F2` | Page backgrounds |
| Warm White | `#FFF9F2` | Card backgrounds |
| Warm Brown | `#5C3D2E` | Headings |
| Text Dark | `#3A2A1E` | Body text |
| Text Mid | `#7A5C46` | Secondary text |
| Text Light | `#B09080` | Placeholder, muted text |
| Peach Light | `#FAE3CC` | Ghost button backgrounds, chips |
| Peach | `#F5C9A0` | Hover on peach elements |
| Border | `#EDD8C4` | Input borders, dividers |
