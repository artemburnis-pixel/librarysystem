package com.library.service;

import com.library.dao.ReaderDAO;
import com.library.model.Reader;

import java.util.List;
import java.util.logging.Logger;

public class ReaderService {

    private static final Logger LOGGER =
            Logger.getLogger(ReaderService.class.getName());

    private final ReaderDAO readerDAO;

    public ReaderService() {
        this(new ReaderDAO());
    }

    public ReaderService(ReaderDAO readerDAO) {
        this.readerDAO = readerDAO;
    }

    public List<Reader> getAllReaders() {
        return readerDAO.getAllReaders();
    }

    public void addReader(Reader reader) {
        validateReader(reader);

        if (isPhoneNumberExists(reader.getPhoneNumber(), -1)) {
            throw new IllegalArgumentException("Читач з таким номером телефону вже існує в системі!");
        }

        readerDAO.addReader(reader);
        LOGGER.info("Зареєстровано нового читача: " + reader.getFullName());
    }

    public void updateReader(Reader reader) {
        validateReader(reader);

        if (reader.getId() <= 0) {
            throw new IllegalArgumentException("Некоректний ID читача");
        }

        if (isPhoneNumberExists(reader.getPhoneNumber(), reader.getId())) {
            throw new IllegalArgumentException("Цей номер телефону вже використовується іншим читачем!");
        }

        if (!readerDAO.updateReader(reader)) {
            throw new IllegalArgumentException("Читача не знайдено");
        }

        LOGGER.info("Оновлено дані читача ID=" + reader.getId());
    }

    public void deleteReader(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Некоректний ID читача");
        }

        if (!readerDAO.deleteReader(id)) {
            throw new IllegalArgumentException("Читача не знайдено");
        }

        LOGGER.info("Видалено читача ID=" + id);
    }

    private void validateReader(Reader reader) {
        if (reader == null) {
            throw new IllegalArgumentException("Читач не може бути порожнім");
        }

        if (reader.getFullName() == null ||
                reader.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Введіть ПІБ читача");
        }

        if (reader.getEmail() == null ||
                reader.getEmail().trim().isEmpty() ||
                !reader.getEmail().contains("@")) {
            throw new IllegalArgumentException("Введіть коректний Email читача");
        }

        if (reader.getPhoneNumber() == null ||
                reader.getPhoneNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Введіть номер телефону читача");
        }
    }

    private boolean isPhoneNumberExists(String phoneNumber, int excludeReaderId) {
        List<Reader> allReaders = readerDAO.getAllReaders();
        for (Reader r : allReaders) {
            if (r.getPhoneNumber() != null && r.getPhoneNumber().trim().equals(phoneNumber.trim())) {
                if (excludeReaderId == -1 || r.getId() != excludeReaderId) {
                    return true;
                }
            }
        }
        return false;
    }
}