package org.example.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
class BackendApplicationTests {

	@DynamicPropertySource
	static void properties(DynamicPropertyRegistry registry) {
		registry.add("TMDB_API_KEY", () -> ("123"));
		registry.add("NETZKINO_ENV", () -> ("456"));
	}

	@Test
	void contextLoads() {
	}
}
