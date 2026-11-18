/// <reference types="cypress" />

const ingresoActivo = {
  id: 21,
  folio: 'TK-000021',
  placa: 'ABC123',
  categoria: 'CARRO',
  vehiculoId: 1,
  espacioId: 1,
  espacioNumero: 'A01',
  tarifaId: 1,
  tarifaValor: 3000,
  estado: 'ACTIVO',
  pagoRegistrado: false,
  pagoId: undefined,
  estadoPago: undefined
}

const pagoHistorico = {
  id: 8,
  ingresoId: null,
  vehiculoId: 1,
  espacioId: 1,
  tarifaId: 1,
  valor: 3000,
  fecha: '2026-11-18T10:00:00',
  metodoPago: 'TARJETA',
  estadoPago: 'CONFIRMADO' as const
}

describe('Pagos Management', () => {
  beforeEach(() => {
    cy.loginAsAdmin()
    cy.intercept('GET', '/api/pagos', { statusCode: 200, body: [] })
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
    cy.intercept('GET', '/api/ingresos/activos', { statusCode: 200, body: [ingresoActivo] })
    cy.get('[data-testid="btn-nuevo-pago"]').click()
    cy.url().should('include', '/pagos/nuevo')
    cy.get('[data-testid="pago-form"]').should('be.visible')
    cy.get('[data-testid="select-ingreso"]').should('be.visible')
  })

  it('should create a pago linked to the selected ingreso', () => {
    cy.intercept('GET', '/api/ingresos/activos', { statusCode: 200, body: [ingresoActivo] })
    cy.intercept('POST', '/api/pagos', (request) => {
      expect(request.body).to.deep.equal({
        ingresoId: ingresoActivo.id,
        valor: 5000,
        metodoPago: 'EFECTIVO',
        estadoPago: 'CONFIRMADO'
      })
      request.reply({
        statusCode: 201,
        body: {
          ...pagoHistorico,
          id: 11,
          ingresoId: ingresoActivo.id,
          valor: 5000,
          metodoPago: 'EFECTIVO',
          estadoPago: 'CONFIRMADO'
        }
      })
    }).as('createPayment')

    cy.get('[data-testid="btn-nuevo-pago"]').click()
    cy.get('[data-testid="select-ingreso"]').select(1)
    cy.get('[data-testid="input-valor"]').clear().type('5000')
    cy.get('[data-testid="select-metodo-pago"]').select('EFECTIVO')
    cy.get('[data-testid="select-estado-pago"]').select('CONFIRMADO')
    cy.get('[data-testid="btn-guardar"]').click()
    cy.wait('@createPayment')
    cy.url().should('include', '/pagos')
  })

  it('should validate the required ingreso, value and payment method', () => {
    cy.intercept('GET', '/api/ingresos/activos', { statusCode: 200, body: [ingresoActivo] })
    cy.get('[data-testid="btn-nuevo-pago"]').click()
    cy.get('[data-testid="select-ingreso"]').focus().blur()
    cy.get('[data-testid="input-valor"]').focus().blur()
    cy.get('[data-testid="select-metodo-pago"]').focus().blur()
    cy.get('[data-testid="select-estado-pago"]').focus().blur()
    cy.get('[data-testid="error-ingreso"]').should('be.visible')
    cy.get('[data-testid="error-valor"]').should('be.visible')
    cy.get('[data-testid="error-metodo-pago"]').should('be.visible')
    cy.get('[data-testid="error-estado-pago"]').should('be.visible')
    cy.get('[data-testid="select-metodo-pago"]').should('have.value', '')
  })

  it('should retain a failed payment attempt without enabling vehicle exit', () => {
    cy.intercept('GET', '/api/ingresos/activos', { statusCode: 200, body: [ingresoActivo] })
    cy.intercept('POST', '/api/pagos', (request) => {
      expect(request.body.estadoPago).to.equal('FALLIDO')
      request.reply({
        statusCode: 201,
        body: { ...pagoHistorico, id: 11, ingresoId: ingresoActivo.id, estadoPago: 'FALLIDO' }
      })
    }).as('failedPayment')
    cy.intercept('GET', '/api/ingresos', {
      statusCode: 200,
      body: [{ ...ingresoActivo, pagoId: 11, estadoPago: 'FALLIDO', pagoRegistrado: false }]
    })

    cy.visit(`/pagos/nuevo?ingresoId=${ingresoActivo.id}`)
    cy.get('[data-testid="input-valor"]').clear().type('5000')
    cy.get('[data-testid="select-metodo-pago"]').select('EFECTIVO')
    cy.get('[data-testid="select-estado-pago"]').select('FALLIDO')
    cy.get('[data-testid="btn-guardar"]').click()
    cy.wait('@failedPayment')
    cy.url().should('include', '/ingresos')
    cy.get(`[data-testid="btn-reintentar-pago-${ingresoActivo.id}"]`).should('be.visible')
    cy.get(`[data-testid="btn-salida-${ingresoActivo.id}"]`).should('not.exist')
  })

  it('should edit a historical pago without losing compatibility', () => {
    cy.intercept('GET', '/api/pagos', { statusCode: 200, body: [pagoHistorico] })
    cy.intercept('GET', '/api/pagos/8', { statusCode: 200, body: pagoHistorico })
    cy.intercept('GET', '/api/ingresos/activos', { statusCode: 200, body: [] })
    cy.intercept('PUT', '/api/pagos/8', (request) => {
      expect(request.body).to.include({ vehiculoId: 1, espacioId: 1, tarifaId: 1, valor: 4000 })
      request.reply({ statusCode: 200, body: { ...pagoHistorico, valor: 4000, metodoPago: 'TRANSFERENCIA' } })
    }).as('updatePayment')

    cy.visit('/pagos')
    cy.get('[data-testid="btn-editar-8"]').click()
    cy.get('[data-testid="input-valor"]').clear().type('4000')
    cy.get('[data-testid="select-metodo-pago"]').select('TRANSFERENCIA')
    cy.get('[data-testid="btn-guardar"]').click()
    cy.wait('@updatePayment')
    cy.url().should('include', '/pagos')
  })

  it('should delete a historical pago', () => {
    let payments = [pagoHistorico]
    cy.intercept('GET', '/api/pagos', (request) => request.reply({ statusCode: 200, body: payments }))
    cy.intercept('DELETE', '/api/pagos/8', (request) => {
      payments = []
      request.reply({ statusCode: 204 })
    }).as('deletePayment')

    cy.visit('/pagos')
    cy.on('window:confirm', () => true)
    cy.get('[data-testid="btn-eliminar-8"]').click()
    cy.wait('@deletePayment')
    cy.get('[data-testid="empty-state"]').should('be.visible')
  })
})
