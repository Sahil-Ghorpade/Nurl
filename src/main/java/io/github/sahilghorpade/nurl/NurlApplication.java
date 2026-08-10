package io.github.sahilghorpade.nurl;

import io.github.sahilghorpade.nurl.auth.config.AuthCookieProperties;
import io.github.sahilghorpade.nurl.auth.config.AuthTokenProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableConfigurationProperties({
		AuthCookieProperties.class,
		AuthTokenProperties.class
})
@EnableScheduling
public class NurlApplication {

	public static void main(String[] args) {

		SpringApplication.run(NurlApplication.class, args);
	}

}
