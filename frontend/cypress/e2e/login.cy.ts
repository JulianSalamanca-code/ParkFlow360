/// <reference types="cypress" />

describe('Login', () => {
  beforeEach(() => {
    cy.visit('/login')
  })

  it('should display login page with all elements', () => {
    cy.get('[data-testid="email-input"]').should('be.visible')
    cy.get('[data-testid="password-input"]').should('be.visible')
    cy.get('[data-testid="login-button"]').should('be.visible')
    cy.contains('Apertura de Turno').should('be.visible')
  })

  it('should show validation errors for empty fields', () => {
    cy.get('[data-testid="login-button"]').click()
    cy.get('[data-testid="email-input"]').should('have.class', 'ng-invalid')
    cy.get('[data-testid="password-input"]').should('have.class', 'ng-invalid')
  })

  it('should show error for invalid email format', () => {
    cy.get('[data-testid="email-input"]').type('invalid-email')
    cy.get('[data-testid="password-input"]').type('password123')
    cy.get('[data-testid="login-button"]').click()
    cy.get('[data-testid="email-input"]').should('have.class', 'ng-invalid')
  })

  it('should login successfully as admin', () => {
    cy.loginAsAdmin()
    cy.shouldBeLoggedIn()
    cy.shouldBeOnPage('/vehiculos')
  })

  it('should show error for invalid credentials', () => {
    cy.get('[data-testid="email-input"]').type('wrong@email.com')
    cy.get('[data-testid="password-input"]').type('wrongpassword')
    cy.get('[data-testid="login-button"]').click()
    cy.get('[data-testid="error-message"]').should('be.visible')
    cy.get('[data-testid="error-message"]').should('contain', 'Error al iniciar sesión')
  })
})