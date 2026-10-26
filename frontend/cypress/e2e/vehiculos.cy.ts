/// <reference types="cypress" />

describe('Vehículos Management', () => {
  beforeEach(() => {
    cy.loginAsAdmin()
    cy.visit('/vehiculos')
  })

  it('should display vehicles list page', () => {
    cy.get('[data-testid="btn-nuevo-vehiculo"]').should('be.visible')
    cy.get('[data-testid="vehiculos-table"]').should('be.visible')
    cy.get('[data-testid="metrics-container"]').should('be.visible')
  })

  it('should show metrics with correct counts', () => {
    cy.get('[data-testid="total-vehiculos"]').should('be.visible')
    cy.get('[data-testid="total-carros"]').should('be.visible')
    cy.get('[data-testid="total-motos"]').should('be.visible')
  })

  it('should navigate to create new vehicle form', () => {
    cy.get('[data-testid="btn-nuevo-vehiculo"]').click()
    cy.url().should('include', '/vehiculos/nuevo')
    cy.get('[data-testid="vehiculo-form"]').should('be.visible')
  })

  it('should create a new vehicle', () => {
    cy.get('[data-testid="btn-nuevo-vehiculo"]').click()
    
    cy.get('[data-testid="input-placa"]').type('TEST123')
    cy.get('[data-testid="select-tipo"]').select('CARRO')
    cy.get('[data-testid="input-color"]').type('Azul')
    cy.get('[data-testid="input-modelo"]').type('Test Model')
    
    cy.get('[data-testid="btn-guardar"]').click()
    
    cy.url().should('include', '/vehiculos')
    cy.get('[data-testid="vehiculo-row-"]').should('exist')
    cy.contains('TEST123').should('be.visible')
  })

  it('should show validation errors for required fields', () => {
    cy.get('[data-testid="btn-nuevo-vehiculo"]').click()
    cy.get('[data-testid="btn-guardar"]').click()
    
    cy.get('[data-testid="error-placa"]').should('be.visible')
    cy.get('[data-testid="error-tipo"]').should('be.visible')
  })

  it('should edit an existing vehicle', () => {
    // First create a vehicle to edit
    cy.get('[data-testid="btn-nuevo-vehiculo"]').click()
    cy.get('[data-testid="input-placa"]').type('EDIT123')
    cy.get('[data-testid="select-tipo"]').select('MOTO')
    cy.get('[data-testid="input-color"]').type('Rojo')
    cy.get('[data-testid="input-modelo"]').type('Edit Model')
    cy.get('[data-testid="btn-guardar"]').click()
    
    cy.url().should('include', '/vehiculos')
    
    // Find the created vehicle and click edit
    cy.contains('EDIT123').parent().parent().within(() => {
      cy.get('[data-testid^="btn-editar-"]').click()
    })
    
    cy.url().should('include', '/vehiculos/editar/')
    cy.get('[data-testid="input-placa"]').should('have.value', 'EDIT123')
    
    // Update the vehicle
    cy.get('[data-testid="input-color"]').clear().type('Verde')
    cy.get('[data-testid="btn-guardar"]').click()
    
    cy.url().should('include', '/vehiculos')
    cy.contains('Verde').should('be.visible')
  })

  it('should delete a vehicle', () => {
    // First create a vehicle to delete
    cy.get('[data-testid="btn-nuevo-vehiculo"]').click()
    cy.get('[data-testid="input-placa"]').type('DELETE123')
    cy.get('[data-testid="select-tipo"]').select('CAMIONETA')
    cy.get('[data-testid="input-color"]').type('Negro')
    cy.get('[data-testid="input-modelo"]').type('Delete Model')
    cy.get('[data-testid="btn-guardar"]').click()
    
    cy.url().should('include', '/vehiculos')
    
    // Find the created vehicle and click delete
    cy.contains('DELETE123').parent().parent().within(() => {
      cy.get('[data-testid^="btn-eliminar-"]').click()
    })
    
    cy.on('window:confirm', () => true)
    
    cy.contains('DELETE123').should('not.exist')
  })
})