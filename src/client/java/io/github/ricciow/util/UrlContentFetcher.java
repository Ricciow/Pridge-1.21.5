package io.github.ricciow.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class UrlContentFetcher {

    /**
     * Fetches the raw text content from a given URL.
     *
     * @param urlString The complete URL from which to fetch content.
     * @return The content of the URL as a String.
     * @throws IOException If a network error occurs, or if the server returns a non-OK response.
     * @throws URISyntaxException If the provided URL string is malformed.
     */
    public static String fetchContentFromURL(String urlString) throws IOException, URISyntaxException {
        StringBuilder content = new StringBuilder();
        HttpURLConnection connection = null;

        try {
            URL url = new URI(urlString).toURL();

            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");

            connection.setConnectTimeout(5000); // 5 seconds to connect
            connection.setReadTimeout(5000);    // 5 seconds to read data

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP GET request failed with response code: " + responseCode + " " + connection.getResponseMessage());
            }

            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = in.readLine()) != null) {
                    content.append(line);
                }
            }

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return content.toString();
    }
}