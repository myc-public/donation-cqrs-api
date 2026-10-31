package ma.myc.donation.cqrs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class DonationCqrsApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(DonationCqrsApiApplication.class, args);
	}
}
