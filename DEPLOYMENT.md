# 🚀 Theater Management - Complete Deployment Guide

**Project:** Theater Management System (Full-stack)
**Tech Stack:** Next.js 16 (Frontend) + Spring Boot (Backend)
**Last Updated:** 2025-11-29

---

## 📋 Table of Contents

1. [Project Overview](#project-overview)
2. [Prerequisites](#prerequisites)
3. [Environment Configuration](#environment-configuration)
4. [Frontend Deployment (Vercel)](#frontend-deployment-vercel)
5. [Backend Deployment Options](#backend-deployment-options)
6. [Post-Deployment Setup](#post-deployment-setup)
7. [Maintenance & Updates](#maintenance--updates)
8. [Troubleshooting](#troubleshooting)

---

## Project Overview

### Architecture

```
User Browser
    ↓
Vercel Frontend (Next.js 16)
    - SSR + Client-side rendering
    - SEO optimized (metadata, sitemap, structured data)
    - PWA support
    ↓
Backend API (Spring Boot)
    - REST API
    - JWT Authentication
    - MySQL/PostgreSQL Database
    ↓
Database
```

### Project Structure

```
theater-mgnt/
├── frontend/
│   └── client/              # Next.js frontend
│       ├── app/             # App router pages
│       ├── components/      # React components
│       ├── lib/             # Utilities & API calls
│       └── public/          # Static assets
├── backend/
│   └── theatermgnt/         # Spring Boot backend
│       ├── src/main/java/   # Java source code
│       └── pom.xml          # Maven dependencies
└── DEPLOYMENT.md            # This file
```

---

## Prerequisites

### Development Tools

- **Node.js** 18+ (for frontend)
- **Java** 17+ (for backend)
- **Maven** 3.8+ (for backend)
- **Git** (version control)
- **GitHub Account** (for Vercel integration)

### Accounts (Free Tier Available)

- [Vercel](https://vercel.com/) - Frontend hosting
- [Render](https://render.com/) or [Railway](https://railway.app/) - Backend hosting
- [Ngrok](https://ngrok.com/) - (Optional) Local backend testing

---

## Environment Configuration

### 1. Frontend Environment Variables

**File:** `frontend/client/.env.local` (local development)

```env
# Site URL for SEO metadata
NEXT_PUBLIC_SITE_URL=http://localhost:3000

# Backend API URL
NEXT_PUBLIC_API_URL=http://localhost:8080
```

**File:** `frontend/client/.env.example` (template for team)

```env
# Development
NEXT_PUBLIC_SITE_URL=http://localhost:3000
NEXT_PUBLIC_API_URL=http://localhost:8080

# Production (update when deploying)
# NEXT_PUBLIC_SITE_URL=https://your-domain.vercel.app
# NEXT_PUBLIC_API_URL=https://your-backend.render.com
```

### 2. Backend Configuration

**File:** `backend/theatermgnt/src/main/resources/application.properties`

Update CORS allowed origins:

```java
// File: SecurityConfig.java
@Bean
public UrlBasedCorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration corsConfiguration = new CorsConfiguration();

    // Local development
    corsConfiguration.addAllowedOrigin("http://localhost:5173");
    corsConfiguration.addAllowedOrigin("http://localhost:3000");

    // Production - UPDATE THIS
    corsConfiguration.addAllowedOrigin("https://your-frontend.vercel.app");

    corsConfiguration.addAllowedMethod("*");
    corsConfiguration.addAllowedHeader("*");
    corsConfiguration.setAllowCredentials(true);

    // ... rest of config
}
```

**Key Files to Update:**
- `backend/theatermgnt/src/main/java/com/theatermgnt/theatermgnt/configuration/SecurityConfig.java`

---

## Frontend Deployment (Vercel)

### Step 1: Prepare Code

```bash
# Navigate to project root
cd theater-mgnt

# Ensure code is committed
git status
git add .
git commit -m "chore: Prepare for deployment"
git push origin main
```

### Step 2: Create Vercel Account

1. Visit: https://vercel.com/signup
2. Click "**Continue with GitHub**"
3. Authorize Vercel to access your repositories

### Step 3: Import Project

1. Go to: https://vercel.com/dashboard
2. Click "**Add New Project**"
3. Select repository: `theater-mgnt`
4. **IMPORTANT:** Configure project settings:

   ```
   Framework Preset: Next.js
   Root Directory: frontend/client
   Build Command: npm run build
   Output Directory: .next
   Install Command: npm install
   ```

### Step 4: Configure Environment Variables

In Vercel project settings, add:

```
Key: NEXT_PUBLIC_SITE_URL
Value: (Leave empty, will update after first deploy)
Environment: Production

Key: NEXT_PUBLIC_API_URL
Value: (Your backend URL - see Backend Deployment section)
Environment: Production
```

### Step 5: Deploy

1. Click "**Deploy**"
2. Wait 2-3 minutes for build to complete
3. Copy deployment URL: `https://your-project-name.vercel.app`

### Step 6: Update Environment Variables

1. Go to: **Project Settings** > **Environment Variables**
2. Update `NEXT_PUBLIC_SITE_URL`:
   ```
   Value: https://your-project-name.vercel.app
   ```
3. Click "**Redeploy**" from Deployments tab

### Step 7: Verify Deployment

Visit your site and check:

```
✅ Homepage: https://your-project.vercel.app/
✅ Sitemap: https://your-project.vercel.app/sitemap.xml
✅ Robots: https://your-project.vercel.app/robots.txt
✅ Manifest: https://your-project.vercel.app/manifest.webmanifest
```

---

## Backend Deployment Options

### Option A: Deploy to Render.com (Recommended - FREE)

#### Step 1: Create Render Account

1. Visit: https://render.com/
2. Sign up with GitHub

#### Step 2: Create Web Service

1. Click "**New +**" > "**Web Service**"
2. Connect repository: `theater-mgnt`
3. Configure:

   ```
   Name: theater-backend
   Region: Choose closest to your users
   Branch: main
   Root Directory: backend/theatermgnt
   Runtime: Java
   Build Command: ./mvnw clean package -DskipTests
   Start Command: java -jar target/theatermgnt-0.0.1-SNAPSHOT.jar
   Instance Type: Free
   ```

#### Step 3: Add Environment Variables (if needed)

```
DATABASE_URL=your-database-url
SPRING_PROFILES_ACTIVE=prod
```

#### Step 4: Deploy

1. Click "**Create Web Service**"
2. Wait 5-10 minutes for deployment
3. Copy service URL: `https://theater-backend.onrender.com`

#### Step 5: Update Frontend

1. Go to Vercel Dashboard
2. Update `NEXT_PUBLIC_API_URL`:
   ```
   Value: https://theater-backend.onrender.com
   ```
3. Redeploy frontend

#### Step 6: Update Backend CORS

```java
// SecurityConfig.java
corsConfiguration.addAllowedOrigin("https://your-frontend.vercel.app");
```

Commit and push to trigger redeploy.

---

### Option B: Railway.app (Alternative - FREE)

1. Visit: https://railway.app/
2. Sign up with GitHub
3. "**New Project**" > "**Deploy from GitHub repo**"
4. Select `theater-mgnt`
5. Railway auto-detects Spring Boot
6. Set root directory: `backend/theatermgnt`
7. Deploy

Similar to Render, copy Railway URL and update Vercel env vars.

---

### Option C: Local Backend + Ngrok (Testing Only)

**Use Case:** Quick testing without deploying backend

#### Prerequisites

```bash
# Install ngrok
npm install -g ngrok

# Sign up: https://ngrok.com/
# Get authtoken from dashboard
ngrok config add-authtoken YOUR_AUTH_TOKEN
```

#### Daily Workflow

```bash
# Terminal 1: Start Backend
cd backend/theatermgnt
./mvnw spring-boot:run

# Terminal 2: Start Ngrok
ngrok http 8080
```

Copy ngrok URL (e.g., `https://abc123.ngrok-free.app`) and update Vercel:

```
NEXT_PUBLIC_API_URL=https://abc123.ngrok-free.app
```

**⚠️ Important:**
- Ngrok URL **changes** every restart (free plan)
- Must update Vercel env var and redeploy each time
- Only for testing, not production

---

## Post-Deployment Setup

### 1. SEO - Google Search Console

#### Verify Website Ownership

1. Visit: https://search.google.com/search-console
2. Add property: `https://your-domain.vercel.app`
3. Verification method: **HTML tag**
4. Copy meta tag
5. Update `frontend/client/app/layout.tsx`:

   ```tsx
   export const metadata: Metadata = {
     // ... existing metadata
     verification: {
       google: "paste-your-verification-code-here",
     },
   };
   ```

6. Commit, push, wait for Vercel redeploy
7. Click "Verify" in Search Console

#### Submit Sitemap

1. In Search Console > **Sitemaps**
2. Add sitemap URL:
   ```
   https://your-domain.vercel.app/sitemap.xml
   ```
3. Submit

### 2. Analytics (Optional)

#### Google Analytics

1. Create GA4 property: https://analytics.google.com/
2. Copy Measurement ID: `G-XXXXXXXXXX`
3. Add to `frontend/client/app/layout.tsx`:

   ```tsx
   import Script from 'next/script'

   // Inside RootLayout, add to <body>:
   <Script
     src="https://www.googletagmanager.com/gtag/js?id=G-XXXXXXXXXX"
     strategy="afterInteractive"
   />
   <Script id="google-analytics" strategy="afterInteractive">
     {`
       window.dataLayer = window.dataLayer || [];
       function gtag(){dataLayer.push(arguments);}
       gtag('js', new Date());
       gtag('config', 'G-XXXXXXXXXX');
     `}
   </Script>
   ```

### 3. Custom Domain (Optional)

#### Setup in Vercel

1. **Project Settings** > **Domains**
2. Click "**Add Domain**"
3. Enter domain: `your-domain.com`
4. Configure DNS (provided by Vercel):

   ```
   Type: A
   Name: @
   Value: 76.76.21.21

   Type: CNAME
   Name: www
   Value: cname.vercel-dns.com
   ```

5. Update environment variable:
   ```
   NEXT_PUBLIC_SITE_URL=https://your-domain.com
   ```
6. Redeploy

---

## Maintenance & Updates

### Updating Frontend

```bash
cd theater-mgnt

# Make changes to frontend code
cd frontend/client
# ... edit files ...

# Commit and push
git add .
git commit -m "feat: Your feature description"
git push origin main

# Vercel auto-deploys on push to main
# Check deployment: https://vercel.com/dashboard
```

### Updating Backend

#### If deployed to Render/Railway:

```bash
# Make changes to backend code
cd backend/theatermgnt
# ... edit files ...

# Commit and push
git add .
git commit -m "feat: Your backend feature"
git push origin main

# Render/Railway auto-deploys on push
```

#### If using Ngrok (local):

```bash
# Just restart backend after changes
cd backend/theatermgnt
./mvnw spring-boot:run
```

### Important Files to Update When:

#### Changing Domain/URL:

Update these files:
- Frontend: `.env.local` / Vercel env vars
- Backend: `SecurityConfig.java` (CORS)

#### Adding New API Endpoints:

Update:
- Backend: Create controller, service
- Backend: `SecurityConfig.java` (if endpoint needs auth)
- Frontend: `lib/api-*.ts` (API client functions)

#### Database Schema Changes:

Update:
- Backend: Entity classes
- Backend: Migration scripts (if using Flyway/Liquibase)
- May need to rebuild and redeploy

#### Adding Environment Variables:

1. Add to `frontend/client/.env.example` (template)
2. Add to `frontend/client/.env.local` (local dev)
3. Add to Vercel dashboard (production)
4. Redeploy

---

## Troubleshooting

### Frontend Issues

#### Build Failed on Vercel

**Error:** `Module not found` or `Type error`

**Fix:**
```bash
cd frontend/client

# Install dependencies locally
npm install

# Test build locally
npm run build

# If successful, commit package-lock.json
git add package-lock.json
git commit -m "fix: Update dependencies"
git push
```

#### Environment Variables Not Working

**Symptom:** Old values still showing

**Fix:**
1. Update env vars in Vercel dashboard
2. **Must redeploy** - env vars only load at build time
3. Option 1: Deployments > Redeploy
4. Option 2: Push new commit to trigger auto-deploy

#### Images Not Loading

**Symptom:** 404 errors for images

**Fix:**
1. Check images are in `public/` folder
2. Use relative paths: `/image.jpg` not `./image.jpg`
3. For external images, configure `next.config.ts`:
   ```ts
   images: {
     domains: ['your-image-cdn.com'],
   }
   ```

### Backend Issues

#### CORS Errors

**Error in Console:** `Access to fetch has been blocked by CORS policy`

**Fix:**
```java
// backend/theatermgnt/src/.../SecurityConfig.java

@Bean
public UrlBasedCorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration corsConfiguration = new CorsConfiguration();

    // ADD YOUR FRONTEND URL
    corsConfiguration.addAllowedOrigin("https://your-frontend.vercel.app");

    corsConfiguration.addAllowedMethod("*");
    corsConfiguration.addAllowedHeader("*");
    corsConfiguration.setAllowCredentials(true);

    // ... rest
}
```

**Important:** Must restart/redeploy backend after updating CORS.

#### Database Connection Errors

**Error:** `Connection refused` or `Access denied`

**Fix:**
1. Check database is running
2. Verify connection string in `application.properties`
3. For deployed backend, check environment variables
4. Ensure database allows connections from deployed backend IP

#### Backend Not Starting

**Error:** `Port 8080 already in use`

**Fix:**
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Mac/Linux
lsof -ti:8080 | xargs kill -9
```

### API Connection Issues

#### Frontend Can't Reach Backend

**Symptom:** Network errors, timeout

**Checklist:**
1. ✅ Backend is running/deployed
2. ✅ `NEXT_PUBLIC_API_URL` correct in Vercel
3. ✅ CORS configured with frontend URL
4. ✅ Backend URL is HTTPS (if frontend is HTTPS)
5. ✅ No firewall blocking

**Test backend directly:**
```bash
curl https://your-backend.render.com/api/movies
```

If this works but frontend doesn't → Check CORS.

### Ngrok-Specific Issues

#### URL Changed After Restart

**Symptom:** Frontend suddenly can't load data

**Fix:**
1. Check ngrok terminal for new URL
2. Update Vercel env var: `NEXT_PUBLIC_API_URL`
3. Redeploy frontend

**Prevent:** Upgrade ngrok to paid plan for static domain

#### "Visit Site" Warning Every Time

**Symptom:** Ngrok shows warning page before accessing backend

**Fix:**
- This is normal for ngrok free plan
- Click "Visit Site" (one-time per browser)
- Or upgrade ngrok plan
- Or deploy backend permanently

---

## Testing Checklist

### After Frontend Deployment

- [ ] Homepage loads: `https://your-site.vercel.app/`
- [ ] Movies list displays (if backend connected)
- [ ] Movie detail pages work
- [ ] Authentication flow works
- [ ] Booking flow functional
- [ ] No console errors (F12)
- [ ] Mobile responsive
- [ ] Dark/Light mode toggle works

### After Backend Deployment

- [ ] API accessible: `https://your-backend.com/api/movies`
- [ ] No CORS errors
- [ ] Authentication endpoints work
- [ ] Database connected
- [ ] CRUD operations work
- [ ] Response times acceptable

### SEO Verification

- [ ] Sitemap accessible: `/sitemap.xml`
- [ ] Robots.txt accessible: `/robots.txt`
- [ ] Meta tags present (view page source)
- [ ] Open Graph preview (https://metatags.io/)
- [ ] Structured data valid (https://search.google.com/test/rich-results)
- [ ] Lighthouse SEO score > 90

---

## Architecture Reference

### Frontend Tech Stack

- **Framework:** Next.js 16 (App Router)
- **React:** 19
- **Styling:** Tailwind CSS
- **UI Components:** shadcn/ui
- **State:** Zustand
- **HTTP Client:** Fetch API
- **SEO:** Built-in metadata API, JSON-LD structured data

### Backend Tech Stack

- **Framework:** Spring Boot 3.x
- **Language:** Java 17+
- **Security:** Spring Security + JWT
- **Database:** JPA/Hibernate
- **Build Tool:** Maven

### Key Features Implemented

#### Frontend:
- ✅ Server-side rendering (SSR)
- ✅ SEO optimization (metadata, sitemap, structured data)
- ✅ PWA manifest
- ✅ Protected routes
- ✅ Authentication flow
- ✅ Responsive design
- ✅ Dark/Light mode

#### Backend:
- ✅ REST API
- ✅ JWT authentication
- ✅ CORS configuration
- ✅ Movie/Genre/Showtime management
- ✅ User management
- ✅ Booking system

---

## Quick Reference

### Common Commands

```bash
# Frontend
cd frontend/client
npm install              # Install dependencies
npm run dev             # Development server (localhost:3000)
npm run build           # Production build
npm run start           # Production server

# Backend
cd backend/theatermgnt
./mvnw clean install    # Build
./mvnw spring-boot:run  # Run development server
./mvnw package          # Create JAR file
java -jar target/theatermgnt-0.0.1-SNAPSHOT.jar  # Run JAR

# Git
git status              # Check changes
git add .               # Stage all changes
git commit -m "message" # Commit
git push origin main    # Push to GitHub
```

### Important URLs

**Local Development:**
- Frontend: http://localhost:3000
- Backend: http://localhost:8080
- Ngrok web UI: http://127.0.0.1:4040

**Production:**
- Frontend: https://your-project.vercel.app/
- Backend: https://your-backend.render.com
- Vercel Dashboard: https://vercel.com/dashboard
- Render Dashboard: https://dashboard.render.com/

### File Locations

**Frontend Config:**
```
frontend/client/
├── .env.local                    # Local environment variables
├── .env.example                  # Template for team
├── next.config.ts                # Next.js configuration
├── app/layout.tsx                # Root layout + metadata
├── app/sitemap.ts                # Dynamic sitemap
└── app/robots.ts                 # Robots.txt
```

**Backend Config:**
```
backend/theatermgnt/src/main/
├── resources/
│   └── application.properties    # Spring config
└── java/.../configuration/
    └── SecurityConfig.java       # CORS + Security
```

---

## Support & Resources

### Documentation

- **Next.js:** https://nextjs.org/docs
- **Vercel:** https://vercel.com/docs
- **Spring Boot:** https://spring.io/projects/spring-boot
- **Render:** https://render.com/docs

### Useful Tools

- **Lighthouse:** Chrome DevTools (Performance/SEO audit)
- **Google Search Console:** https://search.google.com/search-console
- **PageSpeed Insights:** https://pagespeed.web.dev/
- **Rich Results Test:** https://search.google.com/test/rich-results
- **Meta Tags Preview:** https://metatags.io/

---

## Version History

- **v1.0.0** (2025-11-29)
  - Initial deployment setup
  - SEO optimization implemented
  - Frontend deployed to Vercel
  - Backend CORS configured for production
  - Environment variable management
  - Documentation completed

---

## Contributing

When making changes:

1. Create feature branch: `git checkout -b feature/your-feature`
2. Make changes and test locally
3. Update `.env.example` if adding new env vars
4. Commit with clear messages
5. Push and create Pull Request
6. After merge to main, auto-deploy triggers

---

**Last Updated:** 2025-11-29
**Maintained By:** Theater Management Team
**Status:** ✅ Production Ready

**Questions?** Check Troubleshooting section or create an issue in the repository.

Good luck with your deployment! 🚀
