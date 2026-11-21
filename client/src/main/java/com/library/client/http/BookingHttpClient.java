package com.library.client.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.library.client.model.Booking;
import com.library.client.model.BookingIndicator;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

public class BookingHttpClient {

    private static final String URL =
            "http://localhost:8080/publications/api/bookings";

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper;

    public BookingHttpClient() {
        mapper = new ObjectMapper();
        // Фіксуємо формат дати для коректної серіалізації/десеріалізації без зсувів часових поясів
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        mapper.setDateFormat(dateFormat);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public List<Booking> getAllBookings() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException(response.body());
        }

        return mapper.readValue(response.body(),
                new TypeReference<List<Booking>>() {});
    }

    public List<BookingIndicator> getBookingIndicators() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL + "?indicators=true"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException(response.body());
        }

        return mapper.readValue(response.body(),
                new TypeReference<List<BookingIndicator>>() {});
    }

    public void addBooking(Booking booking) throws Exception {
        String json = mapper.writeValueAsString(booking);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 201) {
            throw new RuntimeException(response.body());
        }
    }

    public void updateBookingStatus(int id, String status) throws Exception {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setStatus(status);

        String json = mapper.writeValueAsString(booking);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException(response.body());
        }
    }

    public void updateBooking(Booking booking) throws Exception {
        // Для редагування статус не передаємо
        booking.setStatus(null);

        String json = mapper.writeValueAsString(booking);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException(response.body());
        }
    }

    public void deleteBooking(int id) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL + "?id=" + id))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException(response.body());
        }
    }
}