package com.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableCircuitBreaker
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
	
	@Bean
	@LoadBalanced
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}

@FeignClient(name = "reservationservice", fallback = ReservationsClientFallback.class)
interface ReservationsClient {
	
	@RequestMapping(method = RequestMethod.GET, path="/reservations")
	Resources<ReservationPayload> getReservations();
	
}

@Component
class ReservationsClientFallback implements ReservationsClient {

	@Override
	public Resources<ReservationPayload> getReservations() {
		List<ReservationPayload> reservations = new ArrayList<>();
		
		return new Resources<>(new ArrayList<ReservationPayload>(), new ArrayList<Link>());
	}
}

@Slf4j
@RestController
@RequestMapping("/reservations")
class ReservationClientController {
	
//	private RestTemplate restTemplate;
	private ReservationsClient reservationsClient;
	
//	ReservationClientController(RestTemplate restTemplate) {
//		this.restTemplate = restTemplate;
//	}
	
	public ReservationClientController(ReservationsClient reservations) {
		this.reservationsClient = reservations;
	}
	
	@RequestMapping(path = "/names", method = RequestMethod.GET)
	public List<String> getNames() {
//		ParameterizedTypeReference<Resources<ReservationPayload>> resultType = 
//				new ParameterizedTypeReference<Resources<ReservationPayload>>() { };
//		ResponseEntity<Resources<ReservationPayload>> reservations =
//				restTemplate.exchange("http://reservationservice/reservations", HttpMethod.GET, null, resultType);
		Resources<ReservationPayload> reservations = reservationsClient.getReservations();
		return reservations
//				.getBody()
				.getContent().stream()
				.map(ReservationPayload::getName)
				.collect(Collectors.toList());		
	}
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class ReservationPayload {
	String name;
}
