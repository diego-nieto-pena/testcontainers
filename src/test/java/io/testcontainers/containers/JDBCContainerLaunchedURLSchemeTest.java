package io.testcontainers.containers;

import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

@ActiveProfiles("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class JDBCContainerLaunchedURLSchemeTest {

    @LocalServerPort
    private int randomPort = 0;

    TestRestTemplate restTemplate = new TestRestTemplate();

    HttpHeaders httpHeaders = new HttpHeaders();

    @Test
    public void testGetAllItems() throws JSONException, IOException, URISyntaxException {
        HttpEntity<String> entity = new HttpEntity<>(null, httpHeaders);

        final String url = String.format("http://localhost:%s%s", randomPort, "/api/v1/people");
        final ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        String data = Commons.getFileData("data.json");
        JSONAssert.assertEquals(data, response.getBody(), false);
    }
}
