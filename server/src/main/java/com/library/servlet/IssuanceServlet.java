package com.library.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.model.Issuance;
import com.library.service.IssuanceService;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IssuanceServlet extends HttpServlet {

    private static final Logger LOGGER =
            Logger.getLogger(IssuanceServlet.class.getName());

    private final IssuanceService service = new IssuanceService();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        try {
            mapper.writeValue(response.getWriter(), service.getAllIssuances());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Помилка отримання видач", e);
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
            Issuance issuance = mapper.readValue(request.getInputStream(), Issuance.class);
            service.addIssuance(issuance);

            response.setStatus(HttpServletResponse.SC_CREATED);
            mapper.writeValue(response.getWriter(),
                    Map.of("message", "Видачу успішно додано"));
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(response.getWriter(), Map.of("error", e.getMessage()));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Помилка додавання видачі", e);
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
            String idParameter = request.getParameter("id");
            if (idParameter == null || idParameter.isBlank()) {
                throw new IllegalArgumentException("Не вказано ID видачі");
            }

            int id = Integer.parseInt(idParameter);

            service.returnIssuance(id);

            response.setStatus(HttpServletResponse.SC_OK);
            mapper.writeValue(response.getWriter(),
                    Map.of("message", "Книгу успішно повернуто"));

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(response.getWriter(),
                    Map.of("error", "Некоректний ID видачі"));

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(response.getWriter(),
                    Map.of("error", e.getMessage()));

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Помилка повернення книги", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(response.getWriter(),
                    Map.of("error", "Внутрішня помилка сервера"));
        }
    }
}