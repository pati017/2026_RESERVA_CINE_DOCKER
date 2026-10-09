package com.cinema.booking_service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Prueba de integración: requiere PostgreSQL (make infra-up) y auth-service en :50003")
@SpringBootTest
class BookingServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
