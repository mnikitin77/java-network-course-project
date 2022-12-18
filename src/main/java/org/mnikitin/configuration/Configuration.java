package org.mnikitin.configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

public class Configuration {
    private static final String FILE_NAME = "server.properties";
    private static final String DEFAULT_PORT = "12345";
    private final int port;

    private static final Configuration c = new Configuration();

    private Configuration() {
        var rootPath = Objects
                .requireNonNull(Thread.currentThread().getContextClassLoader().getResource(""))
                .getPath();
        try (var reader = Files.newBufferedReader(Paths.get(rootPath + FILE_NAME))) {
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
