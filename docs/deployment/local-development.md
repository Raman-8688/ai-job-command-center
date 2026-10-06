# Local Development Setup Guide

## 1. Prerequisites
- **JDK 21+** (Eclipse Temurin or OpenJDK recommended)
- **Node.js 20 LTS** & **npm 10+**
- **Docker Desktop / Docker Engine** (for local PostgreSQL)
- **Maven 3.9+** (or included Maven wrapper `./mvnw`)

## 2. Quickstart Steps
1. **Clone & Prepare Environment:**
   ```bash
   cp .env.example .env
   ```
2. **Start PostgreSQL Container:**
   ```bash
   docker-compose up -d postgres
   ```
3. **Run Backend (Starting in Phase 1):**
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```
4. **Run Frontend (Starting in Phase 6):**
   ```bash
   cd frontend
   npm install
   npm start
   ```
5. **Access Application:**
   - Frontend: `http://localhost:4200`
   - Backend API: `http://localhost:8080/api`
   - Actuator Health: `http://localhost:8080/actuator/health`
