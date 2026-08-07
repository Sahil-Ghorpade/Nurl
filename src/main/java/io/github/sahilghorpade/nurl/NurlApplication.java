package io.github.sahilghorpade.nurl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class NurlApplication {

	public static void main(String[] args) {

		SpringApplication.run(NurlApplication.class, args);
	}

}
