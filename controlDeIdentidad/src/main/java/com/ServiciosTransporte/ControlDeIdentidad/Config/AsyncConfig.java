package com.ServiciosTransporte.ControlDeIdentidad.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Configuración del executor para tareas asíncronas de recuperación de contraseña.
 *
 * <p>Se usa un pool acotado para garantizar que:</p>
 * <ul>
 *   <li>Las fallas de Keycloak o SMTP no consumen hilos ilimitadamente.</li>
 *   <li>El sistema sigue respondiendo aunque el executor esté bajo carga alta.</li>
 *   <li>Las peticiones en exceso se descartan silenciosamente (la respuesta al usuario
 *       ya fue enviada con el mensaje genérico anti-enumeración).</li>
 * </ul>
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Executor dedicado para el worker de recuperación de contraseña.
     *
     * <ul>
     *   <li>{@code corePoolSize=2}: 2 hilos permanentes — suficiente para carga normal.</li>
     *   <li>{@code maxPoolSize=5}: hasta 5 hilos concurrentes en picos.</li>
     *   <li>{@code queueCapacity=50}: cola acotada; evita acumulación ilimitada de tareas.</li>
     *   <li>{@code keepAliveSeconds=30}: los hilos extra se destruyen si no hay trabajo.</li>
     *   <li>{@code DiscardPolicy}: descarta silenciosamente si cola+pool están llenos
     *       (no lanza RejectedExecutionException — el usuario ya recibió su 200).</li>
     * </ul>
     */
    @Bean("recuperacionPasswordExecutor")
    public Executor recuperacionPasswordExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("recup-pwd-");
        executor.setKeepAliveSeconds(30);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);
        executor.initialize();
        return executor;
    }
}
