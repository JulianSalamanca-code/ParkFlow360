/// <reference types="cypress" />

describe('Navegación responsive por rol', () => {
  describe('PLATAFORMA en móvil', () => {
    beforeEach(() => {
      cy.viewport(390, 844)
      cy.loginAsRole('ADMIN', 'Admin de prueba')
      cy.visit('/')
    })

    it('muestra la barra inferior con accesos rápidos y hamburguesa', () => {
      cy.url().should('include', '/planos')
      cy.getByTestId('bottom-nav').should('be.visible')
      cy.getByTestId('bottom-nav-item-planos').should('be.visible')
      cy.getByTestId('bottom-nav-item-espacios').should('be.visible')
      cy.getByTestId('bottom-nav-item-ingresos').should('be.visible')
      cy.getByTestId('bottom-nav-item-pagos').should('be.visible')
      cy.getByTestId('nav-hamburger').should('be.visible')
    })

    it('abre y cierra el menú lateral desde la hamburguesa', () => {
      cy.get('#app-sidebar').should('not.have.class', 'open')
      cy.getByTestId('nav-hamburger').click()
      cy.get('#app-sidebar').should('have.class', 'open')
      cy.getByTestId('nav-hamburger').click()
      cy.get('#app-sidebar').should('not.have.class', 'open')
    })

    it('navega desde la barra inferior', () => {
      cy.getByTestId('bottom-nav-item-espacios').click()
      cy.url().should('include', '/espacios')
      cy.getByTestId('bottom-nav-item-espacios').should('have.class', 'active')
    })
  })

  describe('USUARIO en móvil', () => {
    beforeEach(() => {
      cy.viewport(390, 844)
      cy.loginAsRole('USUARIO', 'Cliente de prueba')
      cy.visit('/')
    })

    it('aterriza en mis parqueos y muestra sus accesos', () => {
      cy.url().should('include', '/mis-parqueos')
      cy.getByTestId('bottom-nav-item-reservar').should('be.visible')
      cy.getByTestId('bottom-nav-item-mis-parqueos').should('be.visible')
      cy.getByTestId('bottom-nav-item-mis-vehiculos').should('be.visible')
      cy.getByTestId('bottom-nav-item-pagar').should('be.visible')
      cy.getByTestId('nav-hamburger').should('be.visible')
    })

    it('no permite abrir rutas de la plataforma', () => {
      cy.visit('/usuarios')
      cy.url().should('include', '/mis-parqueos')
    })
  })

  describe('Escritorio', () => {
    it('oculta la barra inferior y mantiene el sidebar', () => {
      cy.viewport(1280, 720)
      cy.loginAsRole('ADMIN')
      cy.visit('/')

      cy.getByTestId('bottom-nav').should('not.be.visible')
      cy.getByTestId('nav-item-planos').should('be.visible')
    })
  })
})
