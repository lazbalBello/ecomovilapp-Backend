#!/bin/bash
set -e  # Detener en cualquier error

echo "🔒 INICIANDO PRUEBA SEGURA"

# 1. Verificar servicios
echo "1. ✅ Verificando servicios..."
docker-compose ps | grep "Up" || {
    echo "❌ Algunos servicios no están corriendo"
    exit 1
}

# 2. Enviar mensaje de prueba
echo "2. 📤 Enviando mensaje de prueba..."
docker run --rm --network ecomviles-despliegue_app-network eclipse-mosquitto:2.0.15 \
  mosquitto_pub -h emqx -t  "vehicles/CORRECTOTEST/telemetry" -m '{
    "vehicleId": "CORRECTOTEST",
    "latitude": 40.4168,         
    "longitude": -3.7038,          
    "speed": 50.0,
    "batteryLevel": 80.0,        
    "timestamp": 1717600000000
  }'

# 3. Pequeña pausa para procesamiento
sleep 2

# 4. Ver logs del servicio
echo "3. 📝 Revisando logs del servicio..."
echo "--- ÚLTIMAS LÍNEAS DE LOG ---"
docker logs emqx --tail 10
echo "--- FIN DE LOGS ---"

echo "🎉 PRUEBA COMPLETADA EXITOSAMENTE"
echo "📊 Revisa los logs arriba para confirmar que el mensaje fue procesado"
