package io.testcontainers.commons;

import io.testcontainers.containers.MessageListenerIT;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Utils {

    public static String getFileData(String filename) throws IOException, URISyntaxException {
        URI uri = MessageListenerIT.class.getClassLoader()
                .getResource(filename).toURI();
        return new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));
    }
}
