-- Usuario de prueba con rol USUARIO (cliente) · contraseña: usuario123
INSERT INTO usuarios (email, password, rol, nombre, fecha_creacion)
VALUES (
    'cliente@parkflow360.com',
    '$2b$10$ieyeBThrReQskIAaiWflYOmWJLqf78IOC1zfVaKY8.Noot0qvYmGG',
    'USUARIO',
    'Cliente Demo',
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;
