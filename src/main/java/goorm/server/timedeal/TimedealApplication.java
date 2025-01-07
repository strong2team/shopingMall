package goorm.server.timedeal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class TimedealApplication {
	public static void main(String[] args) {
		SpringApplication.run(TimedealApplication.class, args);
	}

}
