package io.testcontainers.containers;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.platform.commons.annotation.Testable;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.startupcheck.MinimumDurationRunningStartupCheckStrategy;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testable
public class GenericContainerTest {

    @ClassRule
    public static GenericContainer webServer =
            new GenericContainer("alpine:3.2")
                    .withExposedPorts(8005)
                    .withCommand("/bin/sh", "-c", "while true; do echo "
                            + "\"HTTP/1.1 200 OK\n\nHello World!\" | nc -l -p 8005; done")
                    .withStartupCheckStrategy(
                            new MinimumDurationRunningStartupCheckStrategy(Duration.ofSeconds(1))
                    );

    @Test
    public void test_webserver_container_get_request_then_return_response() throws Exception {
        String address = String.format("http://%s:%d", webServer.getContainerIpAddress(),
                webServer.getMappedPort(8005));

        String response = getRequest(new URL(address));

        assertEquals(response, "Hello World!");
    }

    private String getRequest(URL url) throws Exception {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        return content.toString();
    }

}
