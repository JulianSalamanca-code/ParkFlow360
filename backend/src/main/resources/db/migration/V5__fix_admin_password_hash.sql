-- Corregir contraseña del usuario admin (contraseña: admin123)
-- Hash BCrypt generado correctamente para "admin123"
UPDATE usuarios 
SET password = '$2b$10$unss71Ore5uMMQzKNKL9hurPpIoTmqUXO6YEAqxK2OnuKnp4R7H76'
WHERE email = 'admin@parkflow360.com';
