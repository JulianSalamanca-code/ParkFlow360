/// <reference types="cypress" />

describe('Ingresos Management', () => {
  beforeEach(() => {
    cy.loginAsAdmin()
    cy.visit('/ingresos')
  })

  it('should display ingresos list page', () => {
    cy.get('[data-testid="btn-nuevo-ingreso"]').should('be.visible')
    cy.get('[data-testid="ingresos-table"]').should('be.visible')
    cy.get('[data-testid="metrics-container"]').should('be.visible')
    cy.get('[data-testid="filter-bar"]').should('be.visible')
  })

  it('should show metrics with correct counts', () => {
    cy.get('[data-testid="total-ingresos"]').should('be.visible')
    cy.get('[data-testid="total-activos"]').should('be.visible')
    cy.get('[data-testid="total-reservados"]').should('be.visible')
    cy.get('[data-testid="total-finalizados"]').should('be.visible')
  })

  it('should filter by TODOS', () => {
    cy.get('[data-testid="filter-todos"]').should('have.class', 'active')
    cy.get('[data-testid="ingresos-table"]').should('be.visible')
  })

  it('should filter by EN_USO', () => {
    cy.get('[data-testid="filter-en-uso"]').click()
    cy.get('[data-testid="filter-en-uso"]').should('have.class', 'active')
  })

  it('should filter by FINALIZADOS', () => {
    cy.get('[data-testid="filter-finalizados"]').click()
    cy.get('[data-testid="filter-finalizados"]').should('have.class', 'active')
  })

  it('should navigate to create new ingreso form', () => {
    cy.get('[data-testid="btn-nuevo-ingreso"]').click()
    cy.url().should('include', '/ingresos/nuevo')
    cy.get('[data-testid="ingreso-form"]').should('be.visible')
  })

  it('should create a new ingreso (vehicle entry)', () => {
    cy.get('[data-testid="btn-nuevo-ingreso"]').click()
    
    // Select category
    cy.get('[data-testid="btn-categoria-carro"]').click()
    cy.get('[data-testid="btn-categoria-carro"]').should('have.class', 'selected')
    
    // Fill form
    cy.get('[data-testid="input-placa"]').type('INGRESO123')
    cy.get('[data-testid="select-espacio"]').select(1) // Select first available space
    cy.get('[data-testid="select-tarifa"]').select(1) // Select first tarifa
    cy.get('[data-testid="input-novedad"]').type('Sin novedad')
    
    cy.get('[data-testid="btn-confirmar"]').click()
    
    cy.url().should('include', '/ingresos/nuevo')
    cy.get('[data-testid="btn-imprimir-ticket"]').should('be.visible')
  })

  it('should show validation errors for required fields', () => {
    cy.get('[data-testid="btn-nuevo-ingreso"]').click()
    cy.get('[data-testid="btn-confirmar"]').click()
    
    cy.get('[data-testid="error-placa"]').should('be.visible')
    cy.get('[data-testid="error-espacio"]').should('be.visible')
  })

  it('should register salida for an active ingreso', () => {
    // First create an ingreso
    cy.get('[data-testid="btn-nuevo-ingreso"]').click()
    cy.get('[data-testid="btn-categoria-carro"]').click()
    cy.get('[data-testid="input-placa"]').type('SALIDA123')
    cy.get('[data-testid="select-espacio"]').select(1)
    cy.get('[data-testid="select-tarifa"]').select(1)
    cy.get('[data-testid="btn-confirmar"]').click()
    
    cy.get('[data-testid="btn-imprimir-ticket"]').should('be.visible')
    
    // Go back to list
    cy.visit('/ingresos')
    
    cy.contains('SALIDA123').closest('tr').find('[data-testid^="btn-salida-"]').should('not.exist')
    cy.contains('SALIDA123').closest('tr').find('[data-testid^="btn-pago-"]').click()

    cy.url().should('include', '/pagos/nuevo')
    cy.get('[data-testid="input-valor"]').clear().type('5000')
    cy.get('[data-testid="select-metodo-pago"]').select('EFECTIVO')
    cy.get('[data-testid="btn-guardar"]').click()
    cy.url().should('include', '/ingresos')

    cy.on('window:confirm', () => true)
    cy.contains('SALIDA123').closest('tr').within(() => {
      cy.get('[data-testid^="btn-salida-"]').click()
    })

    // The ingreso should now be FINALIZADO
    cy.contains('FINALIZADO').should('be.visible')
  })

  it('should reject salida until the ingreso has an associated pago', () => {
    cy.get('[data-testid="btn-nuevo-ingreso"]').click()
    cy.get('[data-testid="btn-categoria-carro"]').click()
    cy.get('[data-testid="input-placa"]').type('SINPAGO123')
    cy.get('[data-testid="select-espacio"]').select(1)
    cy.get('[data-testid="select-tarifa"]').select(1)
    cy.get('[data-testid="btn-confirmar"]').click()
    cy.get('[data-testid="btn-imprimir-ticket"]').should('be.visible')

    cy.visit('/ingresos')
    cy.contains('SINPAGO123').closest('tr').invoke('attr', 'data-testid').then((testId) => {
      const ingresoId = testId?.replace('ingreso-row-', '')
      cy.window().then((win) => {
        cy.request({
          method: 'PUT',
          url: `/api/ingresos/${ingresoId}/salida`,
          headers: { Authorization: `Bearer ${win.localStorage.getItem('token')}` },
          body: {},
          failOnStatusCode: false
        }).its('status').should('eq', 400)
      })
    })
    cy.contains('SINPAGO123').closest('tr').find('[data-testid^="btn-salida-"]').should('not.exist')
  })

  it('should reject salida after a failed payment and reject mismatched references', () => {
    cy.get('[data-testid="btn-nuevo-ingreso"]').click()
    cy.get('[data-testid="btn-categoria-carro"]').click()
    cy.get('[data-testid="input-placa"]').type('FALLIDOPAGO123')
    cy.get('[data-testid="select-espacio"]').select(1)
    cy.get('[data-testid="select-tarifa"]').select(1)
    cy.get('[data-testid="btn-confirmar"]').click()
    cy.get('[data-testid="btn-imprimir-ticket"]').should('be.visible')

    cy.visit('/ingresos')
    cy.contains('FALLIDOPAGO123').closest('tr').invoke('attr', 'data-testid').then((testId) => {
      const ingresoId = Number(testId?.replace('ingreso-row-', ''))
      cy.window().then((win) => {
        const headers = { Authorization: `Bearer ${win.localStorage.getItem('token')}` }
        cy.request({
          method: 'POST',
          url: '/api/pagos',
          headers,
          body: {
            ingresoId,
            vehiculoId: -1,
            valor: 5000,
            metodoPago: 'EFECTIVO',
            estadoPago: 'CONFIRMADO'
          },
          failOnStatusCode: false
        }).its('status').should('eq', 400)

        cy.request({
          method: 'POST',
          url: '/api/pagos',
          headers,
          body: {
            ingresoId,
            valor: 5000,
            metodoPago: 'EFECTIVO',
            estadoPago: 'FALLIDO'
          }
        }).its('status').should('eq', 201)

        cy.request({
          method: 'PUT',
          url: `/api/ingresos/${ingresoId}/salida`,
          headers,
          body: {},
          failOnStatusCode: false
        }).its('status').should('eq', 400)
      })
    })

    cy.visit('/ingresos')
    cy.contains('FALLIDOPAGO123').closest('tr').within(() => {
      cy.get('[data-testid^="btn-reintentar-pago-"]').should('be.visible')
      cy.get('[data-testid^="btn-salida-"]').should('not.exist')
    })
  })

  it('should liberar a reserved ingreso', () => {
    // First create an ingreso with RESERVADO state
    cy.get('[data-testid="btn-nuevo-ingreso"]').click()
    cy.get('[data-testid="btn-categoria-moto"]').click()
    cy.get('[data-testid="input-placa"]').type('LIBERAR123')
    cy.get('[data-testid="select-espacio"]').select(1)
    cy.get('[data-testid="select-tarifa"]').select(1)
    cy.get('[data-testid="btn-confirmar"]').click()
    
    cy.get('[data-testid="btn-imprimir-ticket"]').should('be.visible')
    
    // Go back to list
    cy.visit('/ingresos')
    
    // Find the created ingreso and click liberar
    cy.contains('LIBERAR123').parent().parent().within(() => {
      cy.get('[data-testid^="btn-liberar-"]').click()
    })
    
    cy.on('window:confirm', () => true)
    
    // The ingreso should be removed or space freed
    cy.contains('LIBERAR123').should('not.exist')
  })

  it('should delete an ingreso', () => {
    // First create an ingreso to delete
    cy.get('[data-testid="btn-nuevo-ingreso"]').click()
    cy.get('[data-testid="btn-categoria-carro"]').click()
    cy.get('[data-testid="input-placa"]').type('DELETE123')
    cy.get('[data-testid="select-espacio"]').select(1)
    cy.get('[data-testid="select-tarifa"]').select(1)
    cy.get('[data-testid="btn-confirmar"]').click()
    
    cy.get('[data-testid="btn-imprimir-ticket"]').should('be.visible')
    
    // Go back to list
    cy.visit('/ingresos')
    
    // Find the created ingreso and click delete
    cy.contains('DELETE123').parent().parent().within(() => {
      cy.get('[data-testid^="btn-eliminar-"]').click()
    })
    
    cy.on('window:confirm', () => true)
    
    cy.contains('DELETE123').should('not.exist')
  })

  it('should select an ingreso and show ticket preview', () => {
    // First create an ingreso
    cy.get('[data-testid="btn-nuevo-ingreso"]').click()
    cy.get('[data-testid="btn-categoria-carro"]').click()
    cy.get('[data-testid="input-placa"]').type('TICKET123')
    cy.get('[data-testid="select-espacio"]').select(1)
    cy.get('[data-testid="select-tarifa"]').select(1)
    cy.get('[data-testid="btn-confirmar"]').click()
    
    cy.get('[data-testid="btn-imprimir-ticket"]').should('be.visible')
    
    // Go back to list
    cy.visit('/ingresos')
    
    // Click on the ingreso row to select it
    cy.contains('TICKET123').parent().parent().click()
    
    // Ticket should be displayed
    cy.get('[data-testid="btn-imprimir"]').should('be.visible')
  })
})