package com.chungbazi.server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "firebase.enabled=false")
class ServerApplicationTests {

	@Test
	void contextLoads() {
	}

}
