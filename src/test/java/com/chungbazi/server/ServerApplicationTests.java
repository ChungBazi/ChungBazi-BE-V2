package com.chungbazi.server;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = "firebase.enabled=false")
@ActiveProfiles("test")
class ServerApplicationTests {

	@Autowired
	private Environment environment;

	@Test
	void contextLoads() {
		assertThat(environment.getProperty("management.endpoints.web.exposure.include[0]"))
				.isEqualTo("health");
		assertThat(environment.getProperty("management.endpoints.web.exposure.include[1]"))
				.isEqualTo("prometheus");
		assertThat(environment.getProperty("management.endpoint.health.group.readiness.include[0]"))
				.isEqualTo("readinessState");
	}

}
