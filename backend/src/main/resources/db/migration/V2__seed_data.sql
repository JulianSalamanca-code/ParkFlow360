-- Datos de prueba para ParkFlow360

-- Usuario administrador (contraseña: admin123)
INSERT INTO usuarios (email, password, rol, nombre, fecha_creacion)
VALUES ('admin@parkflow360.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', 'Administrador', CURRENT_TIMESTAMP);

-- Vehículos de prueba
INSERT INTO vehiculos (placa, tipo, color, modelo, fecha_creacion) VALUES
('ABC123', 'CARRO', 'Rojo', 'Toyota Corolla', CURRENT_TIMESTAMP),
('DEF456', 'MOTO', 'Negro', 'Honda CBR', CURRENT_TIMESTAMP),
('GHI789', 'CAMIONETA', 'Blanco', 'Ford Ranger', CURRENT_TIMESTAMP);

-- Espacios de prueba
INSERT INTO espacios (numero, tipo, estado, piso, fecha_creacion) VALUES
('A01', 'CARRO', 'LIBRE', 1, CURRENT_TIMESTAMP),
('A02', 'CARRO', 'OCUPADO', 1, CURRENT_TIMESTAMP),
('A03', 'CARRO', 'LIBRE', 1, CURRENT_TIMESTAMP),
('B01', 'MOTO', 'LIBRE', 1, CURRENT_TIMESTAMP),
('B02', 'MOTO', 'OCUPADO', 1, CURRENT_TIMESTAMP),
('C01', 'CAMIONETA', 'LIBRE', 2, CURRENT_TIMESTAMP);

-- Tarifas de prueba
INSERT INTO tarifas (nombre, tipo, valor, duracion, fecha_creacion) VALUES
('Tarifa por hora', 'HORA', 2000.00, '1 hora', CURRENT_TIMESTAMP),
('Tarifa por día', 'DIA', 15000.00, '1 día', CURRENT_TIMESTAMP),
('Tarifa mensual', 'MES', 300000.00, '1 mes', CURRENT_TIMESTAMP),
('Tarifa por momento', 'MOMENTO', 5000.00, '15 minutos', CURRENT_TIMESTAMP);

-- Pagos de prueba
INSERT INTO pagos (vehiculo_id, espacio_id, tarifa_id, valor, fecha, metodo_pago) VALUES
(1, 2, 1, 2000.00, CURRENT_TIMESTAMP, 'EFECTIVO'),
(2, 5, 1, 2000.00, CURRENT_TIMESTAMP, 'TARJETA'),
(3, 6, 3, 300000.00, CURRENT_TIMESTAMP, 'TRANSFERENCIA');
