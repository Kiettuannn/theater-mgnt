ru# Service Layer Architecture

## 📁 Structure

```
frontend/client/
├── configurations/
│   ├── configuration.ts     # API endpoints và OAuth config
│   └── httpClient.ts        # Axios instance với interceptors
├── services/
│   ├── authService.ts       # Authentication services
│   ├── customerService.ts   # Customer profile services
│   ├── localStorageService.ts # Local storage utilities
│   └── index.ts            # Export all services
├── components/
│   ├── auth-modal.tsx      # Login/Register modal (uses authService)
│   ├── google-signin-button.tsx # Google OAuth button
│   └── header.tsx          # Main header component
└── app/
    └── authenticate/
        └── page.tsx        # Google OAuth callback handler
```

## 🔧 Configuration

### `configurations/configuration.ts`

- **CONFIG**: Base API URL
- **API**: All API endpoints for customer
- **OAuthConfig**: Google OAuth configuration

### `configurations/httpClient.ts`

- Axios instance with base URL
- Request/Response interceptors
- Global error handling

## 📦 Services

### `authService.ts`

Authentication related services:

- `login(data)` - Email/password login
- `register(data)` - Customer registration
- `authenticateWithGoogle(code)` - Google OAuth authentication
- `getGoogleAuthUrl()` - Generate Google OAuth URL

### `customerService.ts`

Customer profile services:

- `getMyInfo()` - Get current customer info
- `updateMyInfo(customerId, data)` - Update customer profile

### `localStorageService.ts`

Local storage utilities:

- `setToken()`, `getToken()`, `removeToken()` - Token management
- `setUserInfo()`, `getUserInfo()`, `removeUserInfo()` - User info management
- `clearAuthData()` - Clear all auth data

## 🎯 Usage Examples

### Login with Email/Password

```tsx
import { login } from "@/services/authService";

const handleLogin = async (email: string, password: string) => {
  try {
    const response = await login({ email, password });
    console.log("Login successful:", response);
    // Token is automatically saved to localStorage
  } catch (error) {
    console.error("Login failed:", error);
  }
};
```

### Register New Customer

```tsx
import { register } from "@/services/authService";

const handleRegister = async (
  fullName: string,
  email: string,
  password: string
) => {
  try {
    const response = await register({ fullName, email, password });
    console.log("Registration successful:", response);
  } catch (error) {
    console.error("Registration failed:", error);
  }
};
```

### Google OAuth Flow

```tsx
// 1. Redirect to Google (in GoogleSignInButton component)
import { getGoogleAuthUrl } from "@/services/authService";

const handleGoogleSignIn = () => {
  const googleAuthUrl = getGoogleAuthUrl();
  window.location.href = googleAuthUrl;
};

// 2. Handle callback (in /authenticate page)
import { authenticateWithGoogle } from "@/services/authService";

const code = searchParams.get("code");
const response = await authenticateWithGoogle(code);
// Token is automatically saved to localStorage
```

### Get Customer Info

```tsx
import { getMyInfo } from "@/services/customerService";

const fetchUserInfo = async () => {
  try {
    const userInfo = await getMyInfo();
    console.log("User info:", userInfo);
  } catch (error) {
    console.error("Failed to get user info:", error);
  }
};
```

## 🔐 Authentication Flow

1. **User clicks Login/Register** → Opens `AuthModal`
2. **Submit form** → Calls `login()` or `register()` from `authService`
3. **Service makes API call** → Uses `httpClient` with base URL
4. **Success** → Token saved to localStorage, page reloads
5. **Error** → Error displayed in modal

## 🌐 Google OAuth Flow

1. **User clicks "Continue with Google"** → `GoogleSignInButton`
2. **Generate OAuth URL** → `getGoogleAuthUrl()`
3. **Redirect to Google** → User authorizes
4. **Google redirects back** → `/authenticate?code=...`
5. **Exchange code for token** → `authenticateWithGoogle(code)`
6. **Success** → Token saved, redirect to home

## 🛠️ Best Practices

1. **Separation of Concerns**:

   - Components handle UI
   - Services handle API calls
   - Configuration is centralized

2. **Error Handling**:

   - All service methods use try-catch
   - Errors are logged and thrown for component handling

3. **Token Management**:

   - Tokens automatically saved after successful auth
   - Check for `window` before accessing localStorage (SSR)

4. **Type Safety**:
   - All services have TypeScript interfaces
   - Request/Response types are defined

## 🔄 Comparison with Admin

| Feature        | Admin            | Customer               |
| -------------- | ---------------- | ---------------------- |
| Base Path      | `/staffs`        | `/customers`           |
| Auth Endpoint  | `/auth/token`    | `/auth/customer/token` |
| OAuth Redirect | `localhost:5173` | `localhost:3000`       |
| Storage Key    | `staff_token`    | `customer_token`       |
| User Type      | Staff/Admin      | Customer               |

## 📝 Notes

- Customer endpoints use `/customers` prefix
- Admin endpoints use `/staffs` prefix
- Both share the same OAuth configuration but different redirect URIs
- Local storage keys are different to avoid conflicts
