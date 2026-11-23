describe('Recuperación de contraseña', () => {
  it('muestra la pantalla y permite solicitar el enlace', () => {
    cy.visit('/auth/login');
    cy.contains('¿Olvidaste tu contraseña?')
      .parent()
      .contains('Recupérala aquí')
      .click();

    cy.url().should('include', '/auth/recuperar');
    cy.get('[data-testid="recuperar-email"]').type('usuario@ejemplo.com');
    cy.get('[data-testid="recuperar-enviar"]').click();
  });

  it('rechaza dos contraseñas diferentes', () => {
    cy.visit('/auth/restablecer/token-prueba');
    cy.get('[data-testid="restablecer-password"]').type('password123');
    cy.get('[data-testid="restablecer-confirmar"]').type('password456');
    cy.contains('Las contraseñas deben coincidir').should('be.visible');
  });
});
