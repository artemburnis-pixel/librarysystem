
package com.library.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.model.Reader;
import com.library.service.ReaderService;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class ReaderServlet extends HttpServlet {

    private final ReaderService service = new ReaderService();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        try {
            mapper.writeValue(response.getWriter(), service.getAllReaders());
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(response.getWriter(),
                    Map.of("error", "Помилка отримання читачів"));
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
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(response.getWriter(),
                    Map.of("error", e.getMessage()));
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
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(response.getWriter(),
                    Map.of("error", e.getMessage()));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request,
                            HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        try {
            String idParameter = request.getParameter("id");

            if (idParameter == null) {
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
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            mapper.writeValue(response.getWriter(),
                    Map.of("error", e.getMessage()));
        }
    }
}
