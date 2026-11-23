/// <reference types="cypress" />

describe('Roles canónicos en la administración de usuarios', () => {
  beforeEach(() => {
    cy.loginAsRole('PLATAFORMA', 'Plataforma de prueba')
  })

  it('ofrece los cinco roles canónicos y nunca ADMIN', () => {
    cy.visit('/usuarios/nuevo')

    cy.getByTestId('select-rol').find('option').then(opciones => {
      const valores = [...opciones].map(opcion => (opcion as HTMLOptionElement).value)
      expect(valores).to.deep.equal(['', 'PLATAFORMA', 'EMPRESA', 'CAJERO_PAGOS', 'GUARDIA', 'USUARIO'])
      expect(valores).not.to.include('ADMIN')
    })
  })

  it('normaliza un usuario ADMIN legado al editarlo', () => {
    cy.intercept('GET', '/api/usuarios/1', {
      statusCode: 200,
      body: {
        id: 1,
        nombre: 'Administrador legado',
        email: 'legado@parkflow360.com',
        rol: 'ADMIN',
        fechaCreacion: '2026-01-01T00:00:00'
      }
    }).as('usuario')

    cy.visit('/usuarios/editar/1')
    cy.wait('@usuario')

    cy.getByTestId('select-rol').should('have.value', 'PLATAFORMA')
  })

  it('cuenta y etiqueta los roles con los valores del backend', () => {
    cy.intercept('GET', '/api/usuarios', {
      statusCode: 200,
      body: [
        { id: 1, nombre: 'Legado', email: 'legado@parkflow360.com', rol: 'ADMIN', fechaCreacion: '2026-01-01T00:00:00' },
        { id: 2, nombre: 'Plataforma', email: 'plataforma@parkflow360.com', rol: 'PLATAFORMA', fechaCreacion: '2026-01-01T00:00:00' },
        { id: 3, nombre: 'Cajero', email: 'cajero@parkflow360.com', rol: 'CAJERO_PAGOS', fechaCreacion: '2026-01-01T00:00:00' }
      ]
    }).as('usuarios')

    cy.visit('/usuarios')
    cy.wait('@usuarios')

    cy.getByTestId('contador-PLATAFORMA').should('have.text', '2')
    cy.getByTestId('contador-CAJERO_PAGOS').should('have.text', '1')
    cy.getByTestId('contador-USUARIO').should('have.text', '0')
    cy.getByTestId('rol-badge').should('contain.text', 'Plataforma')
    cy.getByTestId('rol-badge').should('contain.text', 'Cajero de pagos')
    cy.getByTestId('rol-badge').should('not.contain.text', 'ADMIN')
  })
})
