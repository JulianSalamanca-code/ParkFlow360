/// <reference types="cypress" />

describe('Pagos Management', () => {
  beforeEach(() => {
    cy.loginAsAdmin()
    cy.visit('/pagos')
  })

  it('should display pagos list page', () => {
    cy.get('[data-testid="btn-nuevo-pago"]').should('be.visible')
    cy.get('[data-testid="pagos-table"]').should('be.visible')
    cy.get('[data-testid="metrics-container"]').should('be.visible')
  })

  it('should show metrics with correct counts', () => {
    cy.get('[data-testid="total-pagos"]').should('be.visible')
    cy.get('[data-testid="recaudo-total"]').should('be.visible')
  })

  it('should navigate to create new pago form', () => {
    cy.get('[data-testid="btn-nuevo-pago"]').click()
    cy.url().should('include', '/pagos/nuevo')
    cy.get('[data-testid="pago-form"]').should('be.visible')
  })

  it('should create a new pago', () => {
    cy.get('[data-testid="btn-nuevo-pago"]').click()
    
    cy.get('[data-testid="input-vehiculo-id"]').type('1')
    cy.get('[data-testid="input-espacio-id"]').type('1')
    cy.get('[data-testid="input-tarifa-id"]').type('1')
    cy.get('[data-testid="input-valor"]').type('5000')
    cy.get('[data-testid="select-metodo-pago"]').select('EFECTIVO')
    
    cy.get('[data-testid="btn-guardar"]').click()
    
    cy.url().should('include', '/pagos')
    cy.get('[data-testid="pago-row-"]').should('exist')
  })

  it('should show validation errors for required fields', () => {
    cy.get('[data-testid="btn-nuevo-pago"]').click()
    cy.get('[data-testid="btn-guardar"]').click()
    
    cy.get('[data-testid="error-vehiculo-id"]').should('be.visible')
    cy.get('[data-testid="error-espacio-id"]').should('be.visible')
    cy.get('[data-testid="error-tarifa-id"]').should('be.visible')
    cy.get('[data-testid="error-valor"]').should('be.visible')
  })

  it('should edit an existing pago', () => {
    // First create a pago to edit
    cy.get('[data-testid="btn-nuevo-pago"]').click()
    cy.get('[data-testid="input-vehiculo-id"]').type('1')
    cy.get('[data-testid="input-espacio-id"]').type('1')
    cy.get('[data-testid="input-tarifa-id"]').type('1')
    cy.get('[data-testid="input-valor"]').type('3000')
    cy.get('[data-testid="select-metodo-pago"]').select('TARJETA')
    cy.get('[data-testid="btn-guardar"]').click()
    
    cy.url().should('include', '/pagos')
    
    // Find the created pago and click edit
    cy.contains('3000').parent().parent().within(() => {
      cy.get('[data-testid^="btn-editar-"]').click()
    })
    
    cy.url().should('include', '/pagos/editar/')
    cy.get('[data-testid="input-valor"]').should('have.value', '3000')
    
    // Update the pago
    cy.get('[data-testid="input-valor"]').clear().type('4000')
    cy.get('[data-testid="select-metodo-pago"]').select('TRANSFERENCIA')
    cy.get('[data-testid="btn-guardar"]').click()
    
    cy.url().should('include', '/pagos')
    cy.contains('4000').should('be.visible')
  })

  it('should delete a pago', () => {
    // First create a pago to delete
    cy.get('[data-testid="btn-nuevo-pago"]').click()
    cy.get('[data-testid="input-vehiculo-id"]').type('2')
    cy.get('[data-testid="input-espacio-id"]').type('2')
    cy.get('[data-testid="input-tarifa-id"]').type('2')
    cy.get('[data-testid="input-valor"]').type('1000')
    cy.get('[data-testid="select-metodo-pago"]').select('QR')
    cy.get('[data-testid="btn-guardar"]').click()
    
    cy.url().should('include', '/pagos')
    
    // Find the created pago and click delete
    cy.contains('1000').parent().parent().within(() => {
      cy.get('[data-testid^="btn-eliminar-"]').click()
    })
    
    cy.on('window:confirm', () => true)
    
    cy.contains('1000').should('not.exist')
  })
})