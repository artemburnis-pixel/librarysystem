package com.library.service;

import com.library.dao.ReaderDAO;
import com.library.model.Reader;

import java.util.List;

public class ReaderService {

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

        // Перевірка на унікальність номера телефону при додаванні
        if (isPhoneNumberExists(reader.getPhoneNumber(), -1)) {
            throw new IllegalArgumentException("Читач з таким номером телефону вже існує в системі!");
        }

        readerDAO.addReader(reader);
    }

    public void updateReader(Reader reader) {
        validateReader(reader);

        if (reader.getId() <= 0) {
            throw new IllegalArgumentException("Некоректний ID читача");
        }

        // Перевірка на унікальність номера телефону при редагуванні (виключаємо поточного читача за ID)
        if (isPhoneNumberExists(reader.getPhoneNumber(), reader.getId())) {
            throw new IllegalArgumentException("Цей номер телефону вже використовується іншим читачем!");
        }

        if (!readerDAO.updateReader(reader)) {
            throw new IllegalArgumentException("Читача не знайдено");
        }
    }

    public void deleteReader(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Некоректний ID читача");
        }

        if (!readerDAO.deleteReader(id)) {
            throw new IllegalArgumentException("Читача не знайдено");
        }
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

    // Допоміжний метод для перевірки наявності телефону в базі
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