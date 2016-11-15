package com.example;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableDiscoveryClient
public class ReservationClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservationClientApplication.class, args);
	}
}

@Configuration
@Slf4j
class ReservationsExtras {

	@Bean
	public ApplicationRunner serviceDiscoveryInit(DiscoveryClient discoveryClient) {
		return args -> {
			discoveryClient.getInstances("RESERVATIONSERVICE").forEach(instance -> {
				log.info("service: " + instance.getUri().toString());
			});
		};
	}
}
