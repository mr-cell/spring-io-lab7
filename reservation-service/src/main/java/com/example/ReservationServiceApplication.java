package com.example;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
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

	Map<String, Reservation> reservations;

	public ReservationController() {
		this.reservations = Arrays
			.stream("Bartek,Marcel,Bartosz,Wojtek,Krzysztof,Daniel".split(","))
			.collect(toMap(identity(), Reservation::new));
	}

	@GetMapping
	public List<Reservation> list() {
		return reservations.values().stream()
			.collect(toList());
	}

	@RequestMapping(method = GET, path = "/{name}")
	public ResponseEntity<?> findOne(@PathVariable("name") String name) {
//		return Optional.ofNullable(reservations.get(name))
//				.map(ResponseEntity::ok)
//				.orElse(notFound().build());
		if (reservations.containsKey(name)) {
			return ok(reservations.get(name));
		} else {
			return notFound().build();
		}
	}

	@PostMapping
	public ResponseEntity<?> create(@RequestBody Reservation reservation) {
		log.info("Creating: {}", reservation);
		if (reservations.containsKey(reservation.name)) {
			return status(CONFLICT).build();
		}
		reservations.put(reservation.name, reservation);
		return created(selfUri(reservation)).build();
	}

	private URI selfUri(@RequestBody Reservation reservation) {
		return linkTo(
            methodOn(ReservationController.class).findOne(reservation.name))
        .toUri();
	}
}

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
class Reservation {

	String name;

}
