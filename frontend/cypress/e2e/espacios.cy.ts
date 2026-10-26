/// <reference types="cypress" />

describe('Espacios Management', () => {
  beforeEach(() => {
    cy.loginAsAdmin()
    cy.visit('/espacios')
  })

  it('should display espacios list page', () => {
    cy.get('[data-testid="btn-nuevo-espacio"]').should('be.visible')
    cy.get('[data-testid="espacios-table"]').should('be.visible')
    cy.get('[data-testid="metrics-container"]').should('be.visible')
  })

  it('should show metrics with correct counts', () => {
    cy.get('[data-testid="total-espacios"]').should('be.visible')
    cy.get('[data-testid="total-libres"]').should('be.visible')
    cy.get('[data-testid="total-ocupados"]').should('be.visible')
    cy.get('[data-testid="total-reservados"]').should('be.visible')
  })

  it('should navigate to create new espacio form', () => {
    cy.get('[data-testid="btn-nuevo-espacio"]').click()
    cy.url().should('include', '/espacios/nuevo')
    cy.get('[data-testid="espacio-form"]').should('be.visible')
  })

  it('should create a new espacio', () => {
    cy.get('[data-testid="btn-nuevo-espacio"]').click()
    
    cy.get('[data-testid="input-numero"]').type('TEST01')
    cy.get('[data-testid="select-tipo"]').select('CARRO')
    cy.get('[data-testid="select-estado"]').select('LIBRE')
    cy.get('[data-testid="input-piso"]').type('1')
    
    cy.get('[data-testid="btn-guardar"]').click()
    
    cy.url().should('include', '/espacios')
    cy.contains('TEST01').should('be.visible')
  })

  it('should show validation errors for required fields', () => {
    cy.get('[data-testid="btn-nuevo-espacio"]').click()
    cy.get('[data-testid="btn-guardar"]').click()
    
    cy.get('[data-testid="error-numero"]').should('be.visible')
    cy.get('[data-testid="error-tipo"]').should('be.visible')
    cy.get('[data-testid="error-estado"]').should('be.visible')
    cy.get('[data-testid="error-piso"]').should('be.visible')
  })

  it('should edit an existing espacio', () => {
    // First create an espacio to edit
    cy.get('[data-testid="btn-nuevo-espacio"]').click()
    cy.get('[data-testid="input-numero"]').type('EDIT01')
    cy.get('[data-testid="select-tipo"]').select('MOTO')
    cy.get('[data-testid="select-estado"]').select('LIBRE')
    cy.get('[data-testid="input-piso"]').type('2')
    cy.get('[data-testid="btn-guardar"]').click()
    
    cy.url().should('include', '/espacios')
    
    // Find the created espacio and click edit
    cy.contains('EDIT01').parent().parent().within(() => {
      cy.get('[data-testid^="btn-editar-"]').click()
    })
    
    cy.url().should('include', '/espacios/editar/')
    cy.get('[data-testid="input-numero"]').should('have.value', 'EDIT01')
    
    // Update the espacio
    cy.get('[data-testid="select-estado"]').select('OCUPADO')
    cy.get('[data-testid="btn-guardar"]').click()
    
    cy.url().should('include', '/espacios')
    cy.contains('OCUPADO').should('be.visible')
  })

  it('should delete an espacio', () => {
    // First create an espacio to delete
    cy.get('[data-testid="btn-nuevo-espacio"]').click()
    cy.get('[data-testid="input-numero"]').type('DELETE01')
    cy.get('[data-testid="select-tipo"]').select('CAMIONETA')
    cy.get('[data-testid="select-estado"]').select('LIBRE')
    cy.get('[data-testid="input-piso"]').type('1')
    cy.get('[data-testid="btn-guardar"]').click()
    
    cy.url().should('include', '/espacios')
    
    // Find the created espacio and click delete
    cy.contains('DELETE01').parent().parent().within(() => {
      cy.get('[data-testid^="btn-eliminar-"]').click()
    })
    
    cy.on('window:confirm', () => true)
    
    cy.contains('DELETE01').should('not.exist')
  })

  it('should liberar an occupied espacio', () => {
    // First create an espacio and set it to OCUPADO
    cy.get('[data-testid="btn-nuevo-espacio"]').click()
    cy.get('[data-testid="input-numero"]').type('LIBERAR01')
    cy.get('[data-testid="select-tipo"]').select('CARRO')
    cy.get('[data-testid="select-estado"]').select('OCUPADO')
    cy.get('[data-testid="input-piso"]').type('1')
    cy.get('[data-testid="btn-guardar"]').click()
    
    cy.url().should('include', '/espacios')
    
    // Find the created espacio and click liberar
    cy.contains('LIBERAR01').parent().parent().within(() => {
      cy.get('[data-testid^="btn-liberar-"]').click()
    })
    
    cy.on('window:confirm', () => true)
    
    // The espacio should now be LIBRE
    cy.contains('LIBRE').should('be.visible')
  })
})