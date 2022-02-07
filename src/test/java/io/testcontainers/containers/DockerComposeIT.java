package io.testcontainers.containers;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.platform.commons.annotation.Testable;
import org.testcontainers.containers.DockerComposeContainer;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testable
public class DockerComposeIT {
    @ClassRule
    public static DockerComposeContainer compose =
            new DockerComposeContainer(new File("src/test/resources/docker-compose.yml"))
                    .withExposedService("containerWebServer_1", 80);

    @Test
    public void given_web_server_container_when_get_request_then_return_response() throws Exception {
        String address = "http://" + compose.getServiceHost("containerWebServer_1", 80)
                + ":" + compose.getServicePort("containerWebServer_1", 80);

        String response = getRequest(address);
        assertEquals(response, "Test Containers...");
    }

    private String getRequest(String address) throws Exception {
        URL url = new URL(address);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        return content.toString();
    }
}
