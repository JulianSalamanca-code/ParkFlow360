/// <reference types="cypress" />

interface EstadoPagoFalso {
  readonly ingresoId: number;
  readonly estado: string;
  readonly alDia: boolean;
  readonly puedeSalir: boolean;
  readonly fechaPago: string | null;
  readonly minutosTolerancia: number;
  readonly fechaLimiteSalida: string | null;
  readonly valorActual: number | null;
  readonly faltante: number | null;
}

const ingresoActivo = {
  id: 21,
  folio: 'TK-000021',
  placa: 'PAG321',
  categoria: 'CARRO',
  espacioId: 1,
  espacioNumero: 'A01',
  tarifaId: 1,
  tarifaValor: 3000,
  estado: 'ACTIVO',
  fechaEntrada: '2026-11-23T08:00:00'
};

const fechaEn = (minutos: number): string => new Date(Date.now() + minutos * 60000).toISOString();

const estadoPagado: EstadoPagoFalso = {
  ingresoId: ingresoActivo.id,
  estado: 'AL_DIA',
  alDia: true,
  puedeSalir: true,
  fechaPago: fechaEn(-5),
  minutosTolerancia: 30,
  fechaLimiteSalida: fechaEn(25),
  valorActual: 3000,
  faltante: 0
};

describe('Flujo pagaduría → salida del guardia → tolerancia', () => {
  let estadoActual: EstadoPagoFalso;

  beforeEach(() => {
    estadoActual = estadoPagado;
  });

  const visitarGuardia = (): void => {
    cy.intercept('GET', '/api/ingresos/activos', {
      statusCode: 200,
      body: [ingresoActivo]
    }).as('ingresosActivos');
    cy.intercept('GET', '/api/ingresos/*/estado-pago', request => {
      request.reply({ statusCode: 200, body: estadoActual });
    }).as('estadoPago');
    cy.intercept('GET', '/api/espacios', { statusCode: 200, body: [] });
    cy.intercept('PUT', '/api/ingresos/*/salida', {
      statusCode: 200,
      body: { ...ingresoActivo, estado: 'FINALIZADO' }
    }).as('registrarSalida');

    cy.loginAsRole('GUARDIA', 'Guardia de prueba');
    cy.visit('/guardia/ingresos');
    cy.wait(['@ingresosActivos', '@estadoPago']);
  };

  it('muestra el contador y permite la salida cuando el pago está confirmado', () => {
    visitarGuardia();

    cy.getByTestId(`guardia-pago-${ingresoActivo.id}`).should('contain', 'Pagado');
    cy.getByTestId(`guardia-countdown-${ingresoActivo.id}`)
      .should('contain', 'min para salir de la universidad');
    cy.getByTestId(`guardia-accion-${ingresoActivo.id}`)
      .should('not.be.disabled')
      .should('contain', 'Registrar salida')
      .click();

    cy.wait('@registrarSalida').its('request.url')
      .should('include', `/api/ingresos/${ingresoActivo.id}/salida`);
    cy.getByTestId('guardia-success').should('contain', `Salida de ${ingresoActivo.placa} registrada.`);
  });

  it('bloquea la salida si el conductor aún no pagó en pagaduría', () => {
    estadoActual = {
      ...estadoPagado,
      estado: 'PENDIENTE',
      alDia: false,
      puedeSalir: false,
      fechaPago: null,
      fechaLimiteSalida: null,
      faltante: 3000
    };
    visitarGuardia();

    cy.getByTestId(`guardia-pago-${ingresoActivo.id}`).should('contain', 'Pendiente de pago');
    cy.getByTestId(`guardia-countdown-${ingresoActivo.id}`).should('not.exist');
    cy.getByTestId(`guardia-accion-${ingresoActivo.id}`)
      .should('be.disabled');
    cy.contains('El conductor debe pagar en pagaduría.').should('be.visible');
  });

  it('exige el cobro de la diferencia cuando vence la tolerancia', () => {
    estadoActual = {
      ...estadoPagado,
      estado: 'TOLERANCIA_VENCIDA',
      alDia: false,
      puedeSalir: false,
      fechaLimiteSalida: fechaEn(-2),
      valorActual: 4500,
      faltante: 1500
    };
    visitarGuardia();

    cy.getByTestId(`guardia-pago-${ingresoActivo.id}`).should('contain', 'Tolerancia vencida');
    cy.contains('Debe pagar el tiempo adicional en pagaduría.').should('be.visible');
    cy.getByTestId(`guardia-accion-${ingresoActivo.id}`).should('be.disabled');
  });

  it('el cajero cobra solo la diferencia y respeta el tipo de tarifa', () => {
    const facturaDiferencia = {
      id: 99,
      ingresoId: ingresoActivo.id,
      valor: 3000,
      valorActual: 4500,
      faltante: 1500,
      estado: 'PAGADA',
      estadoIngreso: 'ACTIVO',
      fechaEmision: '2026-11-23T08:00:00',
      fechaPago: '2026-11-23T08:05:00'
    };

    cy.intercept('GET', '/api/ingresos/activos', {
      statusCode: 200,
      body: [ingresoActivo]
    }).as('ingresosActivos');
    cy.intercept('GET', '/api/facturas', {
      statusCode: 200,
      body: [facturaDiferencia]
    }).as('facturas');
    cy.intercept('POST', '/api/facturas/99/pagar', request => {
      expect(request.body).to.deep.equal({});
      request.reply({ statusCode: 200, body: { ...facturaDiferencia, valor: 4500, faltante: 0 } });
    }).as('cobrarDiferencia');

    cy.loginAsRole('CAJERO_PAGOS', 'Cajero de prueba');
    cy.visit('/cajero/pagos');
    cy.wait(['@facturas', '@ingresosActivos']);

    cy.getByTestId('cajero-diferencia-99').should('contain', 'Tiempo adicional');
    cy.getByTestId('cajero-confirmar-99')
      .should('contain', 'Cobrar diferencia')
      .click();

    cy.wait('@cobrarDiferencia');
    cy.getByTestId('cajero-success')
      .should('contain', 'Diferencia')
      .and('contain', 'el guardia ya puede autorizar la salida');
  });

  it('la empresa configura una tolerancia heredada por todos sus parqueaderos', () => {
    cy.intercept('GET', '/api/empresas/mias', {
      statusCode: 200,
      body: [{
        id: 7,
        nombre: 'Universidad Ejemplo',
        activo: true,
        toleranciaSalidaMinutos: 30
      }]
    }).as('empresas');
    cy.intercept('PUT', '/api/empresas/7/configuracion', request => {
      expect(request.body).to.deep.equal({ toleranciaSalidaMinutos: 45 });
      request.reply({
        statusCode: 200,
        body: {
          id: 7,
          nombre: 'Universidad Ejemplo',
          activo: true,
          toleranciaSalidaMinutos: 45
        }
      });
    }).as('guardarConfiguracion');

    cy.loginAsRole('EMPRESA', 'Empresa de prueba');
    cy.visit('/empresa/configuracion');
    cy.wait('@empresas');

    cy.getByTestId('empresa-configuracion').should('be.visible');
    cy.getByTestId('empresa-tolerancia')
      .should('have.value', '30')
      .clear()
      .type('45');
    cy.getByTestId('empresa-guardar-tolerancia').click();

    cy.wait('@guardarConfiguracion');
    cy.getByTestId('empresa-success').should('contain', 'Tolerancia de salida actualizada');
    cy.getByTestId('empresa-tolerancia').should('have.value', '45');
  });
});
