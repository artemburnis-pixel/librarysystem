package com.library.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.model.Reader;
import com.library.service.ReaderService;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReaderServlet extends HttpServlet {

    private static final Logger LOGGER =
            Logger.getLogger(ReaderServlet.class.getName());

    private final ReaderService service = new ReaderService();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        try {
            mapper.writeValue(response.getWriter(), service.getAllReaders());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Помилка отримання читачів", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(response.getWriter(),
                    Map.of("error", "Внутрішня помилка сервера"));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        try {
            Reader reader = mapper.readValue(
                    request.getInputStream(), Reader.class);

            service.addReader(reader);

            response.setStatus(HttpServletResponse.SC_CREATED);
            mapper.writeValue(response.getWriter(),
                    Map.of("message", "Читача успішно додано"));

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(response.getWriter(),
                    Map.of("error", e.getMessage()));

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Помилка додавання читача", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(response.getWriter(),
                    Map.of("error", "Внутрішня помилка сервера"));
        }
    }

    @Override
    protected void doPut(HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        try {
            Reader reader = mapper.readValue(
                    request.getInputStream(), Reader.class);

            service.updateReader(reader);

            response.setStatus(HttpServletResponse.SC_OK);
            mapper.writeValue(response.getWriter(),
                    Map.of("message", "Читача успішно відредаговано"));

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(response.getWriter(),
                    Map.of("error", e.getMessage()));

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Помилка редагування читача", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(response.getWriter(),
                    Map.of("error", "Внутрішня помилка сервера"));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request,
                            HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        try {
            String idParameter = request.getParameter("id");

            if (idParameter == null || idParameter.isBlank()) {
                throw new IllegalArgumentException("Не вказано ID читача");
            }

            int id = Integer.parseInt(idParameter);
            service.deleteReader(id);

            response.setStatus(HttpServletResponse.SC_OK);
            mapper.writeValue(response.getWriter(),
                    Map.of("message", "Читача успішно видалено"));

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(response.getWriter(),
                    Map.of("error", "ID має бути цілим числом"));

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(response.getWriter(),
                    Map.of("error", e.getMessage()));

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Помилка видалення читача", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(response.getWriter(),
                    Map.of("error", "Внутрішня помилка сервера"));
        }
    }
}