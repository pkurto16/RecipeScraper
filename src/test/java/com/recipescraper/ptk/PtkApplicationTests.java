package com.recipescraper.ptk;

import com.recipescraper.Application;
import com.recipescraper.model.wire.ScrapeRequest;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static io.restassured.RestAssured.given;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
				webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
				classes = Application.class)
class PtkApplicationTests {
//todo: write tests
	@Test
	void kingArthur() {
		ScrapeRequest request = ScrapeRequest.builder().url("https://www.kingarthurbaking.com/recipes/chocolate-chip-cookies-recipe").build();
		given().contentType(ContentType.JSON)
						.when().body(request)
						.post("/scrape")
						.then()
						.statusCode(HttpStatus.SC_OK);
	}

}
