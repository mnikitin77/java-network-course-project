package org.mnikitin.configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

public class Configuration {
    private static final String FILE_NAME = "/server.properties";
    private static final String DEFAULT_PORT = "12345";
    private final int port;

    private static final Configuration c = new Configuration();

    private Configuration() {
        try (var in = Objects.requireNonNull(getClass().getResourceAsStream(FILE_NAME));
             var reader = new BufferedReader(new InputStreamReader(in))
        ) {
            var props = new Properties();
            props.load(reader);
            port = Integer.parseInt(Optional.ofNullable(props.getProperty("port")).orElse(DEFAULT_PORT));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Configuration instance() {
        return c;
    }

    public int port() {
        return port;
    }
}
