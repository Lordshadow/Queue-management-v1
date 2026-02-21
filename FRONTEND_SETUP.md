# Next.js + TypeScript Queue Management Frontend - Complete Setup Guide

## Table of Contents
1. [Project Structure](#project-structure)
2. [API Endpoints Reference](#api-endpoints-reference)
3. [Request/Response Examples](#requestresponse-examples)
4. [Implementation Details](#implementation-details)
5. [Key Features](#key-features)

---

## Project Structure (Fully Modular - Next.js App Router)

```
queue-management-frontend/
├── app/
│   ├── layout.tsx                          # Root layout
│   ├── page.tsx                            # Home page
│   ├── globals.css                         # Global styles
│   ├── (auth)/
│   │   ├── layout.tsx                      # Auth layout
│   │   ├── login/
│   │   │   ├── page.tsx
│   │   │   └── page.module.css
│   │   ├── register/
│   │   │   ├── page.tsx
│   │   │   └── page.module.css
│   │   ├── forgot-password/
│   │   │   ├── page.tsx
│   │   │   └── page.module.css
│   │   └── reset-password/
│   │       ├── page.tsx
│   │       └── page.module.css
│   ├── student/
│   │   ├── layout.tsx                      # Protected student layout
│   │   ├── dashboard/
│   │   │   ├── page.tsx
│   │   │   └── page.module.css
│   │   ├── profile/
│   │   │   ├── page.tsx
│   │   │   └── page.module.css
│   │   ├── history/
│   │   │   ├── page.tsx
│   │   │   └── page.module.css
│   │   └── token/
│   │       ├── page.tsx
│   │       └── page.module.css
│   └── counter/
│       ├── layout.tsx                      # Protected counter layout
│       ├── dashboard/
│       │   ├── page.tsx
│       │   └── page.module.css
│       └── status/
│           ├── page.tsx
│           └── page.module.css
├── components/
│   ├── auth/
│   │   ├── LoginForm/
│   │   │   ├── LoginForm.tsx
│   │   │   ├── LoginForm.module.css
│   │   │   └── index.ts
│   │   ├── RegisterForm/
│   │   │   ├── RegisterForm.tsx
│   │   │   ├── RegisterForm.module.css
│   │   │   └── index.ts
│   │   └── index.ts
│   ├── student/
│   │   ├── TokenGeneration/
│   │   │   ├── TokenGeneration.tsx
│   │   │   ├── TokenGeneration.module.css
│   │   │   └── index.ts
│   │   ├── QueueStatus/
│   │   │   ├── QueueStatus.tsx
│   │   │   ├── QueueStatus.module.css
│   │   │   └── index.ts
│   │   ├── TokenHistory/
│   │   │   ├── TokenHistory.tsx
│   │   │   ├── TokenHistory.module.css
│   │   │   └── index.ts
│   │   └── index.ts
│   ├── counter/
│   │   ├── CallNextToken/
│   │   │   ├── CallNextToken.tsx
│   │   │   ├── CallNextToken.module.css
│   │   │   └── index.ts
│   │   ├── BreakManagement/
│   │   │   ├── BreakManagement.tsx
│   │   │   ├── BreakManagement.module.css
│   │   │   └── index.ts
│   │   └── index.ts
│   ├── common/
│   │   ├── Navbar/
│   │   │   ├── Navbar.tsx
│   │   │   ├── Navbar.module.css
│   │   │   └── index.ts
│   │   ├── Sidebar/
│   │   │   ├── Sidebar.tsx
│   │   │   ├── Sidebar.module.css
│   │   │   └── index.ts
│   │   ├── Button/
│   │   │   ├── Button.tsx
│   │   │   ├── Button.module.css
│   │   │   └── index.ts
│   │   ├── Card/
│   │   │   ├── Card.tsx
│   │   │   ├── Card.module.css
│   │   │   └── index.ts
│   │   ├── Modal/
│   │   │   ├── Modal.tsx
│   │   │   ├── Modal.module.css
│   │   │   └── index.ts
│   │   ├── LoadingSpinner/
│   │   │   ├── LoadingSpinner.tsx
│   │   │   ├── LoadingSpinner.module.css
│   │   │   └── index.ts
│   │   └── index.ts
│   └── providers/
│       ├── AuthProvider.tsx
│       ├── ClientProvider.tsx              # Client-side providers wrapper
│       └── index.ts
├── contexts/
│   ├── authContext.tsx
│   ├── queueContext.tsx
│   └── index.ts
├── hooks/
│   ├── useAuth.ts
│   ├── useQueue.ts
│   ├── useFetch.ts
│   └── index.ts
├── services/
│   ├── api/
│   │   ├── apiClient.ts
│   │   └── index.ts
│   ├── auth/
│   │   ├── authService.ts
│   │   └── index.ts
│   ├── student/
│   │   ├── studentService.ts
│   │   └── index.ts
│   ├── counter/
│   │   ├── counterService.ts
│   │   └── index.ts
│   └── index.ts
├── utils/
│   ├── constants.ts
│   ├── validation.ts
│   ├── errors.ts
│   ├── localStorage.ts
│   └── index.ts
├── styles/
│   ├── variables.css
│   ├── reset.css
│   └── globals.css
├── config/
│   ├── apiConfig.ts
│   ├── routes.ts
│   └── index.ts
├── types/
│   ├── auth.ts
│   ├── student.ts
│   ├── counter.ts
│   ├── queue.ts
│   └── index.ts
├── lib/
│   ├── middleware.ts                       # Next.js middleware for auth
│   └── index.ts
├── public/
│   └── favicon.ico
├── package.json
├── next.config.js
├── tsconfig.json
├── .env.local
├── .env.example
├── .gitignore
└── README.md
```

### Modular Component Structure (TypeScript)

Each component folder contains:
- **ComponentName.tsx** - React component (TypeScript)
- **ComponentName.module.css** - Scoped CSS styles (CSS Modules)
- **index.ts** - Barrel export for clean imports

#### Example Component Structure (LoginForm)
```
LoginForm/
├── LoginForm.tsx          # Component logic (TypeScript)
├── LoginForm.module.css   # Component styles (scoped)
└── index.ts               # Export file

// LoginForm.tsx
import { FC, FormEvent } from 'react';
import styles from './LoginForm.module.css';

interface LoginFormProps {
  onSubmit: (data: LoginData) => Promise<void>;
  isLoading?: boolean;
}

interface LoginData {
  identifier: string;
  password: string;
}

const LoginForm: FC<LoginFormProps> = ({ onSubmit, isLoading = false }) => {
  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    // Form logic
  };

  return <form className={styles.form} onSubmit={handleSubmit}>...</form>;
};

export default LoginForm;

// LoginForm.module.css
.form {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.formGroup {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

// index.ts
export { default } from './LoginForm';
export type { LoginFormProps };
```

#### Example Page Component (Next.js)
```
app/login/
├── page.tsx               # Page component
└── page.module.css        # Page styles

// app/login/page.tsx
'use client';
import { useRouter } from 'next/navigation';
import LoginForm from '@/components/auth/LoginForm';
import styles from './page.module.css';

const LoginPage = () => {
  const router = useRouter();

  const handleSubmit = async (data: LoginData) => {
    // Handle login
    router.push('/student/dashboard');
  };

  return (
    <div className={styles.container}>
      <div className={styles.card}>
        <h1 className={styles.title}>Login</h1>
        <LoginForm onSubmit={handleSubmit} />
      </div>
    </div>
  );
};

export default LoginPage;
```

### Component Responsibilities

#### **Auth Components**
- **LoginPage.jsx**: Handle student & staff login with role selection
- **RegisterPage.jsx**: Student registration form
- **ForgotPasswordPage.jsx**: Password reset request
- **ResetPasswordPage.jsx**: Password reset with token

#### **Student Components**
- **StudentDashboard.jsx**: Main student view (token, queue, profile)
- **TokenGeneration.jsx**: Generate new token with queue preview
- **QueueStatus.jsx**: Display both counters (A & B) status
- **TokenHistory.jsx**: Past tokens with dates & statuses
- **StudentProfile.jsx**: Edit name, email, change password

#### **Counter/Staff Components**
- **CounterDashboard.jsx**: Staff main view (call next, break, config)
- **CallNextToken.jsx**: Call next token, complete, drop operations
- **BreakManagement.jsx**: Start/end break with reason & duration
- **CounterStatus.jsx**: Display current counter stats & daily limit

#### **Common Components**
- **Navbar.jsx**: Top navigation with logout
- **Sidebar.jsx**: Navigation menu
- **ProtectedRoute.jsx**: Middleware for authenticated routes
- **LoadingSpinner.jsx**: Loading UI indicator

---

## API Endpoints Reference

### Base URL
```
http://localhost:8080/api
```

### Authentication Endpoints
```
POST   /auth/register          - Register new student
POST   /auth/login             - Login (student or staff)
POST   /auth/forgot-password   - Request password reset
POST   /auth/reset-password    - Reset password with token
GET    /auth/test              - Test API connectivity
```

### Student Endpoints
```
POST   /student/tokens/generate      - Generate new token
GET    /student/tokens/my-token      - Get current token
DELETE /student/tokens/cancel        - Cancel token
GET    /student/tokens/position      - Get queue position
GET    /student/tokens/history       - Get token history
GET    /student/queue/status         - Get all queues status
GET    /student/queue/waiting-count  - Total waiting count
GET    /student/profile              - Get profile
PUT    /student/profile              - Update profile
PUT    /student/change-password      - Change password
```

### Counter/Staff Endpoints
```
POST   /counter/tokens/call-next     - Call next token
POST   /counter/tokens/complete      - Complete current token
POST   /counter/tokens/drop          - Drop current token
POST   /counter/break/start          - Start break (query params: reason, estimatedDuration)
POST   /counter/break/end            - End break
POST   /counter/queue/stop-reschedule - Stop queue & reschedule to tomorrow
POST   /counter/queue/stop-expire     - Stop queue & expire tokens
GET    /counter/status               - Get counter status
PUT    /counter/config/daily-limit   - Update daily limit (query param: newLimit)
```

---

## Request/Response Examples

### 1. Register Student
```
POST /auth/register
Content-Type: application/json

REQUEST:
{
  "rollNumber": "ROLL001",
  "name": "John Doe",
  "email": "john@example.com",
  "password": "SecurePass123",
  "confirmPassword": "SecurePass123"
}

RESPONSE (200):
{
  "success": true,
  "message": "Student registered successfully!",
  "data": null
}

RESPONSE (400):
{
  "success": false,
  "message": "Roll number already registered: ROLL001",
  "data": null
}
```

### 2. Login Student
```
POST /auth/login
Content-Type: application/json

REQUEST:
{
  "identifier": "ROLL001",
  "password": "SecurePass123",
  "userType": "STUDENT",
  "selectedCounter": null
}

RESPONSE (200):
{
  "success": true,
  "message": "Login successful!",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userType": "STUDENT",
    "identifier": "ROLL001",
    "message": "Login successful!",
    "assignedCounter": null
  }
}

RESPONSE (401):
{
  "success": false,
  "message": "Invalid password!",
  "data": null
}
```

### 3. Login Staff/Counter
```
POST /auth/login
Content-Type: application/json

REQUEST:
{
  "identifier": "STAFF001",
  "password": "StaffPass123",
  "userType": "STAFF",
  "selectedCounter": "COUNTER_A"
}

RESPONSE (200):
{
  "success": true,
  "message": "Login successful!",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userType": "STAFF",
    "identifier": "STAFF001",
    "message": "Login successful!",
    "assignedCounter": "COUNTER_A"
  }
}
```

### 4. Generate Token (Student)
```
POST /student/tokens/generate
Authorization: Bearer {jwt_token}
Content-Type: application/json

RESPONSE (200):
{
  "success": true,
  "message": "Token generated successfully!",
  "data": {
    "id": 1,
    "tokenNumber": "A001",
    "studentRollNumber": "ROLL001",
    "status": "WAITING",
    "counter": "COUNTER_A",
    "generatedAt": "2026-02-21T10:30:00",
    "position": 1
  }
}

RESPONSE (409 - Conflict):
{
  "success": false,
  "message": "Student already has an active token!",
  "data": null
}
```

### 5. Get My Token
```
GET /student/tokens/my-token
Authorization: Bearer {jwt_token}

RESPONSE (200):
{
  "success": true,
  "message": "Token fetched successfully!",
  "data": {
    "id": 1,
    "tokenNumber": "A001",
    "studentRollNumber": "ROLL001",
    "status": "WAITING",
    "counter": "COUNTER_A",
    "position": 3,
    "generatedAt": "2026-02-21T10:30:00"
  }
}

RESPONSE (404):
{
  "success": false,
  "message": "No active token found for this student",
  "data": null
}
```

### 6. Cancel Token
```
DELETE /student/tokens/cancel
Authorization: Bearer {jwt_token}

RESPONSE (200):
{
  "success": true,
  "message": "Token cancelled successfully!",
  "data": null
}
```

### 7. Get Queue Position
```
GET /student/tokens/position
Authorization: Bearer {jwt_token}

RESPONSE (200):
{
  "success": true,
  "message": "Position fetched!",
  "data": 5
}
```

### 8. Get Token History
```
GET /student/tokens/history
Authorization: Bearer {jwt_token}

RESPONSE (200):
{
  "success": true,
  "message": "Token history fetched!",
  "data": [
    {
      "id": 1,
      "tokenNumber": "A001",
      "generatedAt": "2026-02-20T09:00:00",
      "completedAt": "2026-02-20T09:15:00",
      "status": "COMPLETED",
      "counter": "COUNTER_A",
      "serviceTime": 15
    },
    {
      "id": 2,
      "tokenNumber": "A055",
      "generatedAt": "2026-02-21T10:30:00",
      "completedAt": "2026-02-21T10:45:00",
      "status": "COMPLETED",
      "counter": "COUNTER_A",
      "serviceTime": 15
    }
  ]
}
```

### 9. Get Queue Status (All Counters)
```
GET /student/queue/status
(No authentication required)

RESPONSE (200):
{
  "success": true,
  "message": "Queue status fetched!",
  "data": [
    {
      "counterName": "COUNTER_A",
      "currentToken": "A015",
      "waitingCount": 12,
      "status": "ACTIVE",
      "averageServiceTime": 5.5,
      "tokensServedToday": 23
    },
    {
      "counterName": "COUNTER_B",
      "currentToken": "B008",
      "waitingCount": 8,
      "status": "ON_BREAK",
      "averageServiceTime": 4.2,
      "tokensServedToday": 18
    }
  ]
}
```

### 10. Get Total Waiting Count
```
GET /student/queue/waiting-count
(No authentication required)

RESPONSE (200):
{
  "success": true,
  "message": "Total waiting count!",
  "data": 20
}
```

### 11. Get Student Profile
```
GET /student/profile
Authorization: Bearer {jwt_token}

RESPONSE (200):
{
  "success": true,
  "message": "Profile fetched!",
  "data": {
    "rollNumber": "ROLL001",
    "name": "John Doe",
    "email": "john@example.com",
    "registeredAt": "2026-01-15T10:00:00",
    "updatedAt": "2026-02-21T09:30:00"
  }
}
```

### 12. Update Student Profile
```
PUT /student/profile
Authorization: Bearer {jwt_token}
Content-Type: application/json

REQUEST:
{
  "name": "John Smith",
  "email": "johnsmith@example.com"
}

RESPONSE (200):
{
  "success": true,
  "message": "Profile updated successfully!",
  "data": null
}
```

### 13. Change Password
```
PUT /student/change-password
Authorization: Bearer {jwt_token}
Content-Type: application/json

REQUEST:
{
  "currentPassword": "OldPass123",
  "newPassword": "NewPass123",
  "confirmNewPassword": "NewPass123"
}

RESPONSE (200):
{
  "success": true,
  "message": "Password changed successfully!",
  "data": null
}

RESPONSE (400):
{
  "success": false,
  "message": "Current password is incorrect!",
  "data": null
}
```

### 14. Forgot Password
```
POST /auth/forgot-password?email=john@example.com
Content-Type: application/json

RESPONSE (200):
{
  "success": true,
  "message": "If an account with that email exists, a reset link has been sent.",
  "data": null
}
```

### 15. Reset Password
```
POST /auth/reset-password
Content-Type: application/json

REQUEST:
{
  "token": "uuid-token-from-email",
  "newPassword": "NewPass123",
  "confirmNewPassword": "NewPass123"
}

RESPONSE (200):
{
  "success": true,
  "message": "Password reset successfully! You can now log in with your new password.",
  "data": null
}

RESPONSE (400):
{
  "success": false,
  "message": "Invalid or already used reset token!",
  "data": null
}
```

### 16. Call Next Token (Staff)
```
POST /counter/tokens/call-next
Authorization: Bearer {staff_jwt_token}

RESPONSE (200):
{
  "success": true,
  "message": "Next token called!",
  "data": {
    "id": 5,
    "tokenNumber": "A016",
    "studentRollNumber": "ROLL005",
    "status": "CALLING",
    "counter": "COUNTER_A"
  }
}

RESPONSE (404):
{
  "success": false,
  "message": "No waiting tokens found!",
  "data": null
}
```

### 17. Complete Token (Staff)
```
POST /counter/tokens/complete
Authorization: Bearer {staff_jwt_token}

RESPONSE (200):
{
  "success": true,
  "message": "Token completed!",
  "data": {
    "id": 5,
    "tokenNumber": "A016",
    "studentRollNumber": "ROLL005",
    "status": "COMPLETED",
    "counter": "COUNTER_A",
    "serviceTime": 8
  }
}
```

### 18. Drop Token (Staff)
```
POST /counter/tokens/drop
Authorization: Bearer {staff_jwt_token}

RESPONSE (200):
{
  "success": true,
  "message": "Token dropped!",
  "data": {
    "id": 5,
    "tokenNumber": "A016",
    "studentRollNumber": "ROLL005",
    "status": "DROPPED",
    "counter": "COUNTER_A"
  }
}
```

### 19. Start Break (Staff)
```
POST /counter/break/start?reason=Lunch&estimatedDuration=30
Authorization: Bearer {staff_jwt_token}

RESPONSE (200):
{
  "success": true,
  "message": "Break started!",
  "data": null
}
```

### 20. End Break (Staff)
```
POST /counter/break/end
Authorization: Bearer {staff_jwt_token}

RESPONSE (200):
{
  "success": true,
  "message": "Break ended! Counter is now active.",
  "data": null
}
```

### 21. Get Counter Status (Staff)
```
GET /counter/status
Authorization: Bearer {staff_jwt_token}

RESPONSE (200):
{
  "success": true,
  "message": "Counter status fetched!",
  "data": {
    "counterName": "COUNTER_A",
    "currentToken": "A015",
    "waitingCount": 12,
    "status": "ACTIVE",
    "tokensServedToday": 23,
    "breakCount": 2,
    "totalBreakTime": 65,
    "averageServiceTime": 5.5,
    "dailyLimit": 50
  }
}
```

### 22. Update Daily Limit (Staff)
```
PUT /counter/config/daily-limit?newLimit=60
Authorization: Bearer {staff_jwt_token}

RESPONSE (200):
{
  "success": true,
  "message": "Daily limit updated to: 60",
  "data": null
}
```

### 23. Stop & Reschedule Queue (Staff)
```
POST /counter/queue/stop-reschedule
Authorization: Bearer {staff_jwt_token}

RESPONSE (200):
{
  "success": true,
  "message": "Queue stopped! Waiting tokens rescheduled to tomorrow.",
  "data": null
}
```

### 24. Stop & Expire Queue (Staff)
```
POST /counter/queue/stop-expire
Authorization: Bearer {staff_jwt_token}

RESPONSE (200):
{
  "success": true,
  "message": "Queue stopped! All waiting tokens expired.",
  "data": null
}
```

### 25. Test Endpoint
```
GET /auth/test
(No authentication required)

RESPONSE (200):
{
  "success": true,
  "message": "API is working!",
  "data": "Hello from Queue Management System!"
}
```

---

## Implementation Details

### Environment Variables (.env.local)
```
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/api
NEXT_PUBLIC_JWT_TOKEN_KEY=queue_mgmt_token
```

### TypeScript Configuration (tsconfig.json)
```json
{
  "compilerOptions": {
    "target": "ES2020",
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "jsx": "react-jsx",
    "module": "ESNext",
    "moduleResolution": "bundler",
    "strict": true,
    "skipLibCheck": true,
    "baseUrl": ".",
    "paths": {
      "@/*": ["./*"]
    }
  },
  "include": ["next-env.d.ts", "**/*.ts", "**/*.tsx"],
  "exclude": ["node_modules"]
}
```

### Next.js Configuration (next.config.js)
```javascript
/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  swcMinify: true,
};

module.exports = nextConfig;
```

### Modular Services Architecture (TypeScript)

Each service has its own folder with separate files:

#### API Service (services/api/apiClient.ts)
```typescript
import axios, { AxiosInstance, AxiosError, AxiosResponse } from 'axios';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080/api';
const TOKEN_KEY = process.env.NEXT_PUBLIC_JWT_TOKEN_KEY || 'queue_mgmt_token';

const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add token
apiClient.interceptors.request.use(
  (config) => {
    if (typeof window !== 'undefined') {
      const token = localStorage.getItem(TOKEN_KEY);
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to handle errors
apiClient.interceptors.response.use(
  (response: AxiosResponse) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      if (typeof window !== 'undefined') {
        localStorage.clear();
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default apiClient;
```

#### Auth Service Module (services/auth/authService.ts)
```typescript
import apiClient from '../api/apiClient';
import type { LoginResponse, RegisterData, ResetPasswordData } from '@/types';

export const register = (data: RegisterData) => 
  apiClient.post('/auth/register', data);

export const login = (identifier: string, password: string, userType: string, selectedCounter?: string) =>
  apiClient.post<{data: LoginResponse}>('/auth/login', {
    identifier,
    password,
    userType,
    selectedCounter,
  });

export const forgotPassword = (email: string) =>
  apiClient.post('/auth/forgot-password', null, { params: { email } });

export const resetPassword = (data: ResetPasswordData) =>
  apiClient.post('/auth/reset-password', data);
```

#### Student Service Module (services/student/studentService.ts)
```typescript
import apiClient from '../api/apiClient';
import type { TokenResponse, QueueStatusResponse } from '@/types';

export const generateToken = () => 
  apiClient.post<{data: TokenResponse}>('/student/tokens/generate');

export const getMyToken = () =>
  apiClient.get<{data: TokenResponse}>('/student/tokens/my-token');

export const cancelToken = () =>
  apiClient.delete('/student/tokens/cancel');

export const getPosition = () =>
  apiClient.get<{data: number}>('/student/tokens/position');

export const getTokenHistory = () =>
  apiClient.get('/student/tokens/history');

export const getQueueStatus = () =>
  apiClient.get<{data: QueueStatusResponse[]}>('/student/queue/status');

export const getWaitingCount = () =>
  apiClient.get<{data: number}>('/student/queue/waiting-count');

export const getProfile = () =>
  apiClient.get('/student/profile');

export const updateProfile = (name: string, email: string) =>
  apiClient.put('/student/profile', { name, email });

export const changePassword = (currentPassword: string, newPassword: string, confirmNewPassword: string) =>
  apiClient.put('/student/change-password', {
    currentPassword,
    newPassword,
    confirmNewPassword,
  });
```

#### Counter Service Module (services/counter/counterService.ts)
```typescript
import apiClient from '../api/apiClient';
import type { TokenResponse, CounterStatusResponse } from '@/types';

export const callNext = () =>
  apiClient.post<{data: TokenResponse}>('/counter/tokens/call-next');

export const completeToken = () =>
  apiClient.post<{data: TokenResponse}>('/counter/tokens/complete');

export const dropToken = () =>
  apiClient.post<{data: TokenResponse}>('/counter/tokens/drop');

export const startBreak = (reason: string, estimatedDuration: number) =>
  apiClient.post('/counter/break/start', null, {
    params: { reason, estimatedDuration },
  });

export const endBreak = () =>
  apiClient.post('/counter/break/end');

export const getCounterStatus = () =>
  apiClient.get<{data: CounterStatusResponse}>('/counter/status');

export const updateDailyLimit = (newLimit: number) =>
  apiClient.put('/counter/config/daily-limit', null, {
    params: { newLimit },
  });

export const stopAndReschedule = () =>
  apiClient.post('/counter/queue/stop-reschedule');

export const stopAndExpire = () =>
  apiClient.post('/counter/queue/stop-expire');
```

### Type Definitions (types/)

Create TypeScript interfaces for all data types:

#### types/auth.ts
```typescript
export interface User {
  identifier: string;
  userType: 'STUDENT' | 'STAFF';
  token: string;
  assignedCounter?: string;
}

export interface LoginResponse {
  token: string;
  userType: string;
  identifier: string;
  assignedCounter?: string;
  message: string;
}

export interface RegisterData {
  rollNumber: string;
  name: string;
  email: string;
  password: string;
  confirmPassword: string;
}

export interface ResetPasswordData {
  token: string;
  newPassword: string;
  confirmNewPassword: string;
}

export interface ProfileResponse {
  rollNumber: string;
  name: string;
  email: string;
  registeredAt: string;
  updatedAt: string;
}
```

#### types/queue.ts
```typescript
export interface TokenResponse {
  id: number;
  tokenNumber: string;
  studentRollNumber: string;
  status: 'WAITING' | 'CALLING' | 'COMPLETED' | 'CANCELLED' | 'DROPPED';
  counter: string;
  generatedAt: string;
  position?: number;
}

export interface QueueStatusResponse {
  counterName: string;
  currentToken: string;
  waitingCount: number;
  status: string;
  averageServiceTime: number;
  tokensServedToday: number;
}

export interface CounterStatusResponse {
  counterName: string;
  status: string;
  currentToken: string;
  tokensServedToday: number;
  breakCount: number;
  totalBreakTime: number;
  averageServiceTime: number;
  dailyLimit: number;
}
```

### Middleware for Authentication (lib/middleware.ts)
```typescript
import { NextRequest, NextResponse } from 'next/server';

const protectedRoutes = [
  '/student/dashboard',
  '/student/profile',
  '/counter/dashboard',
];

const authRoutes = ['/login', '/register', '/forgot-password'];

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;
  const token = request.cookies.get(process.env.NEXT_PUBLIC_JWT_TOKEN_KEY!)?.value;

  // Redirect authenticated users away from auth pages
  if (authRoutes.includes(pathname) && token) {
    return NextResponse.redirect(new URL('/student/dashboard', request.url));
  }

  // Redirect unauthenticated users to login
  if (protectedRoutes.some((route) => pathname.startsWith(route)) && !token) {
    return NextResponse.redirect(new URL('/login', request.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: ['/((?!api|_next/static|_next/image|favicon.ico).*)'],
};
```

### Modular Context Architecture (TypeScript)

#### contexts/authContext.tsx
```typescript
'use client';

import { createContext, ReactNode, useState, useCallback, useEffect } from 'react';
import type { User } from '@/types';

export interface AuthContextType {
  user: User | null;
  loading: boolean;
  error: string | null;
  login: (identifier: string, password: string, userType: string, selectedCounter?: string) => Promise<void>;
  logout: () => void;
  isAuthenticated: boolean;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const token = localStorage.getItem(process.env.NEXT_PUBLIC_JWT_TOKEN_KEY || 'queue_mgmt_token');
    const userType = localStorage.getItem('userType');
    const identifier = localStorage.getItem('identifier');
    
    if (token && userType && identifier) {
      setUser({ token, userType: userType as 'STUDENT' | 'STAFF', identifier });
    }
  }, []);

  const login = useCallback(async (identifier: string, password: string, userType: string, selectedCounter?: string) => {
    setLoading(true);
    setError(null);
    // Login logic implementation
  }, []);

  const logout = useCallback(() => {
    localStorage.clear();
    setUser(null);
    setError(null);
  }, []);

  return (
    <AuthContext.Provider value={{
      user,
      loading,
      error,
      login,
      logout,
      isAuthenticated: !!user,
    }}>
      {children}
    </AuthContext.Provider>
  );
}
```

### Custom Hooks (TypeScript)

#### hooks/useAuth.ts
```typescript
'use client';

import { useContext } from 'react';
import { AuthContext, type AuthContextType } from '@/contexts/authContext';

export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);
  
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  
  return context;
}
```

#### hooks/useFetch.ts
```typescript
'use client';

import { useState, useEffect } from 'react';

interface UseFetchReturn<T> {
  data: T | null;
  loading: boolean;
  error: string | null;
}

export function useFetch<T>(asyncFunction: () => Promise<any>): UseFetchReturn<T> {
  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const response = await asyncFunction();
        setData(response.data?.data);
        setError(null);
      } catch (err: any) {
        setError(err.response?.data?.message || 'An error occurred');
        setData(null);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [asyncFunction]);

  return { data, loading, error };
}
```

### Modular Services Architecture

Each service has its own folder with separate files:

#### API Service (services/api)
```
services/
├── api/
│   ├── api.js
│   └── index.js
├── auth/
│   ├── authService.js
│   └── index.js
├── student/
│   ├── studentService.js
│   └── index.js
├── counter/
│   ├── counterService.js
│   └── index.js
├── queue/
│   ├── queueService.js
│   └── index.js
└── index.js (main barrel export)

// services/api/api.js
import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor
api.interceptors.request.use(/* ... */);

// Response interceptor
api.interceptors.response.use(/* ... */);

export default api;

// services/api/index.js
export { default } from './api';

// services/index.js (main barrel export)
export { default as api } from './api';
export * as authService from './auth';
export * as studentService from './student';
export * as counterService from './counter';
export * as queueService from './queue';
```

### Styled Button Component (components/common/Button/Button.tsx)
```typescript
'use client';

import { FC, ButtonHTMLAttributes } from 'react';
import styles from './Button.module.css';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'danger';
  fullWidth?: boolean;
  loading?: boolean;
  children: React.ReactNode;
}

const Button: FC<ButtonProps> = ({ 
  variant = 'primary', 
  fullWidth = false, 
  loading = false, 
  disabled,
  children,
  className,
  ...props 
}) => {
  const classNames = [
    styles.button,
    styles[variant],
    fullWidth && styles.fullWidth,
    (loading || disabled) && styles.disabled,
    className,
  ].filter(Boolean).join(' ');

  return (
    <button 
      className={classNames} 
      disabled={disabled || loading}
      {...props}
    >
      {loading ? 'Loading...' : children}
    </button>
  );
};

export default Button;
```

### Card Component (components/common/Card/Card.tsx)
```typescript
'use client';

import { FC, ReactNode } from 'react';
import styles from './Card.module.css';

interface CardProps {
  children: ReactNode;
  className?: string;
}

const Card: FC<CardProps> = ({ children, className }) => {
  return (
    <div className={`${styles.card} ${className || ''}`}>
      {children}
    </div>
  );
};

export default Card;
```

### Modal Component (components/common/Modal/Modal.tsx)
```typescript
'use client';

import { FC, ReactNode } from 'react';
import styles from './Modal.module.css';

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  children: ReactNode;
}

const Modal: FC<ModalProps> = ({ isOpen, onClose, title, children }) => {
  if (!isOpen) return null;

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
        <div className={styles.header}>
          <h2 className={styles.title}>{title}</h2>
          <button className={styles.closeBtn} onClick={onClose}>×</button>
        </div>
        <div className={styles.content}>
          {children}
        </div>
      </div>
    </div>
  );
};

export default Modal;
```

### Page Component Example (app/student/dashboard/page.tsx)
```typescript
'use client';

import { useAuth } from '@/hooks';
import { studentService } from '@/services';
import { useFetch } from '@/hooks';
import TokenGeneration from '@/components/student/TokenGeneration';
import QueueStatus from '@/components/student/QueueStatus';
import styles from './page.module.css';

export default function StudentDashboard() {
  const { user } = useAuth();
  const { data: token, loading: tokenLoading } = useFetch(() => 
    studentService.getMyToken()
  );

  return (
    <div className={styles.dashboard}>
      <h1 className={styles.title}>Welcome, {user?.identifier}</h1>
      
      <div className={styles.grid}>
        <section className={styles.section}>
          <h2>Token Management</h2>
          <TokenGeneration />
        </section>

        <section className={styles.section}>
          <h2>Queue Status</h2>
          <QueueStatus />
        </section>
      </div>
    </div>
  );
}
```

### Authentication Headers
```javascript
// All authenticated endpoints require:
headers: {
  'Authorization': `Bearer ${jwt_token}`,
  'Content-Type': 'application/json'
}
```

### Error Response Format
```json
{
  "success": false,
  "message": "Error description here",
  "data": null
}
```

### Success Response Format
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { /* actual data */ }
}
```

---

## Key Features

### Authentication
- ✅ JWT token-based authentication
- ✅ Token persistence in localStorage
- ✅ Auto-logout on token expiry (401 response)
- ✅ Role-based access control (STUDENT vs STAFF)
- ✅ Password reset via email
- ✅ Password change for logged-in users

### Student Features
- ✅ Register with roll number & email
- ✅ Generate token for queue
- ✅ View token status & position
- ✅ View both counters (A & B) status
- ✅ Get token history
- ✅ Cancel active token
- ✅ Update profile (name, email)
- ✅ Change password
- ✅ View total waiting count

### Counter/Staff Features
- ✅ Login and select counter (A or B)
- ✅ Call next token in queue
- ✅ Complete token with service time
- ✅ Drop token
- ✅ Start break with reason & duration
- ✅ End break
- ✅ View counter status & daily statistics
- ✅ Update daily token limit
- ✅ Stop queue & reschedule/expire tokens

### State Management
- **AuthContext**: Handle authentication state, login, logout, token
- **QueueContext**: Handle queue status, current tokens, positions

### Custom Hooks
- **useAuth()**: Access auth state & methods
- **useQueue()**: Access queue state & methods

### Common Components
- **ProtectedRoute**: Middleware for authenticated pages
- **LoadingSpinner**: Loading indicator UI
- **Navbar**: Navigation with logout
- **Sidebar**: Menu navigation

### Styling (CSS Modules - Fully Modular)

Each component has its own scoped CSS file:

#### Global Styles Structure
```
styles/
├── global.css        # Global resets & base styles
├── variables.css     # CSS variables (colors, spacing, fonts)
├── reset.css         # CSS reset rules
└── index.css         # Import all global styles
```

#### styles/variables.css
```css
:root {
  /* Colors */
  --color-primary: #1e40af;
  --color-secondary: #64748b;
  --color-success: #16a34a;
  --color-danger: #dc2626;
  --color-warning: #ea580c;
  --color-bg: #f8fafc;
  --color-border: #e2e8f0;
  --color-text: #1e293b;
  
  /* Spacing */
  --spacing-xs: 4px;
  --spacing-sm: 8px;
  --spacing-md: 16px;
  --spacing-lg: 24px;
  --spacing-xl: 32px;
  
  /* Typography */
  --font-base: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  --font-size-sm: 12px;
  --font-size-md: 14px;
  --font-size-lg: 16px;
  --font-size-xl: 20px;
  
  /* Border */
  --border-radius: 8px;
  --border-width: 1px;
  
  /* Shadows */
  --shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.05);
  --shadow-md: 0 4px 6px rgba(0, 0, 0, 0.1);
  --shadow-lg: 0 10px 15px rgba(0, 0, 0, 0.1);
}
```

#### Component CSS Module Example
```css
/* LoginPage.module.css */
.container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: var(--color-bg);
  padding: var(--spacing-md);
}

.card {
  background: white;
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-md);
  padding: var(--spacing-lg);
  width: 100%;
  max-width: 400px;
}

.title {
  font-size: var(--font-size-xl);
  color: var(--color-text);
  margin-bottom: var(--spacing-lg);
  text-align: center;
  font-weight: 600;
}

.form {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.formGroup {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.label {
  font-size: var(--font-size-md);
  color: var(--color-text);
  font-weight: 500;
}

.input {
  padding: var(--spacing-sm) var(--spacing-md);
  border: var(--border-width) solid var(--color-border);
  border-radius: var(--border-radius);
  font-size: var(--font-size-md);
  font-family: var(--font-base);
  transition: border-color 0.2s;
}

.input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(30, 64, 175, 0.1);
}

.button {
  padding: var(--spacing-sm) var(--spacing-md);
  background-color: var(--color-primary);
  color: white;
  border: none;
  border-radius: var(--border-radius);
  font-size: var(--font-size-md);
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
}

.button:hover {
  background-color: #1e3a8a;
}

.button:active {
  transform: scale(0.98);
}

.link {
  color: var(--color-primary);
  text-decoration: none;
  cursor: pointer;
  transition: color 0.2s;
}

.link:hover {
  text-decoration: underline;
}

.error {
  color: var(--color-danger);
  font-size: var(--font-size-sm);
  margin-top: var(--spacing-sm);
}

.success {
  color: var(--color-success);
  font-size: var(--font-size-sm);
  margin-top: var(--spacing-sm);
}

@media (max-width: 640px) {
  .card {
    padding: var(--spacing-md);
  }
  
  .title {
    font-size: var(--font-size-lg);
  }
}
```

### Reusable Component Modules

#### Button Module (Common/Button)
```
Button/
├── Button.jsx
├── Button.module.css
└── index.js

// Button.jsx - Reusable across all components
export default function Button({ 
  text, 
  onClick, 
  type = 'primary', 
  disabled = false,
  fullWidth = false 
}) {
  // Implementation
}
```

#### Card Module (Common/Card)
```
Card/
├── Card.jsx
├── Card.module.css
└── index.js

// Card.jsx - Reusable container
export default function Card({ children, className }) {
  // Implementation
}
```

#### Modal Module (Common/Modal)
```
Modal/
├── Modal.jsx
├── Modal.module.css
└── index.js

// Modal.jsx - Reusable modal
export default function Modal({ isOpen, onClose, title, children }) {
  // Implementation
}
```

### Real-time Features (Optional)
- WebSocket for live queue updates
- Real-time token calling notifications
- Live counter status updates

---

## Package.json Dependencies
```json
{
  "name": "queue-management-frontend",
  "version": "1.0.0",
  "private": true,
  "scripts": {
    "dev": "next dev",
    "build": "next build",
    "start": "next start",
    "lint": "next lint",
    "type-check": "tsc --noEmit",
    "format": "prettier --write \"**/*.{ts,tsx,css,json,md}\""
  },
  "dependencies": {
    "next": "^14.0.0",
    "react": "^18.2.0",
    "react-dom": "^18.2.0",
    "axios": "^1.6.0"
  },
  "devDependencies": {
    "typescript": "^5.3.0",
    "@types/node": "^20.10.0",
    "@types/react": "^18.2.0",
    "@types/react-dom": "^18.2.0",
    "eslint": "^8.55.0",
    "eslint-config-next": "^14.0.0",
    "prettier": "^3.1.0",
    "@typescript-eslint/eslint-plugin": "^6.13.0",
    "@typescript-eslint/parser": "^6.13.0"
  }
}
```

### CSS Modules Benefits
- ✅ **Scoped Styles**: No global namespace pollution
- ✅ **Name Mangling**: CSS class names are automatically scoped
- ✅ **No Conflicts**: Different components can use same class names
- ✅ **Tree-shakable**: Unused styles don't get included
- ✅ **Type Safety**: Get autocomplete in CSS imports
- ✅ **Easy Maintenance**: Styles live with components

---

---

## Modular Architecture Principles (TypeScript + Next.js)

### 1. Barrel Exports (index.ts Pattern)

Every folder has an `index.ts` file for clean imports:

```typescript
// ❌ Bad - Long imports
import LoginForm from '../components/auth/LoginForm/LoginForm';
import Button from '../components/common/Button';

// ✅ Good - Short, clean imports (using barrel exports)
import { LoginForm, Button } from '../components';
import { useAuth, useQueue } from '../hooks';
import { authService, studentService } from '../services';
```

### 2. Single Responsibility Principle

Each module does one thing well:
- **Pages**: Routing and page structure only (app/*)
- **Components**: UI rendering only (never call services directly, use hooks)
- **Services**: API communication only
- **Hooks**: State logic and side effects only
- **Contexts**: Global state only
- **Types**: Type definitions only
- **Utils**: Helper functions only

### 3. TypeScript Benefits

```typescript
// ✅ Full type safety
interface LoginFormProps {
  onSubmit: (data: LoginData) => Promise<void>;
  isLoading?: boolean;
}

// ✅ Generics for reusable hooks
export function useFetch<T>(asyncFn: () => Promise<any>): UseFetchReturn<T>

// ✅ Type-safe service responses
const apiClient = axios.create<{data: any}>({...})
```

### 4. CSS Module Naming Conventions

```css
/* ✅ Use camelCase for CSS class names in modules */
.container { }
.formGroup { }
.inputError { }

/* ✅ Use BEM-like naming for complex components */
.card { }
.cardHeader { }
.cardBody { }
.cardFooter { }
```

### 5. Component Composition Pattern (TypeScript)

```typescript
// ✅ Modular - Easy to reuse and test
interface LoginPageProps {}

const LoginPage: FC<LoginPageProps> = () => {
  return (
    <div className={styles.container}>
      <Card>
        <LoginForm onSubmit={handleSubmit} />
        <LoginFooter />
      </Card>
    </div>
  );
};

// ✅ Smaller components are more reusable
interface LoginFormProps {
  onSubmit: (data: LoginData) => Promise<void>;
}

const LoginForm: FC<LoginFormProps> = ({ onSubmit }) => {
  // Form logic
};

interface LoginFooterProps {}

const LoginFooter: FC<LoginFooterProps> = () => {
  // Footer logic
};
```

### 6. Next.js Best Practices

```typescript
// ✅ Use 'use client' for interactive components
'use client';

import { useAuth } from '@/hooks';
import styles from './ComponentName.module.css';

// ✅ Use next/navigation for routing
import { useRouter, usePathname } from 'next/navigation';

// ✅ Use path aliases (@/*)
import { useAuth } from '@/hooks';
import Button from '@/components/common/Button';

// ✅ Mark pages with 'use client' only if needed for interactivity
```

### 7. File Organization Best Practices

```
✅ DO:
- Keep components small (max 200 lines)
- One component per file
- Place styles next to components
- Use TypeScript interfaces for all props
- Export types along with components
- Create types/index.ts for re-exports
- Use barrel exports (index.ts) in all folders
- Mark interactive components with 'use client'

❌ DON'T:
- Mix logic, UI, and styles in one file
- Create mega parent components
- Use any types excessively
- Deeply nest folder structures
- Put all styles in global CSS
- Use relative paths beyond 2-3 levels
- Mix server and client logic
```

---

## Notes for Google AI Studio Development

### Development Order (TypeScript + Next.js)
1. **Set up Next.js project** with TypeScript support
2. **Create types/** folder with all interfaces
3. **Create services/** with apiClient and service modules (TypeScript)
4. **Create contexts/** with AuthContext and providers
5. **Create hooks/** (useAuth, useFetch with TypeScript)
6. **Create components/** start with reusable common components (Button, Card, Modal)
7. **Create layout components** (Navbar, Sidebar)
8. **Create auth pages** (login, register, forgot-password)
9. **Create student pages** (dashboard, profile, history, token)
10. **Create counter pages** (dashboard, status)
11. **Add middleware.ts** for route protection
12. **Test all endpoints** with backend

### Key Next.js Features to Use
- ✅ **App Router**: File-based routing with `/app` directory
- ✅ **Server vs Client Components**: Use `'use client'` for interactive components
- ✅ **Middleware**: Route protection and authentication
- ✅ **API Routes**: Optional backend proxy (not needed with external API)
- ✅ **Environment Variables**: Use `NEXT_PUBLIC_*` for client-side vars
- ✅ **Image Optimization**: Built-in Image component
- ✅ **TypeScript**: Full type safety out of the box

### TypeScript + Next.js Best Practices
- Always define component props with interfaces
- Use generics for hooks (useFetch<T>)
- Create type files for all API responses
- Mark Client Components with 'use client'
- Use Next.js Image instead of <img>
- Leverage next/navigation for router
- Use next/link for client-side navigation
- Use environment variables for configuration

### Component Creation Pattern (TypeScript)
```typescript
'use client';  // Mark client component

import { FC } from 'react';
import styles from './ComponentName.module.css';

interface ComponentNameProps {
  title: string;
  onSubmit?: (data: any) => void;
  loading?: boolean;
}

const ComponentName: FC<ComponentNameProps> = ({ 
  title, 
  onSubmit, 
  loading = false 
}) => {
  return (
    <div className={styles.container}>
      <h1>{title}</h1>
    </div>
  );
};

export default ComponentName;
```

### Page Creation Pattern (Next.js)
```typescript
'use client';  // Required for interactive features

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/hooks';
import styles from './page.module.css';

export default function PageName() {
  const router = useRouter();
  const { user } = useAuth();

  useEffect(() => {
    if (!user) {
      router.push('/login');
    }
  }, [user, router]);

  return (
    <div className={styles.page}>
      {/* Content */}
    </div>
  );
}
```

### Service Usage in Components
```typescript
'use client';

import { useState } from 'react';
import { useAuth } from '@/hooks';
import { studentService } from '@/services';
import type { TokenResponse } from '@/types';

export default function TokenComponent() {
  const { user } = useAuth();
  const [token, setToken] = useState<TokenResponse | null>(null);
  const [loading, setLoading] = useState(false);

  const handleGenerateToken = async () => {
    try {
      setLoading(true);
      const response = await studentService.generateToken();
      setToken(response.data.data);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      {token && <p>Token: {token.tokenNumber}</p>}
      <button onClick={handleGenerateToken} disabled={loading}>
        {loading ? 'Generating...' : 'Generate Token'}
      </button>
    </div>
  );
}
```

---

## File Checklist for Google AI Studio (TypeScript + Next.js)

Create these files in order:

### Phase 1: Configuration & Setup
- [ ] `package.json`
- [ ] `package-lock.json` or `yarn.lock`
- [ ] `tsconfig.json`
- [ ] `next.config.js`
- [ ] `.env.local` (example)
- [ ] `.env.example`
- [ ] `.gitignore`
- [ ] `public/favicon.ico`

### Phase 2: Global Styles & Config
- [ ] `app/globals.css`
- [ ] `styles/variables.css`
- [ ] `styles/reset.css`
- [ ] `app/layout.tsx` (root layout)
- [ ] `app/page.tsx` (home page)
- [ ] `config/routes.ts`
- [ ] `config/index.ts`

### Phase 3: Types & TypeScript Interfaces
- [ ] `types/auth.ts`
- [ ] `types/queue.ts`
- [ ] `types/student.ts`
- [ ] `types/counter.ts`
- [ ] `types/index.ts`

### Phase 4: Services Layer
- [ ] `services/api/apiClient.ts`
- [ ] `services/api/index.ts`
- [ ] `services/auth/authService.ts`
- [ ] `services/auth/index.ts`
- [ ] `services/student/studentService.ts`
- [ ] `services/student/index.ts`
- [ ] `services/counter/counterService.ts`
- [ ] `services/counter/index.ts`
- [ ] `services/index.ts`

### Phase 5: Contexts & Providers
- [ ] `contexts/authContext.tsx`
- [ ] `contexts/queueContext.tsx`
- [ ] `contexts/index.ts`
- [ ] `components/providers/AuthProvider.tsx`
- [ ] `components/providers/ClientProvider.tsx`
- [ ] `components/providers/index.ts`

### Phase 6: Custom Hooks
- [ ] `hooks/useAuth.ts`
- [ ] `hooks/useQueue.ts`
- [ ] `hooks/useFetch.ts`
- [ ] `hooks/index.ts`

### Phase 7: Common Reusable Components
- [ ] `components/common/Button/Button.tsx`
- [ ] `components/common/Button/Button.module.css`
- [ ] `components/common/Button/index.ts`
- [ ] `components/common/Card/Card.tsx`
- [ ] `components/common/Card/Card.module.css`
- [ ] `components/common/Card/index.ts`
- [ ] `components/common/Modal/Modal.tsx`
- [ ] `components/common/Modal/Modal.module.css`
- [ ] `components/common/Modal/index.ts`
- [ ] `components/common/LoadingSpinner/LoadingSpinner.tsx`
- [ ] `components/common/LoadingSpinner/LoadingSpinner.module.css`
- [ ] `components/common/LoadingSpinner/index.ts`
- [ ] `components/common/index.ts`

### Phase 8: Layout Components
- [ ] `components/common/Navbar/Navbar.tsx`
- [ ] `components/common/Navbar/Navbar.module.css`
- [ ] `components/common/Navbar/index.ts`
- [ ] `components/common/Sidebar/Sidebar.tsx`
- [ ] `components/common/Sidebar/Sidebar.module.css`
- [ ] `components/common/Sidebar/index.ts`

### Phase 9: Auth Pages & Components
- [ ] `app/(auth)/layout.tsx`
- [ ] `app/(auth)/login/page.tsx`
- [ ] `app/(auth)/login/page.module.css`
- [ ] `app/(auth)/register/page.tsx`
- [ ] `app/(auth)/register/page.module.css`
- [ ] `app/(auth)/forgot-password/page.tsx`
- [ ] `app/(auth)/forgot-password/page.module.css`
- [ ] `app/(auth)/reset-password/page.tsx`
- [ ] `app/(auth)/reset-password/page.module.css`
- [ ] `components/auth/LoginForm/LoginForm.tsx`
- [ ] `components/auth/LoginForm/LoginForm.module.css`
- [ ] `components/auth/LoginForm/index.ts`
- [ ] `components/auth/RegisterForm/RegisterForm.tsx`
- [ ] `components/auth/RegisterForm/RegisterForm.module.css`
- [ ] `components/auth/RegisterForm/index.ts`
- [ ] `components/auth/index.ts`

### Phase 10: Student Pages & Components
- [ ] `app/student/layout.tsx`
- [ ] `app/student/dashboard/page.tsx`
- [ ] `app/student/dashboard/page.module.css`
- [ ] `app/student/profile/page.tsx`
- [ ] `app/student/profile/page.module.css`
- [ ] `app/student/history/page.tsx`
- [ ] `app/student/history/page.module.css`
- [ ] `app/student/token/page.tsx`
- [ ] `app/student/token/page.module.css`
- [ ] `components/student/TokenGeneration/TokenGeneration.tsx`
- [ ] `components/student/TokenGeneration/TokenGeneration.module.css`
- [ ] `components/student/TokenGeneration/index.ts`
- [ ] `components/student/QueueStatus/QueueStatus.tsx`
- [ ] `components/student/QueueStatus/QueueStatus.module.css`
- [ ] `components/student/QueueStatus/index.ts`
- [ ] `components/student/TokenHistory/TokenHistory.tsx`
- [ ] `components/student/TokenHistory/TokenHistory.module.css`
- [ ] `components/student/TokenHistory/index.ts`
- [ ] `components/student/index.ts`

### Phase 11: Counter Pages & Components
- [ ] `app/counter/layout.tsx`
- [ ] `app/counter/dashboard/page.tsx`
- [ ] `app/counter/dashboard/page.module.css`
- [ ] `app/counter/status/page.tsx`
- [ ] `app/counter/status/page.module.css`
- [ ] `components/counter/CallNextToken/CallNextToken.tsx`
- [ ] `components/counter/CallNextToken/CallNextToken.module.css`
- [ ] `components/counter/CallNextToken/index.ts`
- [ ] `components/counter/BreakManagement/BreakManagement.tsx`
- [ ] `components/counter/BreakManagement/BreakManagement.module.css`
- [ ] `components/counter/BreakManagement/index.ts`
- [ ] `components/counter/index.ts`

### Phase 12: Middleware & Finalization
- [ ] `lib/middleware.ts`
- [ ] `lib/index.ts`
- [ ] `README.md`

---

## Summary

This fully modular **TypeScript + Next.js** setup provides:

✅ **Type Safety**: Full TypeScript support for compile-time error checking  
✅ **File-Based Routing**: No router configuration needed  
✅ **Server Components**: Better performance with Server-Side Rendering  
✅ **Built-in Middleware**: Route protection without external dependencies  
✅ **Environment Variables**: NEXT_PUBLIC_ for client-side, secure backend vars  
✅ **CSS Modules**: Scoped styles prevent naming conflicts  
✅ **Maintainability**: Find code quickly, understand structure easily  
✅ **Reusability**: Use components, hooks, and services across project  
✅ **Testability**: Each module can be tested independently  
✅ **Developer Experience**: TypeScript, linting, and formatting built-in  
✅ **Production Ready**: Optimized builds, code splitting, tree-shaking  
✅ **Real-time Features**: WebSocket support for live queue updates  

### Next.js Advantages Over React + Vite
- **Built-in Routing**: No react-router-dom needed
- **Middleware**: Native authentication & route protection
- **API Routes**: Optional backend proxy (not needed here)
- **Image Optimization**: Automatic image optimization
- **Performance**: Automatic code splitting & optimization
- **SSR/SSG**: Server-side rendering out of the box
- **TypeScript**: First-class TypeScript support
- **Environment Variables**: Automatic client/server separation

This comprehensive setup is ideal for production applications and scales beautifully as your project grows!
