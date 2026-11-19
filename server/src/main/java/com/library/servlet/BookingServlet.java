
package com.library.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.model.Booking;
import com.library.service.BookingService;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class BookingServlet extends HttpServlet {

    private final BookingService service = new BookingService();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws IOException {

        response.setContentType("application/json;charset=UTF-8");

        try {
            if ("true".equalsIgnoreCase(
                    request.getParameter("indicators"))) {

                mapper.writeValue(
                        response.getWriter(),
                        service.getAllBookingIndicators()
                );

            } else {
                mapper.writeValue(
                        response.getWriter(),
                        service.getAllBookings()
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            mapper.writeValue(
                    response.getWriter(),
                    Map.of("error", e.getMessage() == null
                            ? "Невідома помилка"
                            : e.getMessage())
            );
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws IOException {

        response.setContentType("application/json;charset=UTF-8");

        try {
            Booking booking = mapper.readValue(
                    request.getInputStream(), Booking.class);

            service.addBooking(booking);

            response.setStatus(HttpServletResponse.SC_CREATED);

            mapper.writeValue(
                    response.getWriter(),
                    Map.of("message", "Бронювання успішно додано")
            );

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            mapper.writeValue(
                    response.getWriter(),
                    Map.of("error", e.getMessage())
            );

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            mapper.writeValue(
                    response.getWriter(),
                    Map.of("error", e.getMessage() == null
                            ? "Невідома помилка"
                            : e.getMessage())
            );
        }
    }


    @Override
    protected void doPut(HttpServletRequest request,
                         HttpServletResponse response) throws IOException {

        response.setContentType("application/json;charset=UTF-8");

        try {
            Booking booking = mapper.readValue(
                    request.getInputStream(), Booking.class);

            if (booking.getStatus() != null &&
                    !booking.getStatus().isBlank()) {

                service.updateBookingStatus(
                        booking.getId(),
                        booking.getStatus()
                );

                mapper.writeValue(
                        response.getWriter(),
                        Map.of("message", "Статус бронювання успішно змінено")
                );

            } else {
                service.updateBooking(booking);

                mapper.writeValue(
                        response.getWriter(),
                        Map.of("message", "Бронювання успішно відредаговано")
                );
            }

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(response.getWriter(),
                    Map.of("error", e.getMessage()));

        } catch (IllegalStateException e) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            mapper.writeValue(response.getWriter(),
                    Map.of("error", e.getMessage()));

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(response.getWriter(),
                    Map.of("error", e.getMessage() == null
                            ? "Невідома помилка"
                            : e.getMessage()));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request,
                            HttpServletResponse response) throws IOException {

        response.setContentType("application/json;charset=UTF-8");

        try {
            String idParameter = request.getParameter("id");

            if (idParameter == null || idParameter.isBlank()) {
                throw new IllegalArgumentException(
                        "Не вказано ID бронювання");
            }

            int id = Integer.parseInt(idParameter);

            service.deleteBooking(id);

            mapper.writeValue(
                    response.getWriter(),
                    Map.of("message", "Бронювання успішно видалено")
            );

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(response.getWriter(),
                    Map.of("error", "Некоректний ID бронювання"));

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(response.getWriter(),
                    Map.of("error", e.getMessage()));

        } catch (IllegalStateException e) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            mapper.writeValue(response.getWriter(),
                    Map.of("error", e.getMessage()));

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(response.getWriter(),
                    Map.of("error", e.getMessage() == null
                            ? "Невідома помилка"
                            : e.getMessage()));
        }
    }
}
