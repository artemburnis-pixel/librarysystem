package com.library.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.model.Publication;
import com.library.service.PublicationService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PublicationServlet extends HttpServlet {

    private static final Logger LOGGER =
            Logger.getLogger(PublicationServlet.class.getName());

    private final PublicationService publicationService =
            new PublicationService();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            List<Publication> publications =
                    publicationService.getAllPublications();

            objectMapper.writeValue(
                    response.getOutputStream(),
                    publications
            );

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Помилка отримання видань", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            objectMapper.writeValue(
                    response.getOutputStream(),
                    Map.of("error", "Внутрішня помилка сервера")
            );
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            Publication publication =
                    objectMapper.readValue(
                            request.getInputStream(),
                            Publication.class
                    );

            publicationService.addPublication(publication);

            response.setStatus(HttpServletResponse.SC_CREATED);

            objectMapper.writeValue(
                    response.getOutputStream(),
                    Map.of("message", "Видання успішно додано")
            );

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            objectMapper.writeValue(
                    response.getOutputStream(),
                    Map.of("error", e.getMessage())
            );

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Помилка додавання видання", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            objectMapper.writeValue(
                    response.getOutputStream(),
                    Map.of("error", "Внутрішня помилка сервера")
            );
        }
    }

    @Override
    protected void doPut(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            Publication publication =
                    objectMapper.readValue(
                            request.getInputStream(),
                            Publication.class
                    );

            publicationService.updatePublication(publication);

            objectMapper.writeValue(
                    response.getOutputStream(),
                    Map.of("message", "Видання успішно оновлено")
            );

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            objectMapper.writeValue(
                    response.getOutputStream(),
                    Map.of("error", e.getMessage())
            );

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Помилка оновлення видання", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            objectMapper.writeValue(
                    response.getOutputStream(),
                    Map.of("error", "Внутрішня помилка сервера")
            );
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request,
                            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            String idParameter = request.getParameter("id");

            if (idParameter == null || idParameter.trim().isEmpty()) {
                throw new IllegalArgumentException("Не вказано ID видання");
            }

            int id = Integer.parseInt(idParameter);

            publicationService.deletePublication(id);

            objectMapper.writeValue(
                    response.getOutputStream(),
                    Map.of("message", "Видання успішно видалено")
            );

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            objectMapper.writeValue(
                    response.getOutputStream(),
                    Map.of("error", "ID видання має бути цілим числом")
            );

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            objectMapper.writeValue(
                    response.getOutputStream(),
                    Map.of("error", e.getMessage())
            );

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Помилка видалення видання", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            objectMapper.writeValue(
                    response.getOutputStream(),
                    Map.of("error", "Внутрішня помилка сервера")
            );
        }
    }
}