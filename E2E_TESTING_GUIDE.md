# ParkFlow360 - End-to-End Testing Guide

## Overview

This project includes comprehensive end-to-end (E2E) tests using **Cypress** for the frontend and **JUnit + Testcontainers** for the backend integration tests.

## Frontend E2E Tests (Cypress)

### Prerequisites

- Node.js 18+
- Angular CLI
- Backend running on port 8080
- Frontend running on port 4200

### Installation

```bash
cd frontend
npm install
```

### Running Tests

#### Headless Mode (CI/CD)
```bash
npm run e2e
```

#### Interactive Mode (Development)
```bash
npm run e2e:open
```

#### Headed Mode (Watch tests run in browser)
```bash
npm run e2e:headed
```

### Test Structure

```
frontend/cypress/
├── config.ts              # Cypress configuration
├── support/
│   ├── commands.ts        # Custom Cypress commands
│   └── e2e.ts            # Global test setup
├── fixtures/              # Test data fixtures
├── e2e/                   # Test specifications
│   ├── login.cy.ts        # Login tests
│   ├── vehiculos.cy.ts    # Vehicle management tests
│   ├── espacios.cy.ts     # Parking spaces tests
│   ├── ingresos.cy.ts     # Vehicle entries/exits tests
│   └── pagos.cy.ts        # Payments tests
```

### Custom Commands

- `cy.login(email, password)` - Login with credentials
- `cy.loginAsAdmin()` - Login as admin user
- `cy.loginAsUser()` - Login as regular user
- `cy.getByTestId(id)` - Get element by data-testid
- `cy.shouldBeLoggedIn()` - Assert user is logged in
- `cy.shouldBeOnPage(path)` - Assert current page

### Test Data

Tests use the following seeded data:
- **Admin**: admin@parkflow360.com / admin123
- **User**: usuario@parkflow360.com / usuario123

### Adding data-testid Attributes

All interactive elements should have `data-testid` attributes for reliable test selection:

```html
<button data-testid="btn-guardar">Guardar</button>
<input data-testid="input-placa" />
<select data-testid="select-tipo">...</select>
```

## Backend Integration Tests (JUnit + Testcontainers)

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker (for Testcontainers)

### Running Tests

#### Unit Tests Only
```bash
cd backend
mvn test
```

#### Integration Tests Only
```bash
mvn verify -DskipUnitTests=true
```

#### All Tests
```bash
mvn verify
```

### Test Structure

```
backend/src/test/
├── java/com/parkflow360/
│   ├── ParkFlow360ApplicationIntegrationTest.java  # Spring context + Testcontainers
│   ├── security/service/AuthServiceTest.java       # Auth service unit tests
│   ├── modulos/vehiculos/service/VehiculoServiceTest.java
│   ├── modulos/espacios/service/EspacioServiceTest.java
│   └── modulos/ingresos/service/IngresoServiceTest.java
└── resources/
    └── application-test.yml                        # Test configuration
```

### Testcontainers Configuration

The integration tests use Testcontainers to spin up a real PostgreSQL database:

```java
@Container
static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("parkflow360")
        .withUsername("parkflow360")
        .withPassword("parkflow360");
```

### Test Profiles

Tests run with the `test` profile using `application-test.yml` which configures:
- Testcontainers PostgreSQL
- Hibernate create-drop DDL
- Disabled Flyway
- Test JWT secret

## CI/CD Pipeline

### GitHub Actions Example

```yaml
name: E2E Tests

on: [push, pull_request]

jobs:
  frontend-e2e:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_DB: parkflow360
          POSTGRES_USER: parkflow360
          POSTGRES_PASSWORD: parkflow360
        ports: ["5432:5432"]
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
    
    steps:
      - uses: actions/checkout@v4
      
      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: frontend/package-lock.json
      
      - name: Install frontend dependencies
        run: npm ci
        working-directory: frontend
      
      - name: Build frontend
        run: npm run build
        working-directory: frontend
      
      - name: Start backend
        run: mvn spring-boot:run -Dspring-boot.run.profiles=test &
        working-directory: backend
      
      - name: Wait for backend
        run: sleep 30
      
      - name: Start frontend
        run: npm run start &
        working-directory: frontend
      
      - name: Wait for frontend
        run: sleep 20
      
      - name: Run Cypress E2E tests
        run: npm run e2e
        working-directory: frontend

  backend-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Setup Java
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
          cache: maven
      
      - name: Run backend tests
        run: mvn verify
        working-directory: backend
```

## Best Practices

1. **Use data-testid attributes** - Never rely on CSS classes or text content for test selectors
2. **Isolate tests** - Each test should be independent and not depend on other tests
3. **Use cy.session()** - Cache login sessions to speed up tests
4. **Clean up test data** - Delete created entities in afterEach or after hooks
5. **Test user flows** - Focus on critical user journeys, not implementation details
6. **Handle async operations** - Use proper Cypress commands for waiting

## Troubleshooting

### Cypress Tests Fail

1. Ensure backend is running on port 8080
2. Ensure frontend is running on port 4200
3. Check that test users exist in database
4. Verify CORS configuration allows localhost:4200

### Backend Tests Fail

1. Ensure Docker is running (for Testcontainers)
2. Check that ports 5432 is available
3. Verify test dependencies are downloaded: `mvn dependency:resolve`

## Adding New Tests

### Frontend
1. Add `data-testid` attributes to new components
2. Create new `.cy.ts` file in `cypress/e2e/`
3. Follow existing test patterns
4. Add custom commands if needed

### Backend
1. Create new test class in appropriate package
2. Use `@ExtendWith(MockitoExtension.class)` for unit tests
3. Use `@Testcontainers` + `@SpringBootTest` for integration tests
4. Follow existing test patterns