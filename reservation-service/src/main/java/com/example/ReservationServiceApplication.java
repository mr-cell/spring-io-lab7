package com.example;

import static java.lang.String.format;
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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
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

	@Bean
	public ApplicationRunner init(ReservationRepository reservations) {
        return args -> Arrays
            .stream("Bartek,Marcel,Bartosz,Wojtek,Krzysztof,Daniel".split(","))
            .map(Reservation::new)
            .forEach(reservations::save);
    }
}

@Slf4j
@RestController
@RequestMapping("/custom-reservations")
class ReservationController {

	ReservationRepository reservations;

	public ReservationController(ReservationRepository reservations) {
        this.reservations = reservations;
    }

	@GetMapping
	public List<Reservation> list() {
		return reservations.findAll();
	}

	@RequestMapping(method = GET, path = "/{name}")
	public ResponseEntity<?> findOne(@PathVariable("name") String name) {
//		return Optional.ofNullable(reservations.get(name))
//				.map(ResponseEntity::ok)
//				.orElse(notFound().build());
        Reservation reservation = reservations.findByName(name);
        if (reservation != null) {
			return ok(reservation);
		} else {
			return notFound().build();
		}
	}

	@PostMapping
	public ResponseEntity<?> create(@RequestBody Reservation reservation) {
		log.info("Creating: {}", reservation);
		if (reservations.exists(Example.of(reservation))) {
			return status(CONFLICT).build();
		}
		reservations.save(reservation);
		return created(selfUri(reservation)).build();
	}

	private URI selfUri(@RequestBody Reservation reservation) {
		return linkTo(
            methodOn(ReservationController.class).findOne(reservation.name))
        .toUri();
	}
}

@Component
class ReservationResourceProcessor implements ResourceProcessor<Resource<Reservation>> {

    @Override
    public Resource<Reservation> process(Resource<Reservation> resource) {
        Reservation reservation = resource.getContent();
        String url = format("https://www.google.pl/search?tbm=isch&q=%s",
            reservation.getName());
        resource.add(new Link(url, "photo"));
        return resource;
    }
}

@RepositoryRestResource
interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @RestResource(path = "by-name", rel = "find-by-name")
    Reservation findByName(@Param("name") String name);

    @RestResource(exported = false)
    @Override
    void delete(Long id);
}

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
@Entity
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = "name")
})
class Reservation {

    @Id
    @GeneratedValue
    Long id;

    String name;

    Reservation(String name) {
        this.name = name;
    }
}
