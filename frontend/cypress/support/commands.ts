/// <reference types="cypress" />

declare global {
  namespace Cypress {
    interface Chainable {
      login(email: string, password: string): Chainable<void>
      loginAsAdmin(): Chainable<void>
      loginAsUser(): Chainable<void>
      getByTestId(dataTestId: string): Chainable<JQuery<HTMLElement>>
      shouldBeLoggedIn(): Chainable<void>
      shouldBeOnPage(path: string): Chainable<void>
    }
  }
}

Cypress.Commands.add('login', (email: string, password: string) => {
  cy.session([email, password], () => {
    cy.visit('/login')
    cy.get('[data-testid="email-input"]').clear().type(email)
    cy.get('[data-testid="password-input"]').clear().type(password)
    cy.get('[data-testid="login-button"]').click()
    cy.url().should('not.include', '/login')
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