package com.library.service;

import com.library.dao.IssuanceDAO;
import com.library.model.Issuance;

import java.util.List;
import java.util.logging.Logger;

public class IssuanceService {

    private static final Logger LOGGER =
            Logger.getLogger(IssuanceService.class.getName());

    private final IssuanceDAO issuanceDAO;

    public IssuanceService() {
        this.issuanceDAO = new IssuanceDAO();
    }

    public List<Issuance> getAllIssuances() {
        return issuanceDAO.getAllIssuances();
    }

    public void addIssuance(Issuance issuance) {
        if (issuance == null) {
            throw new IllegalArgumentException(
                    "Видача не може бути порожньою");
        }

        if (issuance.getBookingId() <= 0) {
            throw new IllegalArgumentException(
                    "Некоректний ID бронювання");
        }

        if (issuance.getIssueDate() == null) {
            throw new IllegalArgumentException(
                    "Не вказана дата видачі");
        }

        if (issuance.getReturnDate() != null) {
            throw new IllegalArgumentException(
                    "Дата повернення встановлюється кнопкою повернення книги");
        }

        issuanceDAO.addIssuance(issuance);
        LOGGER.info("Оформлено видачу за бронюванням ID=" + issuance.getBookingId());
    }

    public void returnIssuance(int issuanceId) {
        if (issuanceId <= 0) {
            throw new IllegalArgumentException(
                    "Некоректний ID видачі");
        }

        boolean updated = issuanceDAO.returnIssuance(issuanceId);

        if (!updated) {
            throw new IllegalArgumentException(
                    "Видачу не знайдено або книгу вже повернуто");
        }

        LOGGER.info("Зафіксовано повернення книги за видачею ID=" + issuanceId);
    }
}