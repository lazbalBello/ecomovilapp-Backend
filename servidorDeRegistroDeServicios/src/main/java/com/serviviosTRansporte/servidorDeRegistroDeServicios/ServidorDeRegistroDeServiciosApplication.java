package com.serviviosTRansporte.servidorDeRegistroDeServicios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class ServidorDeRegistroDeServiciosApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServidorDeRegistroDeServiciosApplication.class, args);
	}

}
