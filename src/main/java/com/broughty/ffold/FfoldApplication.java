package com.broughty.ffold;

import com.broughty.ffold.entity.Week;
import com.broughty.ffold.repository.WeekRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class FfoldApplication {

	private static final Logger log = LoggerFactory.getLogger(FfoldApplication.class);
	public static void main(String[] args) {
		SpringApplication.run(FfoldApplication.class);
	}

	@Bean
	public CommandLineRunner loadData(WeekRepository repository) {
		return (args) -> {
			// save a couple of customers
			repository.save(new Week("Jack", "Bauer"));
			repository.save(new Week("Chloe", "O'Brian"));
			repository.save(new Week("Kim", "Bauer"));
			repository.save(new Week("David", "Palmer"));
			repository.save(new Week("Michelle", "Dessler"));

			// fetch all customers
			log.info("Customers found with findAll():");
			log.info("-------------------------------");
			for (Week customer : repository.findAll()) {
				log.info(customer.toString());
			}
			log.info("");

			// fetch an individual customer by ID
			Week customer = repository.findById(1L).get();
			log.info("Week found with findOne(1L):");
			log.info("--------------------------------");
			log.info(customer.toString());
			log.info("");

			// fetch customers by last name
			log.info("Week found with findByLastNameStartsWithIgnoreCase('Bauer'):");
			log.info("--------------------------------------------");
			for (Week bauer : repository
					.findByLastNameStartsWithIgnoreCase("Bauer")) {
				log.info(bauer.toString());
			}
			log.info("");
		};
	}
}
