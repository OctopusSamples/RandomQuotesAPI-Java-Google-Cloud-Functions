package com.octopus;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class RandomQuotes implements HttpFunction {

    private final AtomicInteger count = new AtomicInteger(0);

    @Override
    public void service(final HttpRequest request, final HttpResponse response) throws IOException {
        try {
            final List<String> authors = getResource("authors.txt");
            final List<String> quotes = getResource("quotes.txt");
            final int randomIndex = new Random().nextInt(authors.size());

            final String json = "{\"quote\": \"" + quotes.get(randomIndex)+ "\", " +
                    "\"author\": \"" + authors.get(randomIndex) + "\", " +
                    "\"appVersion\": \"" + getVersion() + "\", " +
                    "\"environmentName\": \"Google Cloud Functions\", " +
                    "\"quoteCount\": \"" + count.getAndIncrement() + "\" " +
                    "}";

            response.getWriter().write(json);
        } catch (Exception ex) {
            response.getWriter().write("Exception: " + ex.toString());
        }
    }

    private List<String> getResource(final String resourceName) throws URISyntaxException, IOException {
        final URL resource = getClass().getClassLoader().getResource(resourceName);
        if (resource == null) {
            throw new IllegalArgumentException("file not found!");
        } else {
            return Files.readAllLines(Paths.get(resource.toURI()));
        }
    }

    private String getVersion() {
        try
        {
            final InputStream resourceAsStream = this.getClass().getResourceAsStream(
                    "/META-INF/maven/com.octopus/randomquotesapi-gcf/pom.properties");
            final Properties props = new Properties();
            props.load( resourceAsStream );
            return props.get("version").toString();
        } catch (final Exception e) {
            return "unknown";
        }

    }
}