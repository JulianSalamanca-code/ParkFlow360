/// <reference types="cypress" />

describe('Etapa 1: portales por rol', () => {
  it('EMPRESA llega a su inicio y mantiene su navegación', () => {
    cy.loginAsRole('EMPRESA', 'Empresa de prueba')
    cy.visit('/')
    cy.url().should('include', '/empresa')
    cy.getByTestId('nav-item-empresa-reportes').should('exist')

    cy.viewport(390, 844)
    cy.getByTestId('bottom-nav').should('be.visible')
    cy.getByTestId('bottom-nav-item-empresa-parqueaderos').click()
    cy.url().should('include', '/empresa/parqueaderos')
    cy.getByTestId('nav-hamburger').click()
    cy.getByTestId('nav-item-empresa-configuracion').should('be.visible')
  })

  it('CAJERO_PAGOS mantiene su navegación', () => {
    cy.loginAsRole('CAJERO_PAGOS', 'Cajero de prueba')
    cy.visit('/')
    cy.url().should('include', '/cajero/pagos')
    cy.getByTestId('nav-item-cajero-historial').should('exist')
    cy.getByTestId('nav-item-cajero-historial').click()
    cy.url().should('include', '/cajero/historial')
    cy.viewport(390, 844)
    cy.getByTestId('bottom-nav').should('be.visible')
    cy.getByTestId('bottom-nav-item-cajero-pagos').should('be.visible')
  })

  it('GUARDIA mantiene su navegación', () => {
    cy.loginAsRole('GUARDIA', 'Guardia de prueba')
    cy.visit('/')
    cy.url().should('include', '/guardia/ingresos')
    cy.getByTestId('nav-item-guardia-ocupacion').should('exist')
    cy.getByTestId('nav-item-guardia-ocupacion').click()
    cy.url().should('include', '/guardia/ocupacion')
    cy.viewport(390, 844)
    cy.getByTestId('bottom-nav').should('be.visible')
    cy.getByTestId('bottom-nav-item-guardia-ingresos').should('be.visible')
  })

  it('redirige a cada inicio propio al intentar abrir una ruta ajena', () => {
    cy.loginAsRole('EMPRESA')
    cy.visit('/cajero/pagos')
    cy.url().should('include', '/empresa')

    cy.loginAsRole('CAJERO_PAGOS')
    cy.visit('/guardia/ingresos')
    cy.url().should('include', '/cajero/pagos')
    cy.getByTestId('cajero-pagos').should('be.visible')

    cy.loginAsRole('GUARDIA')
    cy.visit('/empresa')
    cy.url().should('include', '/guardia/ingresos')
  })

})
