package com.octopus;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RandomQuotes implements HttpFunction {

    private final AtomicInteger count = new AtomicInteger(0);

    @Override
    public void service(final HttpRequest request, final HttpResponse response) throws IOException {
        try {
            final List<String> authors = load("/authors.txt");
            final List<String> quotes = load("/quotes.txt");
            final int randomIndex = new Random().nextInt(authors.size());

            final String json = "{\"quote\": \"" + quotes.get(randomIndex)+ "\", " +
                    "\"author\": \"" + authors.get(randomIndex) + "\", " +
                    "\"appVersion\": \"" + getVersion() + "\", " +
                    "\"environmentName\": \"Google Cloud Functions\", " +
                    "\"quoteCount\": \"" + count.getAndIncrement() + "\" " +
                    "}";

            response.getWriter().write(json);
        } catch (Exception ex) {
            response.getWriter().write("Exception: " + ex);
        }
    }

   private List<String> load(final String path) {
        try (
            final InputStream inputStream = this.getClass().getResourceAsStream(path);
            final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            final Stream<String> lines = bufferedReader.lines();
        ) {
            return lines.collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of("");
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