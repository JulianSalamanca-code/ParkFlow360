/// <reference types="cypress" />

describe('Registro de Usuarios', () => {
  beforeEach(() => {
    cy.visit('/auth/login')
  })

  it('should display registration form', () => {
    cy.get('[data-testid="btn-registrarse"]').should('be.visible')
    cy.get('[data-testid="btn-registrarse"]').click()
    cy.url().should('include', '/auth/registro')
    cy.get('[data-testid="registro-form"]').should('be.visible')
  })

  it('should register a new user with valid data', () => {
    cy.get('[data-testid="btn-registrarse"]').click()
    
    cy.get('[data-testid="input-email"]').type('nuevo@usuario.com')
    cy.get('[data-testid="input-nombre"]').type('Nuevo Usuario')
    cy.get('[data-testid="input-password"]').type('password123')
    cy.get('[data-testid="input-confirm-password"]').type('password123')
    
    cy.get('[data-testid="btn-registrar"]').click()
    
    cy.url().should('include', '/mis-parqueos')
    cy.contains('¡Bienvenido, Nuevo Usuario!').should('be.visible')
  })

  it('should show validation errors for invalid data', () => {
    cy.get('[data-testid="btn-registrarse"]').click()
    cy.get('[data-testid="btn-registrar"]').click()
    
    cy.get('[data-testid="error-email"]').should('be.visible')
    cy.get('[data-testid="error-nombre"]').should('be.visible')
    cy.get('[data-testid="error-password"]').should('be.visible')
  })

  it('should not allow duplicate email registration', () => {
    cy.get('[data-testid="btn-registrarse"]').click()
    
    cy.get('[data-testid="input-email"]').type('admin@parkflow360.com')
    cy.get('[data-testid="input-nombre"]').type('Admin Test')
    cy.get('[data-testid="input-password"]').type('password123')
    cy.get('[data-testid="input-confirm-password"]').type('password123')
    
    cy.get('[data-testid="btn-registrar"]').click()
    
    cy.contains('El email ya está registrado').should('be.visible')
  })
})
