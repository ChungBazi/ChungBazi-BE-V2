package com.chungbazi.server;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

@SpringBootTest(properties = "firebase.enabled=false")
class ServerApplicationTests {

	@Autowired
	private Environment environment;

	@Test
	void contextLoads() {
		assertThat(environment.getProperty("management.endpoints.web.exposure.include"))
				.isEqualTo("health,prometheus");
		assertThat(environment.getProperty("management.endpoint.health.group.readiness.include"))
				.isEqualTo("readinessState,db,redis");
	}

}
