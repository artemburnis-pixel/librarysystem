
package com.library.client.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.client.model.Publication;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class PublicationHttpClient {

    private static final String BASE_URL =
            "http://localhost:8080/publications/api/publications";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public PublicationHttpClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    // Отримання всіх видань
    public List<Publication> getAllPublications() throws Exception {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        checkResponse(response);

        return objectMapper.readValue(
                response.body(),
                new TypeReference<List<Publication>>() {}
        );
    }

    // Додавання видання
    public String addPublication(Publication publication) throws Exception {

        String json = objectMapper.writeValueAsString(publication);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        checkResponse(response);
        return response.body();
    }

    // Оновлення видання
    public String updatePublication(Publication publication) throws Exception {

        String json = objectMapper.writeValueAsString(publication);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        checkResponse(response);
        return response.body();
    }

    // Видалення видання
    public String deletePublication(int id) throws Exception {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "?id=" + id))
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        checkResponse(response);
        return response.body();
    }

    private void checkResponse(HttpResponse<String> response) {

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException(
                    "Помилка сервера: " + response.statusCode()
                            + ", " + response.body()
            );
        }
    }
}
