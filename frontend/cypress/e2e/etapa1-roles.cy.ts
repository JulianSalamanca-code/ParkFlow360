/// <reference types="cypress" />

describe('Etapa 1: portales por rol con API mock', () => {
  it('EMPRESA llega a su inicio, navega y ve sidebar y barra inferior', () => {
    cy.loginAsRole('EMPRESA', 'Empresa de prueba')
    cy.visit('/')
    cy.url().should('include', '/empresa')
    cy.getByTestId('empresa-metricas').should('be.visible')
    cy.getByTestId('nav-item-empresa-reportes').should('exist')

    cy.viewport(390, 844)
    cy.getByTestId('bottom-nav').should('be.visible')
    cy.getByTestId('bottom-nav-item-empresa-parqueaderos').click()
    cy.getByTestId('empresa-parqueaderos').should('be.visible')
    cy.getByTestId('nav-hamburger').click()
    cy.getByTestId('nav-item-empresa-configuracion').should('be.visible')
  })

  it('CAJERO_PAGOS confirma un cobro y lo encuentra en el historial', () => {
    cy.loginAsRole('CAJERO_PAGOS', 'Cajero de prueba')
    cy.visit('/')
    cy.url().should('include', '/cajero/pagos')
    cy.getByTestId('cajero-pagos').should('contain', 'FAC-2026-0301')
    cy.getByTestId('cajero-confirmar-301').click()
    cy.getByTestId('cajero-success').should('contain', 'confirmado')
    cy.getByTestId('nav-item-cajero-historial').should('exist')
    cy.getByTestId('nav-item-cajero-historial').click()
    cy.getByTestId('cajero-historial').should('contain', 'FAC-2026-0301')
    cy.viewport(390, 844)
    cy.getByTestId('bottom-nav').should('be.visible')
    cy.getByTestId('bottom-nav-item-cajero-pagos').should('be.visible')
  })

  it('GUARDIA valida una entrada y registra la salida', () => {
    cy.loginAsRole('GUARDIA', 'Guardia de prueba')
    cy.visit('/')
    cy.url().should('include', '/guardia/ingresos')
    cy.getByTestId('guardia-ingresos').should('contain', 'KLM-482')
    cy.getByTestId('guardia-accion-501').click()
    cy.getByTestId('guardia-success').should('contain', 'Entrada')
    cy.getByTestId('guardia-accion-501').should('contain', 'Registrar salida').click()
    cy.getByTestId('guardia-success').should('contain', 'Salida')
    cy.getByTestId('nav-item-guardia-ocupacion').should('exist')
    cy.getByTestId('nav-item-guardia-ocupacion').click()
    cy.getByTestId('guardia-ocupacion').should('be.visible')
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

  it('muestra estados vacíos y de error controlados por Cypress', () => {
    cy.loginAsRole('EMPRESA')
    cy.setMockApiState('empty')
    cy.visit('/empresa/parqueaderos')
    cy.getByTestId('empresa-empty').should('be.visible')

    cy.loginAsRole('GUARDIA')
    cy.setMockApiState('error')
    cy.visit('/guardia/ocupacion')
    cy.getByTestId('guardia-error').should('be.visible')
    cy.getByTestId('guardia-error').should('contain', 'No se pudo cargar')
  })
})
