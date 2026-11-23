/// <reference types="cypress" />

interface EmpresaApi {
  readonly id: number;
  readonly nombre: string;
  readonly activo: boolean;
  readonly toleranciaSalidaMinutos: number;
}

interface TipoApi {
  readonly id: number;
  readonly tipo: string;
  readonly cantidadFija: boolean;
  readonly cantidad: number;
  readonly espaciosOcupados: number;
}

interface ParqueaderoApi {
  readonly id: number;
  readonly empresaId: number;
  readonly nombre: string;
  readonly identificador: string;
  readonly capacidadVehiculos: number;
  readonly activo: boolean;
  readonly cajeroEmail: string;
  readonly tipos: readonly TipoApi[];
}

describe('EMPRESA: alta de parqueaderos', () => {
  const empresa: EmpresaApi = {
    id: 7,
    nombre: 'Empresa de prueba',
    activo: true,
    toleranciaSalidaMinutos: 30
  };

  const creado: ParqueaderoApi = {
    id: 11,
    empresaId: 7,
    nombre: 'Parqueadero Central',
    identificador: 'CENTRAL-01',
    capacidadVehiculos: 50,
    activo: true,
    cajeroEmail: 'cajero@parkflow360.com',
    tipos: [{ id: 1, tipo: 'CARRO', cantidadFija: true, cantidad: 10, espaciosOcupados: 0 }]
  };

  let parqueaderos: ParqueaderoApi[];

  function llenarFormulario(): void {
    cy.getByTestId('pq-nombre').type('Parqueadero Central');
    cy.getByTestId('pq-identificador').type('central-01');
    cy.getByTestId('pq-capacidad').type('50');
    cy.getByTestId('pq-tipo-0').type('carro');
    cy.getByTestId('pq-cantidad-fija-0').check();
    cy.getByTestId('pq-cantidad-0').type('10');
    cy.getByTestId('pq-puestos-0').type('2');
    cy.getByTestId('pq-cajero-email').type('cajero@parkflow360.com');
    cy.getByTestId('pq-cajero-password').type('clave1234');
    cy.getByTestId('pq-cajero-nombre').type('Cajero Central');
  }

  beforeEach(() => {
    parqueaderos = [];
    cy.intercept('GET', '/api/empresas/mias', {
      statusCode: 200,
      body: [empresa]
    }).as('empresas');
    cy.intercept('GET', '/api/parqueaderos/mios', req => {
      req.reply({ statusCode: 200, body: parqueaderos });
    }).as('parqueaderos');
    cy.intercept('GET', '/api/espacios', {
      statusCode: 200,
      body: []
    }).as('espacios');
  });

  it('ofrece crear el primer parqueadero cuando la empresa no tiene ninguno', () => {
    cy.loginAsRole('EMPRESA', 'Empresa de prueba');
    cy.visit('/empresa/parqueaderos');

    cy.getByTestId('empresa-crear-primer-parqueadero').should('be.visible').click();
    cy.getByTestId('parqueadero-form').should('be.visible');
    cy.getByTestId('empresa-nuevo-parqueadero').should('exist');
  });

  it('crea un parqueadero con tipos y usuario cajero', () => {
    cy.intercept('POST', '/api/empresas/7/parqueaderos', req => {
      expect(req.body).to.deep.equal({
        nombre: 'Parqueadero Central',
        identificador: 'CENTRAL-01',
        capacidadVehiculos: 50,
        tipos: [{ tipo: 'CARRO', cantidadFija: true, cantidad: 10 }],
        espacios: [
          { numero: 'CARRO-001', tipo: 'CARRO', piso: 1 },
          { numero: 'CARRO-002', tipo: 'CARRO', piso: 1 }
        ],
        cajeroEmail: 'cajero@parkflow360.com',
        cajeroPassword: 'clave1234',
        cajeroNombre: 'Cajero Central'
      });
      parqueaderos = [creado];
      req.reply({ statusCode: 201, body: creado });
    }).as('crear');

    cy.loginAsRole('EMPRESA', 'Empresa de prueba');
    cy.visit('/empresa/parqueaderos');
    cy.getByTestId('empresa-nuevo-parqueadero').click();
    llenarFormulario();
    cy.getByTestId('pq-submit').click();

    cy.wait('@crear');
    cy.getByTestId('parqueadero-form').should('not.exist');
    cy.getByTestId('empresa-success').should('contain', 'creado correctamente');
    cy.getByTestId('empresa-parqueaderos').should('contain', 'Parqueadero Central');
    cy.getByTestId('empresa-parqueaderos').should('contain', 'CENTRAL-01');
  });

  it('muestra el mensaje del backend cuando el identificador ya existe', () => {
    cy.intercept('POST', '/api/empresas/7/parqueaderos', {
      statusCode: 400,
      body: {
        timestamp: '2026-01-01T00:00:00',
        status: 400,
        error: 'Solicitud inválida',
        message: 'Ya existe un parqueadero con el identificador: CENTRAL-01',
        path: '/api/empresas/7/parqueaderos'
      }
    }).as('crearDuplicado');

    cy.loginAsRole('EMPRESA', 'Empresa de prueba');
    cy.visit('/empresa/parqueaderos');
    cy.getByTestId('empresa-nuevo-parqueadero').click();
    llenarFormulario();
    cy.getByTestId('pq-submit').click();

    cy.wait('@crearDuplicado');
    cy.getByTestId('pq-form-error').should('contain', 'Ya existe un parqueadero');
    cy.getByTestId('parqueadero-form').should('exist');
  });

  it('permite varios parqueaderos y preselecciona el elegido al agregar puestos', () => {
    parqueaderos = [
      creado,
      {
        id: 22,
        empresaId: 7,
        nombre: 'Parqueadero Norte',
        identificador: 'NORTE-02',
        capacidadVehiculos: 30,
        activo: true,
        cajeroEmail: 'cajero.norte@parkflow360.com',
        tipos: []
      }
    ];

    cy.loginAsRole('EMPRESA', 'Empresa de prueba');
    cy.visit('/empresa/parqueaderos');

    cy.getByTestId('empresa-parqueaderos').should('contain', 'Parqueadero Central');
    cy.getByTestId('empresa-parqueaderos').should('contain', 'Parqueadero Norte');
    cy.getByTestId('empresa-ver-puestos-22').should('exist');
    cy.getByTestId('empresa-agregar-puestos-22').click();

    cy.location('pathname').should('eq', '/espacios/importar');
    cy.location('search').should('contain', 'parqueadero=22');
    cy.getByTestId('select-parqueadero').find('option:selected')
      .should('contain', 'Parqueadero Norte')
      .and('contain', 'NORTE-02');
  });
});
