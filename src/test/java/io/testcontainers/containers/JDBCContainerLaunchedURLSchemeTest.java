package io.testcontainers.containers;

import io.testcontainers.commons.Utils;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;

@ActiveProfiles("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

public class JDBCContainerLaunchedURLSchemeTest {

    @LocalServerPort
    private int randomPort = 0;

    TestRestTemplate restTemplate = new TestRestTemplate();

    HttpHeaders httpHeaders = new HttpHeaders();

    @Test
    public void test_get_all_items() throws JSONException, IOException, URISyntaxException {
        HttpEntity<String> entity = new HttpEntity<>(null, httpHeaders);

        final String url = String.format("http://localhost:%s%s", randomPort, "/api/v1/people");
        final ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        String data = Utils.getFileData("data.json");
        JSONAssert.assertEquals(data, response.getBody(), false);
    }
}
