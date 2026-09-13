UPDATE vehiculo SET fecha_creacion = COALESCE(fecha_creacion, fecha_eliminacion, NOW() - interval '30 days') WHERE fecha_creacion IS NULL;
UPDATE conductor SET fecha_creacion = COALESCE(fecha_creacion, fecha_eliminacion, NOW() - interval '30 days') WHERE fecha_creacion IS NULL;
UPDATE ruta SET fecha_creacion = COALESCE(fecha_creacion, fecha_eliminacion, NOW() - interval '30 days') WHERE fecha_creacion IS NULL;
