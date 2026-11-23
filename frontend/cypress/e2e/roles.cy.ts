/// <reference types="cypress" />

describe('Roles canónicos y alias', () => {
  it('trata un token ADMIN como PLATAFORMA', () => {
    cy.loginAsRole('ADMIN', 'Admin legado')
    cy.visit('/')

    cy.url().should('include', '/planos')
    cy.get('.role').should('contain', 'Plataforma')
  })

  it('redirige a un USUARIO fuera de las rutas de plataforma', () => {
    cy.loginAsRole('USUARIO', 'Cliente')
    cy.visit('/planos')

    cy.url().should('include', '/mis-parqueos')
  })

  it('envía a EMPRESA a su nuevo inicio', () => {
    cy.loginAsRole('EMPRESA', 'Empresa de prueba')
    cy.visit('/')

    cy.url().should('include', '/empresa')
    cy.getByTestId('empresa-metricas').should('be.visible')
  })

  it('envía al login cuando el rol almacenado no se reconoce', () => {
    cy.loginAsRole('ROL_INEXISTENTE')
    cy.visit('/planos')

    cy.url().should('include', '/auth/login')
  })
})
