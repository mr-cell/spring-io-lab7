package com.example;

import static java.util.stream.Collectors.toList;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
public class ReservationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservationServiceApplication.class, args);
	}
}

@Slf4j
@RestController
@RequestMapping("/reservations")
class ReservationController {

	@RequestMapping(method = GET)
	public List<Reservation> list() {
		return  Arrays.stream("Bartek,Marcel,Bartosz,Wojtek,Krzysztof,Daniel".split(","))
			.map(Reservation::new)
			.collect(toList());

	}
}

@NoArgsConstructor
@AllArgsConstructor
@Data
class Reservation {

	String name;

}

