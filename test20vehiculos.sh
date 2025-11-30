#!/bin/bash

echo "🚗 PRUEBA DE 20 VEHÍCULOS - MEGA SEGURA 🛡️"
echo "💡 Usando método ultra-liviano"

# Verificación básica
docker-compose ps | grep -q "Up" || {
    echo "❌ Servicios no están corriendo"
    exit 1
}

# Enviar 5 lotes de 20 mensajes
for lote in {1..5}; do
    echo "📦 Lote $lote/5 - Enviando 20 vehículos..."
    
    for i in {1..20}; do
        vehiculo_id=$(( (lote - 1) * 20 + i ))
        
        docker run --rm --network ecomviles-despliegue_app-network eclipse-mosquitto:2.0.15 \
          mosquitto_pub -h emqx -t "vehicles/SAFECAR${vehiculo_id}/telemetry" -q 1 -m "{
            \"vehicleId\": \"SAFECAR${vehiculo_id}\",
            \"latitude\": 40.41$((vehiculo_id % 10)),
            \"longitude\": -3.70$((vehiculo_id % 10)),
            \"speed\": $((20 + vehiculo_id % 60)),
            \"batteryLevel\": $((30 + vehiculo_id % 50)),
            \"timestamp\": $(date +%s%3N)
          }" &
        
        sleep 0.1  # Pausa mínima entre mensajes
    done
    
    wait
    echo "✅ Lote $lote completado"
    
    # Pausa entre lotes
    if [ $lote -lt 5 ]; then
        echo "⏳ Esperando 10 segundos..."
        sleep 10
    fi
done

echo "🎉 PRUEBA MEGA SEGURA COMPLETADA"
echo "🚗 100 vehículos SAFECAR1 a SAFECAR100 enviados"
