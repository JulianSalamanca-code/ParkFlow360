/// <reference types="cypress" />

export {}

declare global {
  namespace Cypress {
    interface Chainable {
      login(email: string, password: string): Chainable<void>
      loginAsAdmin(): Chainable<void>
      loginAsUser(): Chainable<void>
      loginAsRole(rol: string, nombre?: string): Chainable<void>
      getByTestId(dataTestId: string): Chainable<JQuery<HTMLElement>>
      shouldBeLoggedIn(): Chainable<void>
      shouldBeOnPage(path: string): Chainable<void>
    }
  }
}

Cypress.Commands.add('login', (email: string, password: string) => {
  cy.session([email, password], () => {
    cy.visit('/auth/login')
    cy.get('[data-testid="email-input"]').clear().type(email)
    cy.get('[data-testid="password-input"]').clear().type(password)
    cy.get('[data-testid="login-button"]').click()
    cy.url().should('not.include', '/auth/login')
    cy.window().then((win) => {
      expect(win.localStorage.getItem('token')).to.exist
    })
  })
})

Cypress.Commands.add('loginAsAdmin', () => {
  cy.login('admin@parkflow360.com', 'admin123')
})

Cypress.Commands.add('loginAsUser', () => {
  cy.login('usuario@parkflow360.com', 'usuario123')
})

/**
 * Siembra una sesión local para probar la navegación por rol sin backend.
 * Solo valida interfaz: no reemplaza una prueba de autorización real.
 */
Cypress.Commands.add('loginAsRole', (rol: string, nombre = 'Usuario de prueba') => {
  cy.visit('/auth/login', {
    onBeforeLoad(win) {
      const user = {
        token: 'e2e-ui-token',
        tipo: 'Bearer',
        email: 'e2e@parkflow360.com',
        rol,
        nombre
      }
      win.localStorage.setItem('token', user.token)
      win.localStorage.setItem('user', JSON.stringify(user))
    }
  })
})

Cypress.Commands.add('getByTestId', (dataTestId: string) => {
  return cy.get(`[data-testid="${dataTestId}"]`)
})

Cypress.Commands.add('shouldBeLoggedIn', () => {
  cy.window().then((win) => {
    expect(win.localStorage.getItem('token')).to.exist
  })
})

Cypress.Commands.add('shouldBeOnPage', (path: string) => {
  cy.url().should('include', path)
})
